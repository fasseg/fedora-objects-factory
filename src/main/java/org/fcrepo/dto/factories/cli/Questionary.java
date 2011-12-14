package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;

public class Questionary {
	private final BufferedReader reader;
	private final PrintStream out;

	public Questionary(BufferedReader reader, PrintStream out) {
		this.reader = reader;
		this.out = out;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Object> T poseQuestion(Class<T> resultClazz, T defaultValue, String question) throws Exception {
		boolean valid = false;
		T result = null;
		String input = null;
		while (!valid) {
			try {
				out.print(question);
				input = reader.readLine();
				if (input.length() == 0) {
					result = defaultValue;
					valid = true;
				}else{
					if (resultClazz == String.class) {
						result = (T) input;
						valid = true;
					} else if (resultClazz == Integer.class) {
						result = (T) new Integer(input);
						valid = true;
					} else if (resultClazz == Long.class) {
						result = (T) new Long(input);
						valid=true;
					} else if (resultClazz == Boolean.class) {
						// Booleans need a little more handling than the rest
						// Since "no", "yes" and the like should be checked too
						String arg = input.toLowerCase();
						if (arg.equals("no") || arg.equals("n") || arg.equals("false")) {
							result = (T) Boolean.FALSE;
							valid = true;
						} else if (arg.equals("yes") || arg.equals("y") || arg.equals("true")) {
							result = (T) Boolean.TRUE;
							valid = true;
						} else {
							printError(input);
						}
					} else if (resultClazz == File.class) {
						result = (T) new File(input);
						valid = true;
					} else {
						throw new IllegalArgumentException("unable to handle results of type " + resultClazz.getName());
					}
				}
			} catch (NumberFormatException e) {
				printError(input);
			}
		}
		return result;
	}
	
	private void printError(String input) throws Exception {
		System.err.println("Unable to parse input '" + input + "'.Please try again");
		Thread.sleep(500);
	}

}
