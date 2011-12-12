package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.fcrepo.dto.factories.FOXMLs;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;

public final class CommandlineGenerator {
	private int numFoxml = 100;
	private File targetDirectory;
	private ControlGroup controlGroup;
	private boolean inlineBase64 = false;
	private BufferedReader reader;

	private CommandlineGenerator(BufferedReader reader) {
		this.reader = reader;
	}

	private void questionNumFOXML() throws Exception {
		// 1st question
		boolean validAnswer = false;
		do {
			System.out.print("How many FOXML should be generated [default="
					+ numFoxml + "]? ");
			String line = reader.readLine();
			if (line.length() == 0) {
				validAnswer = true;
			} else {
				try {
					numFoxml = Integer.parseInt(line);
					validAnswer = true;
				} catch (NumberFormatException nfe) {
					System.err.println(line
							+ " is not a valid number.Please try again");
					Thread.sleep(1000); // let the user notice the err msg5
				}
			}

		} while (!validAnswer);
	}

	private void questionTargetDirectory() throws Exception {
		// 2nd question
		boolean validAnswer = false;
		do {
			targetDirectory = new File(System.getProperty("java.io.tmpdir"));
			System.out
					.print("Where should the generated FOXML files be written to [default="
							+ targetDirectory.getAbsolutePath() + "]? ");
			String line = reader.readLine();
			if (line.length() == 0) {
				validAnswer = true;
			} else {
				targetDirectory = new File(line);
				if (!targetDirectory.exists()) {
					System.err
							.println("Directory does not exist. Please try again");
					Thread.sleep(1000); // let the user notice the err msg5
				} else if (!targetDirectory.canWrite()) {
					System.err
							.println("Directory is not writeable. Please try again");
					Thread.sleep(1000); // let the user notice the err msg5
				} else {
					validAnswer = true;
				}
			}
		} while (!validAnswer);
	}

	private void questionControlGroup() throws Exception {
		// 3rd question
		boolean validAnswer = false;
		do {
			controlGroup = ControlGroup.MANAGED;

			System.out
					.print("What kind of ControlGroup should the FOXML's content streams be part of [Default=M] ? [M,I,E,R]");
			String line = reader.readLine().toUpperCase();
			if (line.length() == 0 || line.equals("M")) {
				System.out
						.print("Should the data be included via INLINE_BASE64? [default=no]");
				String inline = reader.readLine().trim().toLowerCase();
				if (inline.equals("yes")) {
					inlineBase64 = true;
				}
				validAnswer = true;
			} else if (line.length() != 1) {
				System.err.println(line
						+ " is not a valid control group. Please try again");
				Thread.sleep(1000); // let the user notice the err msg5
			} else {
				if (line.equals("I")) {
					controlGroup = ControlGroup.INLINE_XML;
					validAnswer = true;
				} else if (line.equals("E")) {
					controlGroup = ControlGroup.EXTERNAL;
					validAnswer = true;
				} else if (line.equals("R")) {
					controlGroup = ControlGroup.REDIRECT;
					validAnswer = true;
				} else {
					System.err
							.println(line
									+ " is not a valid control group. Please try again");
					Thread.sleep(1000); // let the user notice the err msg5
				}
			}
		} while (!validAnswer);
	}

	private void startFOXMLCreation() throws IOException {
		System.out.println("numFOXML: " + this.numFoxml);
		System.out.println("targetDirectory: "
				+ this.targetDirectory.getAbsolutePath());
		System.out.println("ControlGroup: " + this.controlGroup);
		
		List<File> foxmls=new ArrayList<File>();
		if (this.controlGroup == ControlGroup.MANAGED) {
			System.out.println("inlineBase64: " + this.inlineBase64);
		}
		for (int i = 0; i < numFoxml; i++) {
			foxmls.add(FOXMLs.generateFOXMLFromRandomData(1, 1024, targetDirectory
					.getAbsolutePath().toString()));
		}
		System.out.println("generated " + foxmls.size() + " FOXML files");
	}

	public static void main(String[] args) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			CommandlineGenerator generator = new CommandlineGenerator(reader);
			generator.questionNumFOXML();
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
