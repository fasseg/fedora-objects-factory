package org.fcrepo.dto.factories;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
import com.github.cwilper.fcrepo.dto.core.InlineXML;

public class Datastreams {
	
	private final static Random random=new Random();
	
	public final static DatastreamVersion generateInlineDatastreamVersionFromRandomData(final int size) throws IOException{
		final byte[] buffer = new byte[(int) size];
				random.nextBytes(buffer);
		final InlineXML inlineXml=new InlineXML("<?xml version=\"1.0\" encoding=\"utf-8\"?><base64>" + new String(Base64.encodeBase64(buffer),"UTF-8") + "</base64>"); 
		return new DatastreamVersion("ds-" + UUID.randomUUID(), new Date())
			.mimeType("application/octet-stream")
			.formatURI(URI.create("info:fedora/fedora-system:def/foxml#"))
			.label("testobject-" + UUID.randomUUID())
			.inlineXML(inlineXml);
	}
	
	public final static DatastreamVersion generateDatastreamVersionFromRandomData(final long size, final String filePrefix)
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

	public final static Datastream generateDatastreamFromURI(final URI uri) throws IOException{
		return generateDatastreamFromURI(uri, ControlGroup.MANAGED);
	}

	public final static Datastream generateDatastreamFromURI(final URI uri, ControlGroup controlGroup) throws IOException {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		if (controlGroup == null) {
			controlGroup = ControlGroup.MANAGED;
		}
		datastream.controlGroup(controlGroup);
		datastream.versions().add(generateDatastreamVersionFromURI(uri,controlGroup));
		return datastream;

	}

	public final static Datastream generateDatastreamFromURIs(final List<URI> uris,final ControlGroup controlGroup) throws IOException {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		for (URI uri : uris) {
			boolean success = false;
			while (!success) {
				success = datastream.versions().add(generateDatastreamVersionFromURI(uri,controlGroup));
			}
		}
		return datastream;
	}

	public final static List<Datastream> generateDatastreamsFromURIs(final List<URI> uris, final ControlGroup controlGroup) throws IOException{
		final List<Datastream> streams = new ArrayList<Datastream>();
		for (URI uri : uris) {
			streams.add(generateDatastreamFromURI(uri, controlGroup));
		}
		return streams;
	}

	public final static DatastreamVersion generateDatastreamVersionFromURI(final URI uri, ControlGroup controlGroup) throws IOException{
		if (controlGroup == null) {
			controlGroup = ControlGroup.MANAGED;
		}
		final DatastreamVersion version = new DatastreamVersion("datastream-" + UUID.randomUUID(), new Date());
		version.formatURI(URI.create("info:fedora/fedora-system:def/foxml#"))
				.contentLocation(uri);
		if (controlGroup == ControlGroup.INLINE_XML){
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			final InputStream in=null;
			try{
				IOUtils.copy(uri.toURL().openStream(),out);
				final InlineXML xml=new InlineXML(Base64.encodeBase64(out.toByteArray()));
				version.inlineXML(xml);
			}finally{
				IOUtils.closeQuietly(in);
			}
		}
		return version;
	}

}
