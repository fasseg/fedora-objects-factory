package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.fcrepo.dto.factories.FOXMLs;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;

public final class CommandlineGenerator extends Questionary {
	private int numFoxml = 100;
	private File targetDirectory;
	private ControlGroup controlGroup;
	private boolean inlineBase64 = false;
	private boolean randomDatastreams = false;

	private CommandlineGenerator(BufferedReader reader, PrintStream out) {
		super(reader, out);
	}

	private void questionNumFOXML() throws Exception {
		boolean valid = false;
		while (!valid) {
			try {
				numFoxml = poseQuestion(Integer.class, 100, "How many FOXML should be generated [default=" + numFoxml
						+ "]? ");
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			valid = true;
		}
	}

	private void questionInputFiles() throws Exception {
		boolean valid = false;
		while (!valid) {
			randomDatastreams = poseQuestion(Boolean.class, true,
					"Should random data be generated for the datastreams? [default=yes]");
			valid = true;
		}
	}

	private void questionTargetDirectory() throws Exception {
		boolean valid = false;
		while (!valid) {
			String path = poseQuestion(
					String.class,
					System.getProperty("java.io.tmpdir"),
					"Where should the generated FOXML files be written to [default="
							+ System.getProperty("java.io.tmpdir") + "]? ");
			targetDirectory = new File(path);
			if (!targetDirectory.exists()) {
				System.err.println("Directory does not exist. Please try again");
				Thread.sleep(1000); // let the user notice the err msg5
			} else if (!targetDirectory.canWrite()) {
				System.err.println("Directory is not writeable. Please try again");
				Thread.sleep(1000); // let the user notice the err msg5
			} else {
				valid = true;
			}
		}
	}

	private void questionControlGroup() throws Exception {
		boolean valid = false;
		while (!valid) {
			String group = poseQuestion(String.class, "M",
					"What kind of ControlGroup should the FOXML'x content stream be part of [default=M] ? <M,I,E,R>")
					.toLowerCase();
			if (group.length() == 0 || group.equals("m")) {
				this.controlGroup = ControlGroup.MANAGED;
				valid = true;
			} else if (group.equals("i")) {
				this.controlGroup = ControlGroup.EXTERNAL;
				valid = true;
			} else if (group.equals("e")) {
				this.controlGroup = ControlGroup.INLINE_XML;
				valid = true;
			} else if (group.equals("r")) {
				this.controlGroup = ControlGroup.REDIRECT;
				valid = true;
			}
		}
		if (controlGroup == ControlGroup.MANAGED) {
			valid = false;
			while (!valid) {
				inlineBase64 = poseQuestion(Boolean.class, false,
						"Should the data be included via INLINE_BASE64? [default=no]");
				valid=true;
			}
		}
	}

	private void startFOXMLCreation() throws IOException {
		System.out.println("numFOXML: " + this.numFoxml);
		System.out.println("random datastreams: " + this.randomDatastreams);
		System.out.println("target directory: " + this.targetDirectory.getAbsolutePath());
		System.out.println("ControlGroup: " + this.controlGroup);
		if (this.controlGroup == ControlGroup.MANAGED) {
			System.out.println("inlineBase64: " + this.inlineBase64);
		}
		//
		// List<File> foxmls = new ArrayList<File>();
		// for (int i = 0; i < numFoxml; i++) {
		// foxmls.add(FOXMLs.generateFOXMLFromRandomData(1, 1024, targetDirectory.getAbsolutePath().toString()));
		// }
		// System.out.println("generated " + foxmls.size() + " FOXML files");
	}

	public static void main(String[] args) {
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
