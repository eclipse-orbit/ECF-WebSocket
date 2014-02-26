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

package org.eclipse.ecf.examples.remoteservices.ws.host;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.websocket.WebSocketGenericContainerInstantiator;
import org.eclipse.ecf.provider.websocket.WebSocketServerSOContainer;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.osgi.util.tracker.ServiceTracker;

public class MyWebSocketHandler extends WebSocketHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4961885650266002201L;
	private ServiceTracker containerManagerServiceTracker;
	private WebSocketServerSOContainer serverSOContainer;
	
	public MyWebSocketHandler() {
		super();
		
		 IContainerManager icm = getContainerManagerService();
		 ID id = IDFactory.getDefault().createStringID(WebSocketServerSOContainer.getDefaultServerURL());
		 IContainer container = icm.getContainer(id);
		 try {
			 if (container == null)
				 container = icm.getContainerFactory().createContainer(WebSocketGenericContainerInstantiator.SERVER_NAME, new Object[] {id});
		 } catch (ContainerCreateException e) {
			 System.out.println(e);
		 }	
		 
		 serverSOContainer = (WebSocketServerSOContainer)container;	
	}	
	
	
	 @Override
	 public WebSocket doWebSocketConnect(HttpServletRequest req, String resp) {
		org.eclipse.ecf.provider.comm.websocket.WebSocket socket = new org.eclipse.ecf.provider.comm.websocket.WebSocket(serverSOContainer);
		return socket;
	}

	private IContainerManager getContainerManagerService()
    {
        if (containerManagerServiceTracker == null) {
            containerManagerServiceTracker = new ServiceTracker(Activator.getContext(), IContainerManager.class.getName(), null);
            containerManagerServiceTracker.open();
        }

        return (IContainerManager) containerManagerServiceTracker.getService();
    }

	
}
