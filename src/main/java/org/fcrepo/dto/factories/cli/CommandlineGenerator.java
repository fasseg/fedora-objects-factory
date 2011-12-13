package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.fcrepo.dto.factories.FOXMLs;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;

/**
 * A command line utility for creating FOXML files for testing purposes. If run
 * without any options it will default to interactive mode, fetching the
 * relevant information from the user. If used with "-p <properties-file>" it
 * reads the relevant information from the given properties file. The properties
 * get written out to "generator.properties" on every run.
 * 
 * @author fasseg
 * 
 */
public final class CommandlineGenerator extends Questionary {
	public static final String PROPERTY_INLINE_BASE64 = "generator.inline.base64";
	public static final String PROPERTY_NUM_FOXML = "generator.num.files";
	public static final String PROPERTY_TARGET_DIRECTORY = "generator.target.directory";
	public static final String PROPERTY_DATASTREAMS_RANDOM = "generator.datastreams.random";
	public static final String PROPERTY_CONTROLGROUP = "generator.controlgroup";

	private Properties properties = new Properties();

	private CommandlineGenerator(BufferedReader reader, PrintStream out) {
		super(reader, out);
	}

	public CommandlineGenerator(File propFile) throws IOException {
		super(null, null);
		FileInputStream out = null;
		try {
			out = new FileInputStream(propFile);
			properties.load(out);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private void questionNumFOXML() throws Exception {
		boolean valid = false;
		while (!valid) {
			try {
				Integer numFoxml = poseQuestion(Integer.class, 100,
						"How many FOXML should be generated [default=100]? ");
				properties.setProperty(PROPERTY_NUM_FOXML, String.valueOf(numFoxml));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			valid = true;
		}
	}

	private void questionInputFiles() throws Exception {
		boolean valid = false;
		while (!valid) {
			boolean randomDatastreams = poseQuestion(Boolean.class, true,
					"Should random data be generated for the datastreams? [default=yes]");
			properties.setProperty(PROPERTY_DATASTREAMS_RANDOM, String.valueOf(randomDatastreams));
			valid = true;
		}
	}

	private void questionTargetDirectory() throws Exception {
		boolean valid = false;
		while (!valid) {
			String path = poseQuestion(
					String.class,
					System.getProperty("java.io.tmpdir") + "/foxml-test-files",
					"Where should the generated FOXML files be written to [default="
							+ System.getProperty("java.io.tmpdir") + "/foxml-test-files]? ");
			File targetDirectory = new File(path);
			if (targetDirectory.exists() && (!targetDirectory.canWrite() || !targetDirectory.isDirectory())) {
				System.err.println("Directory is not writeable. Please try again");
				Thread.sleep(1000); // let the user notice the err msg5
			} else {
				properties.setProperty(PROPERTY_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
				valid = true;
			}
		}
	}

	private void questionControlGroup() throws Exception {
		boolean valid = false;
		ControlGroup controlGroup = null;
		while (!valid) {
			String group = poseQuestion(String.class, "M",
					"What kind of ControlGroup should the FOXML's content stream be part of [default=M] ? <M,I,E,R>")
					.toLowerCase();
			if (group.length() == 0 || group.equals("m")) {
				controlGroup = ControlGroup.MANAGED;
				valid = true;
			} else if (group.equals("i")) {
				controlGroup = ControlGroup.EXTERNAL;
				valid = true;
			} else if (group.equals("e")) {
				controlGroup = ControlGroup.INLINE_XML;
				valid = true;
			} else if (group.equals("r")) {
				controlGroup = ControlGroup.REDIRECT;
				valid = true;
			}
			if (valid) {
				properties.setProperty(PROPERTY_CONTROLGROUP, controlGroup.toString());
			}
		}
		if (controlGroup == ControlGroup.MANAGED) {
			valid = false;
			while (!valid) {
				boolean inlineBase64 = poseQuestion(Boolean.class, false,
						"Should the data be included via INLINE_BASE64? [default=no]");
				valid = true;
				properties.setProperty(PROPERTY_INLINE_BASE64, String.valueOf(inlineBase64));
			}
		}
	}

	private void startFOXMLCreation() throws IOException {
		properties.store(new FileOutputStream("generator.properties"), "created by generator");
		properties.store(System.out, "none");
		final int numFoxml = Integer.parseInt(properties.getProperty(PROPERTY_NUM_FOXML));
		final File targetDirectory = new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		final boolean randomDatastreams = Boolean.parseBoolean(properties.getProperty(PROPERTY_DATASTREAMS_RANDOM));
		final ControlGroup controlGroup = ControlGroup.valueOf(properties.getProperty(PROPERTY_CONTROLGROUP));
		final boolean inlineXMl = Boolean.parseBoolean(properties.getProperty(PROPERTY_INLINE_BASE64));
		if (!targetDirectory.exists()) {
			targetDirectory.mkdir();
		}
		List<File> foxmls = new ArrayList<File>();
		if (randomDatastreams) {
			for (int i = 0; i < numFoxml; i++) {
				if (controlGroup == ControlGroup.MANAGED && inlineXMl) {
					foxmls.add(FOXMLs.generateInlineFOXMLFromRandomData(1, 1024, targetDirectory.getAbsolutePath().toString()));
				} else {
					foxmls.add(FOXMLs.generateFOXMLFromRandomData(1, 1024L, targetDirectory.getAbsolutePath().toString(), controlGroup));
				}
			}
		} else {

		}
		System.out.println("generated " + foxmls.size() + " FOXML files");
	}

	/**
	 * Main method of the command line utility If run without any options it
	 * will default to interactive mode, fetching the relevant information from
	 * the user. If used with "-p <properties-file>" it reads the relevant
	 * information from the given properties file. The properties get written
	 * out to "generator.properties" on every run.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0 && args[0].toLowerCase().equals("-p")) {
			File propFile = new File(args[1]);
			try {
				CommandlineGenerator generator = new CommandlineGenerator(propFile);
				generator.startFOXMLCreation();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(System.in));
				CommandlineGenerator generator = new CommandlineGenerator(reader, System.out);
				generator.questionNumFOXML();
				generator.questionInputFiles();
				generator.questionTargetDirectory();
				generator.questionControlGroup();
				generator.startFOXMLCreation();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
	}
}
