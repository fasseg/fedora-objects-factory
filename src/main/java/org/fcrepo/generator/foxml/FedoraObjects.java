package org.fcrepo.generator.foxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;
import com.github.cwilper.fcrepo.dto.core.State;
import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;

public abstract class FedoraObjects {
	private static final Random random = new Random();

	public static FedoraObject generateFedoraObjectFromRandomData(final int numVersions, final long size,
			final String filePrefix)
			throws IOException {
		final Datastream datastream = new Datastream("random datastream " + UUID.randomUUID());
		for (int i = 0; i < numVersions; i++) {
			boolean success = false;
			while (!success) {
				success = datastream.versions().add(createDatastreamVersionFromRandomData(size, filePrefix));
			}
		}
		final FedoraObject fo = new FedoraObject()
				.pid("random:" + UUID.randomUUID())
				.ownerId("testOwner")
				.label("random test object " + UUID.randomUUID())
				.state(State.ACTIVE)
				.createdDate(new Date());
		fo.lastModifiedDate(fo.createdDate());
		fo.datastreams().put(datastream.id(), datastream);
		return fo;
	}

	public static DatastreamVersion generateDatastreamVersionFromFile(final File content) {
//		System.out.println(URI.create("file://" + content.getAbsolutePath()));
		return new DatastreamVersion("datastream-" + UUID.randomUUID(), new Date())
				.formatURI(URI.create("info:fedora/fedora-system:def/foxml#"))
				.contentLocation(URI.create("file://" + content.getAbsolutePath()));
	}

	public static Datastream generateDatastreamFromFiles(final List<File> files) {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		for (File f : files) {
			boolean success = false;
			while (!success) {
				success = datastream.versions().add(generateDatastreamVersionFromFile(f));
			}
		}
		return datastream;
	}

	public static List<Datastream> generateDatastreamsFromFiles(final List<File> files) {
		final List<Datastream> streams = new ArrayList<Datastream>();
		for (File f : files) {
			streams.add(generateDatastreamFromFile(f));
		}
		return streams;
	}

	public static Datastream generateDatastreamFromFile(final File input) {
		return generateDatastreamFromFile(input,ControlGroup.MANAGED);
	}

	public static Datastream generateDatastreamFromFile(final File input, final ControlGroup controlGroup) {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		datastream.versions().add(generateDatastreamVersionFromFile(input));
		if (controlGroup != null) {
			datastream.controlGroup(controlGroup);
		}
		return datastream;
	}

	public static FedoraObject generateFedoraObjectFromFile(final File content) {
		final FedoraObject fo = new FedoraObject()
				.pid("random:" + UUID.randomUUID())
				.ownerId("testOwner")
				.label("random test object " + UUID.randomUUID())
				.state(State.ACTIVE)
				.createdDate(new Date());
		fo.lastModifiedDate(fo.createdDate());
		final Datastream ds = generateDatastreamFromFile(content);
		fo.datastreams().put(ds.id(), ds);
		return fo;
	}

	public static File generateFOXMLFromFile(final File content, final File outFile) throws IOException {
		if (!outFile.exists()) {
			outFile.createNewFile();
		}
		final FOXMLWriter writer = new FOXMLWriter();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			writer.writeObject(generateFedoraObjectFromFile(content), out);
		} finally {
			IOUtils.closeQuietly(out);
		}
		return outFile;
	}

	public static File generateFOXMLFromRandomData(final int numVersions, final long size, final String filePrefix)
			throws IOException {
		final FOXMLWriter writer = new FOXMLWriter();
		final File dir = new File(filePrefix);
		final File out = File.createTempFile("testfoxml-", ".xml", dir);
		writer.writeObject(generateFedoraObjectFromRandomData(numVersions, size, filePrefix), new FileOutputStream(out));
		return out;
	}

	public static DatastreamVersion createDatastreamVersionFromRandomData(final long size, final String filePrefix)
			throws IOException {
		final File versionFile = new File(filePrefix + "/" + UUID.randomUUID());
		final FileOutputStream versionOut = new FileOutputStream(versionFile);
		final byte[] buffer = (size < 4096) ? new byte[(int) size] : new byte[4096];
		long sumBytes = size;
		try {
			while (sumBytes > 0) {
				random.nextBytes(buffer);
				if (sumBytes >= 4096) {
					versionOut.write(buffer, 0, 4096);
					sumBytes -= 4096;
				} else {
					versionOut.write(buffer, 0, (int) sumBytes);
					sumBytes = 0;
				}
			}
		} finally {
			IOUtils.closeQuietly(versionOut);
		}
		return new DatastreamVersion("ds-" + UUID.randomUUID(), new Date())
				.mimeType("application/octet-stream")
				.formatURI(URI.create("info:fedora/fedora-system:def/foxml#"))
				.label("testobject-" + UUID.randomUUID())
				.contentLocation(versionFile.toURI());
	}

}
