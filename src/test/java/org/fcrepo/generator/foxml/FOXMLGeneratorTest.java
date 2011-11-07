package org.fcrepo.generator.foxml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;

public class FOXMLGeneratorTest {
	
	private static final String TEMP_FILE_PREFIX=System.getProperty("java.io.tmpdir") + "/foxml-test-" + UUID.randomUUID();
	
	@BeforeClass
	public static void init() throws IOException{
		File dir=new File(TEMP_FILE_PREFIX);
		if (dir.exists()){
			throw new IOException("test directory " + TEMP_FILE_PREFIX + " already exists, this should not happen!");
		}else{
			dir.mkdir();
		}
		
	}
	
	@AfterClass
	public static void cleanUp() throws IOException{
		File dir=new File(TEMP_FILE_PREFIX);
		if (!dir.exists()){
			throw new IOException(TEMP_FILE_PREFIX + " does not exists. this should not happen!");
		}else{
			FileUtils.deleteDirectory(dir);
		}
	}
	
	@Test
	public void testGenerateFOXMLFromFile() throws Exception {
		File dir = new File(TEMP_FILE_PREFIX);
		File tmp=File.createTempFile("datastream-orig", ".ingest",dir);
		File out=new File(URI.create("file://" + TEMP_FILE_PREFIX + "/test-foxml.xml"));
		File f = FedoraObjects.generateFOXMLFromFile(tmp,out);
		assertNotNull(f);
		assertTrue(f.length() > 0);
	}

	@Test
	public void testGenerateFOXMLFromRandomData() throws Exception {
		File f = FedoraObjects.generateFOXMLFromRandomData(3, 8167,TEMP_FILE_PREFIX);
		assertNotNull(f);
		assertTrue(f.length() > 0);
	}

	@Test
	public void testGenerateFedoraObjectFromRandomData() throws Exception {
		FedoraObject fo = FedoraObjects.generateFedoraObjectFromRandomData(3, 8198,TEMP_FILE_PREFIX);
		assertNotNull(fo);
		assertTrue(fo.datastreams().size() == 1);
		assertTrue(fo.datastreams().get(fo.datastreams().firstKey()).versions().size() == 3);
		Iterator<DatastreamVersion> versions=fo.datastreams().get(fo.datastreams().firstKey()).versions().iterator();
		while (versions.hasNext()){
			DatastreamVersion v=versions.next();
			File f=new File(v.contentLocation());
			assertTrue(f.exists());
			assertTrue(f.length() == 8198);
		}
	}
	
	@Test
	public void testGenerateDatastreamsFromFiles() throws Exception {
		List<File> files=new ArrayList<File>();
		File dir = new File(TEMP_FILE_PREFIX);
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		List<Datastream> streams=FedoraObjects.generateDatastreamsFromFiles(files);
		assertTrue(streams.size() == 3);
	}
	
	@Test
	public void testGenerateDatastreamFromFiles() throws Exception {
		List<File> files=new ArrayList<File>();
		File dir = new File(TEMP_FILE_PREFIX);
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		files.add(File.createTempFile("datastream-orig", ".ingest",dir));
		Datastream stream=FedoraObjects.generateDatastreamFromFiles(files);
		assertTrue(stream.versions().size() == 3);
	}
	
}
