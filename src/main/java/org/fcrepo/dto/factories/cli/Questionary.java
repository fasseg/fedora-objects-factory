package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Questionary {
	private final BufferedReader reader;
	private final PrintStream out;
	
	protected Questionary(BufferedReader reader,PrintStream out) {
		this.reader=reader;
		this.out=out;
	}
	
	protected <T extends Object> T poseQuestion(Class<T> resultClazz,T defaultValue,String question) throws Exception{
		out.print(question);
		String input=reader.readLine();
		if(input.length() == 0){
			return defaultValue;
		}
		if (resultClazz == String.class){
			return (T) input;
		} else if (resultClazz == Integer.class){
			return (T) new Integer(input);
		} else if (resultClazz == Boolean.class){
			String arg=input.toLowerCase();
			if (arg.equals("no") || arg.equals("n") || arg.equals("false")){
				return (T) Boolean.FALSE;
			}else if(arg.equals("yes") || arg.equals("y") || arg.equals("true")){
				return (T) Boolean.TRUE;
			}
			else throw new IllegalArgumentException("unable to parse " + input + " as a boolean");
		}else{
			throw new IllegalArgumentException("unable to handle results of type " + resultClazz.getName());
		}
	}
}
