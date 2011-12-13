package org.fcrepo.dto.factories;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;
import com.github.cwilper.fcrepo.dto.core.State;

/**
 * An abstract factory for creating {@link FedoraObject}s
 * 
 * @author fasseg
 * 
 */
public abstract class FedoraObjects {

	private FedoraObjects() {
		// you no construct me from derived class!
	}

	/**
	 * create a {@link FedoraObject} holding the contents of a {@link URI}
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the content
	 * @return a new {@link FedoraObject}
	 * @throws IOException
	 *             if the contents could not be fetched from {@link URI}
	 */
	public final static FedoraObject generateFedoraObjectFromFile(final URI uri) throws IOException {
		return generateFedoraObjectFromURI(uri, ControlGroup.MANAGED);
	}

	/**
	 * create a new {@link FedoraObject} with random content
	 * 
	 * @param numVersions
	 *            the number of versions the object's content should have
	 * @param size
	 *            the size of the object's content
	 * @param filePrefix
	 *            the prefix for writing the content as file on the file system
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the content
	 * @return a new {@link FedoraObject}
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}
	 */
	public final static FedoraObject generateFedoraObjectFromRandomData(final int numVersions, final long size,
			final String filePrefix, final ControlGroup controlGroup) throws IOException {
		final Datastream datastream = new Datastream("random datastream " + UUID.randomUUID())
				.controlGroup(controlGroup);
		for (int i = 0; i < numVersions; i++) {
			boolean success = false;
			while (!success) {
				success = datastream.versions().add(Datastreams.generateDatastreamVersionFromRandomData(size, filePrefix));
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

	/**
	 * create a new {@link FedoraObject} with random contents
	 * 
	 * @param numVersions
	 *            the number of versions the {@link FedoraObject}'s content
	 *            should have
	 * @param size
	 *            the size of the contents
	 * @param filePrefix
	 *            the prefix for writing the contents on the file system
	 * @return a new {@link FedoraObject}
	 * @throws IOException
	 */
	public final static FedoraObject generateFedoraObjectFromRandomData(final int numVersions, final long size,
			final String filePrefix)
			throws IOException {
		return generateFedoraObjectFromRandomData(numVersions, size, filePrefix, ControlGroup.MANAGED);
	}

	/**
	 * create a new FedoraObject with the contents of a {@link URI}
	 * 
	 * @param uri
	 *            the {@link URI} pointing to the content
	 * @param controlGroup
	 *            the {@link ControlGroup} to use for storing the content
	 * @return a new {@link FedoraObject}
	 * @throws IOException
	 *             if the contents could not be fetched from the {@link URI}
	 */
	public final static FedoraObject generateFedoraObjectFromURI(final URI uri, final ControlGroup controlGroup)
			throws IOException {
		final FedoraObject fo = new FedoraObject()
				.pid("random:" + UUID.randomUUID())
				.ownerId("testOwner")
				.label("random test object " + UUID.randomUUID())
				.state(State.ACTIVE)
				.createdDate(new Date());
		fo.lastModifiedDate(fo.createdDate());
		final Datastream ds = Datastreams.generateDatastreamFromURI(uri, controlGroup);
		fo.datastreams().put(ds.id(), ds);
		return fo;
	}

}
