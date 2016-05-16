package org.eclipse.ecf.provider.websockets.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.remoteservice.provider.RemoteServiceDistributionProvider;
import org.osgi.service.http.HttpService;

public class WebSocketsServerDistributionProvider extends RemoteServiceDistributionProvider {

	protected WebSocketsServerDistributionProvider() {

	}

	protected WebSocketsServerDistributionProvider(String name, IContainerInstantiator instantiator) {
		super(name, instantiator);
	}

	protected WebSocketsServerDistributionProvider(String name, IContainerInstantiator instantiator, String description) {
		super(name, instantiator, description);
	}

	protected WebSocketsServerDistributionProvider(String name, IContainerInstantiator instantiator, String description, boolean server) {
		super(name, instantiator, description, server);
	}

	private List<HttpService> httpServices = Collections.synchronizedList(new ArrayList<HttpService>());

	public void bindHttpService(HttpService httpService) {
		if (httpService != null)
			httpServices.add(httpService);
	}

	public void unbindHttpService(HttpService httpService) {
		if (httpService != null)
			httpServices.remove(httpService);
	}

	public List<HttpService> getHttpServices() {
		return httpServices;
	}
}
