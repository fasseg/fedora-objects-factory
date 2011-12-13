package org.fcrepo.dto.factories;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore.Entry;

import org.apache.commons.io.IOUtils;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;
import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;

/**
 * An abstract factory class for creating FOXML files
 * 
 * @author fasseg
 * 
 */
public abstract class FOXMLs {

	private FOXMLs() {
		// again with the no construction from derived class!
	}

	/**
	 * create a new FOXML file from random data
	 * 
	 * @param numVersions
	 *            the number of versions the contents should have
	 * @param size
	 *            the size of the contents
	 * @param filePrefix
	 *            the prefix for writing the content on the file system (not the
	 *            FOXML but the referenced content)
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the content
	 * @return a new {@link File} referencing the newly created FOXML
	 * @throws IOException
	 */
	public final static File generateFOXMLFromRandomData(final int numVersions, final long size,
			final String filePrefix, final ControlGroup controlGroup) throws IOException {
		final FOXMLWriter writer = new FOXMLWriter();
		final File dir = new File(filePrefix);
		final File out = File.createTempFile("testfoxml-", ".xml", dir);
		writer.writeObject(FedoraObjects.generateFedoraObjectFromRandomData(numVersions, size, filePrefix, controlGroup), new FileOutputStream(out));
		return out;
	}

	/**
	 * create a new FOXML file from random data
	 * 
	 * @param numVersions
	 *            the number of versions the content should have
	 * @param size
	 *            the size the content should have
	 * @param filePrefix
	 *            the prefix for writing the content on the file system (not the
	 *            FOXML but the referenced content)
	 * @return a new {@link File} referencing the FOXML
	 * @throws IOException
	 */
	public final static File generateInlineFOXMLFromRandomData(final int numVersions, final int size,
			final String filePrefix) throws IOException {
		final FOXMLWriter writer = new FOXMLWriter();
		final File dir = new File(filePrefix);
		final File out = File.createTempFile("testfoxml-", ".xml", dir);
		final FedoraObject fo = FedoraObjects.generateFedoraObjectFromRandomData(1, 1024, filePrefix, ControlGroup.MANAGED);
		writer.setManagedDatastreamsToEmbed(fo.datastreams().keySet());
		writer.writeObject(fo, new FileOutputStream(out));
		return out;
	}

	/**
	 * create a new FOXML file with random content
	 * 
	 * @param numVersions
	 *            the number of versions the content should have
	 * @param size
	 *            the size of the content
	 * @param filePrefix
	 *            the prefix for writing the content on the file system (not the
	 *            FOXML but the referenced content)
	 * @return a new {@link File} referencing the FOXML
	 * @throws IOException
	 */
	public final static File generateFOXMLFromRandomData(final int numVersions, final long size, final String filePrefix)
			throws IOException {
		return generateFOXMLFromRandomData(numVersions, size, filePrefix, ControlGroup.MANAGED);
	}

	/**
	 * create a new FOXML file from the contents of a {@link URI}
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the contents
	 * @return a new {@link File} referencing the FOXML
	 * @throws IOException
	 */
	public final static File generateFOXMLFromURI(final URI uri) throws IOException {
		final FOXMLWriter writer = new FOXMLWriter();
		final File outFile = File.createTempFile("testfoxml-", ".xml");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			writer.writeObject(FedoraObjects.generateFedoraObjectFromURI(uri, ControlGroup.MANAGED), out);
		} finally {
			IOUtils.closeQuietly(out);
		}
		return outFile;
	}
}
