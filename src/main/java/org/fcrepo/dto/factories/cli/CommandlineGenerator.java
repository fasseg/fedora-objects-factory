package org.fcrepo.dto.factories.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

public class CommandlineGenerator {
	public static void main(String[] args) {
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		try{
			System.out.print("How many FOXML should be generated [default=100]? ");
			int numFoxml=Integer.parseInt(reader.readLine());
			File targetDirectory;
			do{
				System.out.print("Where should the generated FOXML files be written to [default=" + System.getProperty("java.io.tmpdir") + "]? ");
				targetDirectory=new File(reader.readLine());
				if (!targetDirectory.exists()){
					System.err.println("Directory does not exist. Please try again");
					Thread.sleep(500); // let the user notice the err msg5
				}
			}while(!targetDirectory.exists());
			System.out.print("What kind of ControlGroup should the FOXML's content streams be part of [Default=M] ? [M,I,E,R]");
			String controlGourp=reader.readLine();
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(reader);
		}
	}
}
