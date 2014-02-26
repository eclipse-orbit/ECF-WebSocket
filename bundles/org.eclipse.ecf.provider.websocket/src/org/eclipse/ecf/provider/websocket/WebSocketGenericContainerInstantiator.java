/****************************************************************************
 * Copyright (c) 2013 videoNEXT Federal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Max Petrov, videoNEXT Federal Inc - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.websocket;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;
import org.eclipse.ecf.provider.generic.SOContainerConfig;

public class WebSocketGenericContainerInstantiator implements IContainerInstantiator, IRemoteServiceContainerInstantiator {

	/**
	 * @since 2.0
	 */
	protected static final String[] genericProviderIntents = {"passByValue", "exactlyOnce", "ordered",}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String CLIENT_NAME = "ecf.generic.ws.client"; //$NON-NLS-1$

	public static final String SERVER_NAME = "ecf.generic.ws.server"; //$NON-NLS-1$

	private static final int CREATE_INSTANCE_ERROR_CODE = 4441;

	private static final String ID_PROP = "id"; //$NON-NLS-1$

	private static final String HOSTNAME_PROP = "hostname"; //$NON-NLS-1$

	private static final String PORT_PROP = "port"; //$NON-NLS-1$

	private static final String PATH_PROP = "path"; //$NON-NLS-1$

	public WebSocketGenericContainerInstantiator() {
		super();
	}

	protected ID getIDFromArg(Object arg) throws IDCreateException {
		if (arg == null)
			throw new IDCreateException("id cannot be null"); //$NON-NLS-1$
		String val = null;
		if (arg instanceof StringID)
			return (ID) arg;
		else if (arg instanceof GUID)
			val = ((GUID) arg).getName();
		else if (arg instanceof URIID)
			val = ((URIID) arg).toURI().toString();
		else if (arg instanceof LongID)
			val = ((LongID) arg).getName();
		if (arg instanceof String)
			val = (String) arg;
		if (arg instanceof Integer)
			val = IDFactory.getDefault().createGUID(((Integer) arg).intValue()).getName();
		if (val == null)
			val = IDFactory.getDefault().createGUID().getName();
		return IDFactory.getDefault().createStringID(val);
	}

	protected Integer getIntegerFromArg(Object arg) {
		if (arg == null)
			return new Integer(-1);
		if (arg instanceof Integer)
			return (Integer) arg;
		else if (arg instanceof String) {
			return new Integer((String) arg);
		} else
			throw new IllegalArgumentException("arg=" + arg + " is not of integer type"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected class GenericContainerArgs {
		ID id;

		public GenericContainerArgs(ID id) {
			this.id = id;
		}

	
		public ID getID() {
			return id;
		}
	}

	/**
	 * @since 3.0
	 */
	protected GenericContainerArgs getClientArgs(Object[] args) throws IDCreateException {
		ID newID = null;
		if (args != null && args.length > 0) {
			if (args[0] instanceof Map) {
				Map map = (Map) args[0];
				Object idVal = map.get(ID_PROP);
				if (idVal == null)
					idVal = IDFactory.getDefault().createGUID();
				else
					newID = getIDFromArg(idVal);
			} else if (args.length > 1) {
				if (args[0] instanceof String || args[0] instanceof ID)
					newID = getIDFromArg(args[0]);
			} else
				newID = getIDFromArg(args[0]);
		}
		if (newID == null)
			newID = IDFactory.getDefault().createStringID(IDFactory.getDefault().createGUID().getName());
		return new GenericContainerArgs(newID);
	}

	protected boolean isClient(ContainerTypeDescription description) {
		if (description.getName().equals(SERVER_NAME))
			return false;
		return true;
	}

	/**
	 * @since 3.0
	 */
	protected GenericContainerArgs getServerArgs(Object[] args) throws IDCreateException {
		ID newID = null;
	
		if (args != null && args.length > 0) {
			if (args[0] instanceof Map) {
				Map map = (Map) args[0];
				Object idVal = map.get(ID_PROP);
				if (idVal != null) {
					newID = getIDFromArg(idVal);
				} else {
					Object hostVal = map.get(HOSTNAME_PROP);
					if (hostVal != null) {
						if (!(hostVal instanceof String))
							throw new IllegalArgumentException("hostname value must be of type String"); //$NON-NLS-1$
					}
					int port = -1;
					Object portVal = map.get(PORT_PROP);
					if (portVal != null)
						port = getIntegerFromArg(portVal).intValue();
					if (port < 0)
						port = WebSocketServerSOContainer.DEFAULT_PORT;
					String path = WebSocketServerSOContainer.DEFAULT_NAME;
					Object pathVal = map.get(PATH_PROP);
					if (pathVal != null) {
						if (!(pathVal instanceof String))
							throw new IllegalArgumentException("path value must be of type String"); //$NON-NLS-1$
						path = (String) pathVal;
					}
					newID = createWebSocketServerID(WebSocketServerSOContainer.DEFAULT_HOST, port, path);
				}
			} else if (args.length > 1) {
				if (args[0] instanceof String || args[0] instanceof ID)
					newID = getIDFromArg(args[0]);
			} else
				newID = getIDFromArg(args[0]);
		}
		if (newID == null) {
			newID = createWebSocketServerID(WebSocketServerSOContainer.DEFAULT_HOST, WebSocketServerSOContainer.DEFAULT_PORT, WebSocketServerSOContainer.DEFAULT_NAME);
		}
		return new GenericContainerArgs(newID);
	}

	private ID createWebSocketServerID(String hostname, int port, String path) {
		return IDFactory.getDefault().createStringID(WebSocketServerSOContainer.DEFAULT_PROTOCOL + "://" + hostname + ":" + port + path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public IContainer createInstance(ContainerTypeDescription description, Object[] args) throws ContainerCreateException {
		boolean isClient = isClient(description);
		try {
			GenericContainerArgs gcargs = null;
			if (isClient) {
				gcargs = getClientArgs(args);
				return new WebSocketClientSOContainer(new SOContainerConfig(gcargs.getID()));
			}
			// This synchronized block is to prevent issues with
			// multithreaded access to ServerPort (to find available port)
			synchronized (this) {
				gcargs = getServerArgs(args);
				return new WebSocketServerSOContainer(new SOContainerConfig(gcargs.getID()));
			}
		} catch (Exception e) {
			Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "createInstance", e); //$NON-NLS-1$
			ProviderPlugin.getDefault().log(new Status(IStatus.ERROR, ProviderPlugin.PLUGIN_ID, CREATE_INSTANCE_ERROR_CODE, "createInstance", e)); //$NON-NLS-1$
			Trace.throwing(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_THROWING, this.getClass(), "createInstance", e); //$NON-NLS-1$
			throw new ContainerCreateException("Create of containerType=" + description.getName() + " failed.", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected Set getAdaptersForClass(Class clazz) {
		Set result = new HashSet();
		IAdapterManager adapterManager = ProviderPlugin.getDefault().getAdapterManager();
		if (adapterManager != null)
			result.addAll(Arrays.asList(adapterManager.computeAdapterTypes(clazz)));
		return result;
	}

	protected Set getInterfacesForClass(Set s, Class clazz) {
		if (clazz.equals(Object.class))
			return s;
		s.addAll(getInterfacesForClass(s, clazz.getSuperclass()));
		s.addAll(Arrays.asList(clazz.getInterfaces()));
		return s;
	}

	protected Set getInterfacesForClass(Class clazz) {
		Set clazzes = getInterfacesForClass(new HashSet(), clazz);
		Set result = new HashSet();
		for (Iterator i = clazzes.iterator(); i.hasNext();)
			result.add(((Class) i.next()).getName());
		return result;
	}

	protected String[] getInterfacesAndAdaptersForClass(Class clazz) {
		Set result = getAdaptersForClass(clazz);
		result.addAll(getInterfacesForClass(clazz));
		return (String[]) result.toArray(new String[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		if (!isClient(description))
			return getInterfacesAndAdaptersForClass(WebSocketServerSOContainer.class);
		return getInterfacesAndAdaptersForClass(WebSocketClientSOContainer.class);
	}

	/**
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 * @since 2.0
	 */
	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		if (!isClient(description))
			return new Class[][] { {ID.class}, {ID.class, Integer.class}};
		return new Class[][] { {}, {ID.class}, {ID.class, Integer.class}};
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return genericProviderIntents;
	}

	/**
	 * @since 3.0
	 */
	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] {description.getName()};
	}

	/**
	 * @since 3.0
	 */
	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		if (exporterSupportedConfigs == null)
			return null;
		List results = new ArrayList();
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		// For a server, if exporter is a client then we can be an importer
		if (SERVER_NAME.equals(description.getName())) {
			if (supportedConfigs.contains(CLIENT_NAME))
				results.add(SERVER_NAME);
			// For a client, if exporter is server we can import
			// or if remote is either generic server or generic client
		} else if (CLIENT_NAME.equals(description.getName())) {
			if (supportedConfigs.contains(SERVER_NAME) || supportedConfigs.contains(CLIENT_NAME))
				results.add(CLIENT_NAME);
		}
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	/**
	 * @since 3.0
	 */
	public Dictionary getPropertiesForImportedConfigs(ContainerTypeDescription description, String[] importedConfigs, Dictionary exportedProperties) {
		return null;
	}
}
