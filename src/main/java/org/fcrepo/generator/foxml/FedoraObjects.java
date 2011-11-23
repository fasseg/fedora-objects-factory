package org.fcrepo.generator.foxml;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import com.github.cwilper.fcrepo.dto.core.ControlGroup;
import com.github.cwilper.fcrepo.dto.core.Datastream;
import com.github.cwilper.fcrepo.dto.core.FedoraObject;
import com.github.cwilper.fcrepo.dto.core.State;

public abstract class FedoraObjects {
	private static final Random random = new Random();


	public static FedoraObject generateFedoraObjectFromFile(final URI uri) throws IOException {
		return generateFedoraObjectFromURI(uri, ControlGroup.MANAGED);
	}

	public static FedoraObject generateFedoraObjectFromRandomData(final int numVersions, final long size,
			final String filePrefix)
			throws IOException {
		final Datastream datastream = new Datastream("random datastream " + UUID.randomUUID());
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

	public static FedoraObject generateFedoraObjectFromURI(final URI uri, final ControlGroup controlGroup) throws IOException{
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
