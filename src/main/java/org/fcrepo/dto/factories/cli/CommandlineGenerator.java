package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
	public static final String PROPERTY_DATASTREAM_RANDOM_SIZE = "generator.datastream.random.size";
	public static final String PROPERTY_INPUT_DIRECTORY = "generator.input.directory";
	public static final String PROPERTY_INPUT_FILETYPES = "generator.input.filetypes";

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

	private void questionRandomNumFOXML() throws Exception {
		Integer numFoxml = poseQuestion(Integer.class, 100,
				"How many FOXML should be generated [default=100]? ");
		properties.setProperty(PROPERTY_NUM_FOXML, String.valueOf(numFoxml));
		this.questionRandomContentSize();
	}

	private void questionRandomContentSize() throws Exception {
		Integer randomContent = poseQuestion(Integer.class, 10, "What size in kilobytes should the generated datastreams have [default=10] ?");
		properties.setProperty(PROPERTY_DATASTREAM_RANDOM_SIZE, String.valueOf(randomContent));
	}

	private void questionDatastreamContent() throws Exception {
		String dir = poseQuestion(String.class, System.getProperty("java.io.tmpdir"), "Where are the content files located [default="
				+ System.getProperty("java.io.tmpdir") + "] ?");
		File inputDir = new File(dir);
		properties.setProperty(PROPERTY_INPUT_DIRECTORY, inputDir.getAbsolutePath());
		if (inputDir.exists() && inputDir.canRead()) {
			this.questionDatastreamContentFiletypes();
		} else {
			System.err.println(dir + " does not exist or is not readable.");
			Thread.sleep(1000);
		}
	}

	private void questionDatastreamContentFiletypes() throws Exception {
		String fileTypes = poseQuestion(String.class, "", "Which file types should be used as content, e.g. 'jpg,jp2,png' [default=\"\"] ?");
		if (fileTypes.trim().length() == 0) {
			properties.setProperty(PROPERTY_INPUT_FILETYPES, "*");
		} else {
			properties.setProperty(PROPERTY_INPUT_FILETYPES, fileTypes);
		}
	}

	private void questionInputFiles() throws Exception {
		boolean randomDatastreams = poseQuestion(Boolean.class, true,
				"Should random data be generated for the datastreams? [default=yes]");
		properties.setProperty(PROPERTY_DATASTREAMS_RANDOM, String.valueOf(randomDatastreams));
		if (randomDatastreams) {
			this.questionRandomNumFOXML();
		} else {
			this.questionDatastreamContent();
		}
	}

	private void questionTargetDirectory() throws Exception {
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
		}
	}

	private void questionControlGroup() throws Exception {
		ControlGroup controlGroup = null;
		String group = poseQuestion(String.class, "M",
				"What kind of ControlGroup should the FOXML's content stream be part of [default=M] ? <M,I,E,R>")
				.toLowerCase();
		if (group.length() == 0 || group.equals("m")) {
			controlGroup = ControlGroup.MANAGED;
		} else if (group.equals("i")) {
			controlGroup = ControlGroup.EXTERNAL;
		} else if (group.equals("e")) {
			controlGroup = ControlGroup.INLINE_XML;
		} else if (group.equals("r")) {
			controlGroup = ControlGroup.REDIRECT;
		}
		properties.setProperty(PROPERTY_CONTROLGROUP, controlGroup.toString());
		if (controlGroup == ControlGroup.MANAGED) {
			boolean inlineBase64 = poseQuestion(Boolean.class, false,
					"Should the data be included via INLINE_BASE64? [default=no]");
			properties.setProperty(PROPERTY_INLINE_BASE64, String.valueOf(inlineBase64));
		}
	}

	private void startFOXMLCreation() throws IOException {
		properties.store(new FileOutputStream("generator.properties"), "created by generator");
		properties.store(System.out, "none");
		final boolean randomDatastreams = Boolean.parseBoolean(properties.getProperty(PROPERTY_DATASTREAMS_RANDOM));
		final File targetDirectory = new File(properties.getProperty(PROPERTY_TARGET_DIRECTORY));
		if (!targetDirectory.exists()) {
			targetDirectory.mkdir();
		}
		final List<File> foxmls;
		if (randomDatastreams) {
			foxmls = createFOXMLFromRandomData(targetDirectory);
		} else {
			foxmls = createFOXMLFromInputFiles(targetDirectory);
		}
		System.out.println("generated " + foxmls.size() + " FOXML files");
	}

	private List<File> createFOXMLFromInputFiles(File targetDirectory) throws IOException {
		final List<File> foxmls = new ArrayList<File>();
		final File inputDirectiory = new File(properties.getProperty(PROPERTY_INPUT_DIRECTORY));
		final String types = properties.getProperty(PROPERTY_INPUT_FILETYPES);
		final Set<String> fileTypes = new HashSet<String>();
		if (types.length() > 0 && !types.equals("*")) {
			fileTypes.addAll(Arrays.asList(types.split(",")));
		}
		for (File content : getFileList(inputDirectiory, types)) {
			foxmls.add(FOXMLs.generateFOXMLFromURI(content.toURI(), targetDirectory));
		}
		return foxmls;
	}

	private List<File> getFileList(File dir, String types) {
		List<File> fileList = new ArrayList<File>();
		for (String s : dir.list()) {
			File f = new File(dir, s);
			if (f.isFile()) {
				fileList.add(f);
			} else if (f.isDirectory()) {
				fileList.addAll(getFileList(f, types));
			}
		}
		return fileList;
	}

	private List<File> createFOXMLFromRandomData(final File targetDirectory) throws IOException {
		final List<File> foxmls = new ArrayList<File>();
		final int numFoxml = Integer.parseInt(properties.getProperty(PROPERTY_NUM_FOXML));
		final ControlGroup controlGroup = ControlGroup.valueOf(properties.getProperty(PROPERTY_CONTROLGROUP));
		final boolean inlineXMl = Boolean.parseBoolean(properties.getProperty(PROPERTY_INLINE_BASE64));
		final long fileSize = Long.parseLong(properties.getProperty(PROPERTY_DATASTREAM_RANDOM_SIZE)) * 1000;
		for (int i = 0; i < numFoxml; i++) {
			if (controlGroup == ControlGroup.MANAGED && inlineXMl) {
				if (fileSize > Integer.MAX_VALUE) {
					throw new IOException("filesize too large, for fitting a Base64 String in memory");
				}
				foxmls.add(FOXMLs.generateInlineFOXMLFromRandomData(1, (int) fileSize, targetDirectory));
			} else {
				foxmls.add(FOXMLs.generateFOXMLFromRandomData(1, fileSize, targetDirectory, controlGroup));
			}
		}
		return foxmls;
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
