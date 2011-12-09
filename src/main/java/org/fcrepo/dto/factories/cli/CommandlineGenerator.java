package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;

public class CommandlineGenerator {
	public static void main(String[] args) {
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		try{
			// 1st question
			boolean validAnswer=false;
			do{
				int numFoxml=100;
				System.out.print("How many FOXML should be generated [default=" + numFoxml + "]? ");
				String line=reader.readLine();
				if (line.length()==0){
					validAnswer=true;
				} else{
					try{
						numFoxml=Integer.parseInt(line);
						validAnswer=true;
					}catch(NumberFormatException nfe){
						System.err.println(line + " is not a valid number.Please try again");
						Thread.sleep(1000); // let the user notice the err msg5
					}
				}
				
			}while(!validAnswer);
			
			// 2nd question
			validAnswer=false;
			File targetDirectory;
			do{
				targetDirectory=new File(System.getProperty("java.io.tmpdir"));
				System.out.print("Where should the generated FOXML files be written to [default=" + targetDirectory.getAbsolutePath() + "]? ");
				String line=reader.readLine();
				if (line.length() == 0){
					validAnswer=true;
				}else{
					targetDirectory=new File(line);
					if (!targetDirectory.exists()){
						System.err.println("Directory does not exist. Please try again");
						Thread.sleep(1000); // let the user notice the err msg5
					}else if(!targetDirectory.canWrite()){
						System.err.println("Directory is not writeable. Please try again");
						Thread.sleep(1000); // let the user notice the err msg5
					}else{
						validAnswer=true;
					}
				}
			}while(!validAnswer);
			
			// 3rd question
			validAnswer=false;
			do{
				ControlGroup group=ControlGroup.MANAGED;
				System.out.print("What kind of ControlGroup should the FOXML's content streams be part of [Default=M] ? [M,I,E,R]");
				String line=reader.readLine().toUpperCase();
				if (line.length()==0){
					validAnswer=true;
				} else if (line.length()!=1){
					System.err.println(line + " is not a valid control group. Please try again");
					Thread.sleep(1000); // let the user notice the err msg5
				} else {
					if (line.equals("M")){
						group=ControlGroup.MANAGED;
						validAnswer=true;
					}else if(line.equals("I")){
						group=ControlGroup.INLINE_XML;
						validAnswer=true;
					}else if(line.equals("E")){
						group=ControlGroup.EXTERNAL;
						validAnswer=true;
					}else if(line.equals("R")){
						group=ControlGroup.REDIRECT;
						validAnswer=true;
					} else {
						System.err.println(line + " is not a valid control group. Please try again");
						Thread.sleep(1000); // let the user notice the err msg5
					}
				}
			}while(!validAnswer);
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(reader);
		}
	}
}
