package org.eclipse.ecf.provider.websockets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import javax.ws.rs.core.Configuration;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.provider.RemoteServiceContainerInstantiator;

public abstract class WebSocketsContainerInstantiator extends RemoteServiceContainerInstantiator implements IRemoteServiceContainerInstantiator {

	public static final String CONFIG_PARAM = "configuration";

	protected static final String[] jaxIntents = new String[] { "jaxrs" };

	protected WebSocketsContainerInstantiator(String serverConfigTypeName) {
		this.exporterConfigs.add(serverConfigTypeName);
	}

	protected WebSocketsContainerInstantiator(String serverConfigTypeName, String clientConfigTypeName) {
		this(serverConfigTypeName);
		this.exporterConfigToImporterConfigs.put(serverConfigTypeName, Arrays.asList(new String[] { clientConfigTypeName }));
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List<String> results = new ArrayList<String>(Arrays.asList(super.getSupportedIntents(description)));
		results.addAll(Arrays.asList(jaxIntents));
		return (String[]) results.toArray(new String[results.size()]);
	}

	//protected Configuration getConfigurationFromParams(ContainerTypeDescription description, Map<String, ?> parameters) {
	//	return getParameterValue(parameters, CONFIG_PARAM, Configuration.class, null);
	//}

	//public abstract IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters/*, Configuration configuration*/);

	@Override
	public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters) {
		return null;//createInstance(description, parameters/*, getConfigurationFromParams(description, parameters)*/);
	}
}
