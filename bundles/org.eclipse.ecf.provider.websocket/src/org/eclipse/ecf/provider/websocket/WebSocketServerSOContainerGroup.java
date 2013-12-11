/****************************************************************************
 * Copyright (c) 2013 videoNEXT Federal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
 
package org.eclipse.ecf.provider.websocket;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;
import org.eclipse.ecf.provider.generic.SOContainerGroup;


public class WebSocketServerSOContainerGroup extends SOContainerGroup  {

	
	public static final String DEFAULT_GROUP_NAME = WebSocketServerSOContainerGroup.class.getName();

	/**
	 * @since 4.4
	 */
	public WebSocketServerSOContainerGroup(String name) {
		super(name);
	}

	
	protected void trace(String msg) {
		Trace.trace(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.DEBUG, msg);
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, WebSocketServerSOContainerGroup.class, msg, e);
	}


}