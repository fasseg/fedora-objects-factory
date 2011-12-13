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

/**
 * Abstract factory class for creating {@link Datastream} and
 * {@link DatastreamVersion} objects
 * 
 * @author fasseg
 */
public abstract class Datastreams {

	private final static Random random = new Random();

	private Datastreams() {
		// you no construct me from derived class.
	}

	/**
	 * create a new {@link DatastreamVersion} with random content. The content
	 * will be created as a file on the filesystem for later access.
	 * 
	 * @param size
	 *            the size of the {@link DatastreamVersion}'s content
	 * @param filePrefix
	 *            the prefix for generation of the content
	 * @return a new {@link DatastreamVersion}
	 * @throws IOException
	 *             the content could not be written to the filesystem
	 */
	public final static DatastreamVersion generateDatastreamVersionFromRandomData(final long size,
			final String filePrefix)
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

	/**
	 * create a new {@link Datastream} of {@link ControlGroup} "M" holding the
	 * contents of a URI
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the contents which are to be
	 *            stored
	 * @return a new {@link Datastream}
	 * @throws IOException
	 *             If the contents could not be fetched from the {@link URI}
	 */
	public final static Datastream generateDatastreamFromURI(final URI uri) throws IOException {
		return generateDatastreamFromURI(uri, ControlGroup.MANAGED);
	}

	/**
	 * create a new {@link Datastream} of a given {@link ControlGroup} from the
	 * contents of a given {@link URI}
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the contents
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the contents
	 * @return a new {@link Datastream}
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}
	 */
	public final static Datastream generateDatastreamFromURI(final URI uri, ControlGroup controlGroup)
			throws IOException {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		if (controlGroup == null) {
			controlGroup = ControlGroup.MANAGED;
		}
		datastream.controlGroup(controlGroup);
		datastream.versions().add(generateDatastreamVersionFromURI(uri, controlGroup));
		return datastream;

	}

	/**
	 * create a new {@link Datastream} from a {@link List} of {@link URI}s of a
	 * given {@link ControlGroup}
	 * 
	 * @param uris
	 *            the {@link List} of {@link URI}s pointing to the
	 *            {@link Datastream}'s content
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the contents
	 * @return a new {@link Datastream}
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}s
	 */
	public final static Datastream generateDatastreamFromURIs(final List<URI> uris, final ControlGroup controlGroup)
			throws IOException {
		final Datastream datastream = new Datastream("datastream-" + UUID.randomUUID());
		for (URI uri : uris) {
			boolean success = false;
			while (!success) {
				success = datastream.versions().add(generateDatastreamVersionFromURI(uri, controlGroup));
			}
		}
		return datastream;
	}

	/**
	 * create a new {@link List} of {@link Datastream}s each holding one of the
	 * {@link URI}'s content repspectively
	 * 
	 * @param uris
	 *            the {@link List} of {@link URI}s to use for the
	 *            {@link Datastream}s' contents
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the contents
	 * @return a {@link List} of new {@link Datastream}s
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}s
	 */
	public final static List<Datastream> generateDatastreamsFromURIs(final List<URI> uris,
			final ControlGroup controlGroup) throws IOException {
		final List<Datastream> streams = new ArrayList<Datastream>();
		for (URI uri : uris) {
			streams.add(generateDatastreamFromURI(uri, controlGroup));
		}
		return streams;
	}

	/**
	 * create a new {@link DatastreamVersion} from the contents of an
	 * {@link URI} with a given {@link ControlGroup}
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the contents
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the contents
	 * @return a new {@link DatastreamVersion}
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}
	 */
	public final static DatastreamVersion generateDatastreamVersionFromURI(final URI uri, ControlGroup controlGroup)
			throws IOException {
		if (controlGroup == null) {
			controlGroup = ControlGroup.MANAGED;
		}
		final DatastreamVersion version = new DatastreamVersion("datastream-" + UUID.randomUUID(), new Date());
		version.formatURI(URI.create("info:fedora/fedora-system:def/foxml#"))
				.contentLocation(uri);
		if (controlGroup == ControlGroup.INLINE_XML) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final InputStream in = null;
			try {
				IOUtils.copy(uri.toURL().openStream(), out);
				final InlineXML xml = new InlineXML(out.toByteArray());
				version.inlineXML(xml);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		return version;
	}

}
