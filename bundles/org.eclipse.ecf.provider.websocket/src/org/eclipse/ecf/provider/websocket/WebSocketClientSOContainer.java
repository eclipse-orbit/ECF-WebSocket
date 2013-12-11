/****************************************************************************
 * Copyright (c) 2013 videoNEXT Federal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package org.eclipse.ecf.provider.websocket;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.websocket.WebSocketClient;
import org.eclipse.ecf.provider.generic.ClientSOContainer;
import org.eclipse.ecf.provider.generic.SOContainerConfig;

public class WebSocketClientSOContainer extends ClientSOContainer {
	int keepAlive = 0;

	public static final int DEFAULT_TCP_CONNECT_TIMEOUT = 30000;

	public static final String DEFAULT_COMM_NAME = WebSocketClient.class.getName();

	public WebSocketClientSOContainer(ISharedObjectContainerConfig config) {
		super(config);
	}

	public WebSocketClientSOContainer(ISharedObjectContainerConfig config, int ka) {
		super(config);
		keepAlive = ka;
	}

	protected int getConnectTimeout() {
		return DEFAULT_TCP_CONNECT_TIMEOUT;
	}

	/**
	 * @param remoteSpace
	 * @param data
	 * @return ISynchAsynchConnection a non-<code>null</code> instance.
	 * @throws ConnectionCreateException not thrown by this implementation.
	 */
	protected ISynchAsynchConnection createConnection(ID remoteSpace, Object data) throws ConnectionCreateException {
		debug("createClientConnection:" + remoteSpace + ":" + data); //$NON-NLS-1$ //$NON-NLS-2$
		ISynchAsynchConnection conn = null;
		try {
			conn = new WebSocketClient(receiver, keepAlive);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

	public static final void main(String[] args) throws Exception {
		ISharedObjectContainerConfig config = new SOContainerConfig(IDFactory.getDefault().createGUID());
		WebSocketClientSOContainer container = new WebSocketClientSOContainer(config);
		// now join group
		ID serverID = IDFactory.getDefault().createStringID("ecfws://localhost:8080/ecf/"); //$NON-NLS-1$
		container.connect(serverID, null);
		Thread.sleep(200000);
	}

}