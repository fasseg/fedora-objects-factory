package org.fcrepo.generator.foxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.fcrepo.dto.factories.Datastreams;
import org.fcrepo.dto.factories.FOXMLs;
import org.fcrepo.dto.factories.FedoraObjects;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;
import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;

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
		File f = FOXMLs.generateFOXMLFromURI(URI.create("file://" + TEMP_FILE_PREFIX + "/invalid"));
		assertNotNull(f);
		assertTrue(f.length() > 0);
	}

	@Test
	public void testGenerateFOXMLFromRandomData() throws Exception {
		File f = FOXMLs.generateFOXMLFromRandomData(3, 8167,TEMP_FILE_PREFIX);
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
		List<URI> files=new ArrayList<URI>();
		File dir = new File(TEMP_FILE_PREFIX);
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		List<Datastream> streams=Datastreams.generateDatastreamsFromURIs(files,ControlGroup.MANAGED);
		assertTrue(streams.size() == 3);
	}
	
	@Test
	public void testGenerateDatastreamFromFiles() throws Exception {
		List<URI> files=new ArrayList<URI>();
		File dir = new File(TEMP_FILE_PREFIX);
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		files.add(File.createTempFile("datastream-orig", ".ingest",dir).toURI());
		Datastream stream=Datastreams.generateDatastreamFromURIs(files,ControlGroup.MANAGED);
		assertTrue(stream.versions().size() == 3);
	}
	
	@Test
	public void testGenerateDatastreamsFromFileWithControlGroup() throws Exception{
		File tmp = File.createTempFile("datastream-orig", ".ingest",new File(TEMP_FILE_PREFIX));
		Datastream ds=Datastreams.generateDatastreamFromURI(tmp.toURI(), ControlGroup.EXTERNAL);
		assertEquals(ds.controlGroup(),ControlGroup.EXTERNAL);
	}
	
	@Test
	public void testGenerateFedoraObjectFromFileWithControlGroup() throws Exception{
		File tmp = File.createTempFile("datastream-orig", ".ingest",new File(TEMP_FILE_PREFIX));
		FedoraObject object=FedoraObjects.generateFedoraObjectFromURI(tmp.toURI(), ControlGroup.EXTERNAL);
		Datastream ds=object.datastreams().get(object.datastreams().firstKey());
		assertEquals(ds.controlGroup(),ControlGroup.EXTERNAL);
	}
	
	@Test
	public void testGenerateFedoraObjectFromFileWithInlineXML() throws Exception{
		File tmp = new File(this.getClass().getClassLoader().getResource("logback.xml").toURI());
		FedoraObject object=FedoraObjects.generateFedoraObjectFromURI(tmp.toURI(), ControlGroup.INLINE_XML);
		Datastream ds=object.datastreams().get(object.datastreams().firstKey());
		assertEquals(ds.controlGroup(),ControlGroup.INLINE_XML);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		new FOXMLWriter().writeObject(object, out);
		ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray());
		System.out.println(prettyPrint(in));
	}

	private String prettyPrint(InputStream in) throws Exception{
		Document document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
		LSSerializer writer = impl.createLSSerializer();
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		LSOutput output = impl.createLSOutput();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		output.setByteStream(out);
		writer.write(document, output);
		return new String(out.toByteArray());
	}
}
