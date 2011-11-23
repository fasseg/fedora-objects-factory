package org.fcrepo.generator.foxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;

public class FOXMLs {
	public static File generateFOXMLFromRandomData(final int numVersions, final long size, final String filePrefix)
			throws IOException {
		final FOXMLWriter writer = new FOXMLWriter();
		final File dir = new File(filePrefix);
		final File out = File.createTempFile("testfoxml-", ".xml", dir);
		writer.writeObject(FedoraObjects.generateFedoraObjectFromRandomData(numVersions, size, filePrefix),
				new FileOutputStream(out));
		return out;
	}

	public static File generateFOXMLFromURI(final URI uri, final File outFile) throws IOException {
		if (!outFile.exists()) {
			outFile.createNewFile();
		}
		final FOXMLWriter writer = new FOXMLWriter();
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
