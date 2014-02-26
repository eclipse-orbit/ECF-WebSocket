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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;

import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.comm.IConnectRequestHandler;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.ISynchConnection;
import org.eclipse.ecf.provider.comm.tcp.ConnectRequestMessage;
import org.eclipse.ecf.provider.comm.tcp.ConnectResultMessage;
import org.eclipse.ecf.provider.comm.websocket.WebSocket;
import org.eclipse.ecf.provider.comm.websocket.WebSocketClient;
import org.eclipse.ecf.provider.generic.ContainerMessage;
import org.eclipse.ecf.provider.generic.ServerSOContainer;

public class WebSocketServerSOContainer extends ServerSOContainer {
	public static final String DEFAULT_PROTOCOL = "ws"; //$NON-NLS-1$ 

	public static final String DEFAULT_NAME = System.getProperty("org.eclipse.ecf.provider.generic.ws.name", "/ecf/"); //$NON-NLS-1$ //$NON-NLS-2$"

	public static String DEFAULT_HOST = System.getProperty("org.eclipse.ecf.provider.generic.ws.host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$

	public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("org.eclipse.ecf.provider.generic.ws.port", "8080")); //$NON-NLS-1$ //$NON-NLS-2$;

	public static final String INVALID_CONNECT = "Invalid connect request."; //$NON-NLS-1$
	
	protected WebSocketServerSOContainerGroup group;

	protected boolean isSingle = false;

	public static String getServerURL(String host, String name) {
		return DEFAULT_PROTOCOL + "://" + host + ":" + DEFAULT_PORT + name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getDefaultServerURL() {
		return getServerURL("localhost", DEFAULT_NAME); //$NON-NLS-1$
	}

	/**
	 * @since 4.4
	 */
	public WebSocketServerSOContainer(ISharedObjectContainerConfig config, String path) throws IOException {
		super(config);
		isSingle = true;
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new WebSocketServerSOContainerGroup(WebSocketServerSOContainerGroup.DEFAULT_GROUP_NAME);
		this.group.add(path, this);
	}

	/**
	 * @since 4.4
	 */
	public WebSocketServerSOContainer(ISharedObjectContainerConfig config) throws IOException, URISyntaxException {
		super(config);
		isSingle = true;
		URI actualURI = new URI(getID().getName());
		int port = actualURI.getPort();
		String path = actualURI.getPath();
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new WebSocketServerSOContainerGroup(WebSocketServerSOContainerGroup.DEFAULT_GROUP_NAME);
		this.group.add(path, this);
	}

	public WebSocketServerSOContainer(ISharedObjectContainerConfig config, WebSocketServerSOContainerGroup grp) throws IOException, URISyntaxException {
		super(config);
		// Make sure URI syntax is followed.
		URI actualURI = new URI(getID().getName());
		int urlPort = actualURI.getPort();
		String path = actualURI.getPath();
		if (grp == null) {
			isSingle = true;
			this.group = new WebSocketServerSOContainerGroup(WebSocketServerSOContainerGroup.DEFAULT_GROUP_NAME);
		} else
			this.group = grp;
		group.add(path, this);
	}

	public WebSocketServerSOContainer(ISharedObjectContainerConfig config, WebSocketServerSOContainerGroup listener, String path) {
		super(config);
		initialize(listener, path);
	}

	protected void initialize(WebSocketServerSOContainerGroup listener, String path) {
		this.group = listener;
		this.group.add(path, this);
	}

	public void dispose() {
		URI aURI = null;
		try {
			aURI = new URI(getID().getName());
		} catch (Exception e) {
			// Should never happen
		}
		group.remove(aURI.getPath());
		
		super.dispose();
	}

	
	public Serializable handleConnectRequest(WebSocket socket, String target, Serializable data, ISynchAsynchConnection conn) {
		return acceptNewClient(socket, target, data, conn);
	}

	
	public void handleAccept(WebSocket aSocket) throws Exception {
		final ObjectOutputStream oStream = new ObjectOutputStream(aSocket.getOutputStream());
		oStream.flush();
		final ObjectInputStream iStream = new ObjectInputStream(aSocket.getInputStream());
		final ConnectRequestMessage req = (ConnectRequestMessage) iStream.readObject();
		if (req == null)
			throw new InvalidObjectException(INVALID_CONNECT + " Connect request message cannot be null"); //$NON-NLS-1$
		final URI uri = req.getTarget();
		if (uri == null)
			throw new InvalidObjectException(INVALID_CONNECT + " URI connect target cannot be null"); //$NON-NLS-1$
		final String path = uri.getPath();
		if (path == null)
			throw new InvalidObjectException(INVALID_CONNECT + " Path cannot be null"); //$NON-NLS-1$
		final WebSocketServerSOContainer srs = this;
		if (srs == null)
			throw new InvalidObjectException("Container not found for path=" + path); //$NON-NLS-1$
		// Create our local messaging interface
		final WebSocketClient newClient = new WebSocketClient(aSocket, iStream, oStream, srs.getReceiver());
		// No other threads can access messaging interface until space has
		// accepted/rejected
		// connect request
		synchronized (newClient) {
			// Call checkConnect
			final Serializable resp = srs.handleConnectRequest(aSocket, path, req.getData(), newClient);
			// Create connect response wrapper and send it back
			oStream.writeObject(new ConnectResultMessage(resp));
			oStream.flush();
		}
	}
	
	protected ContainerMessage acceptNewClient(WebSocket socket, String target, Serializable data, ISynchAsynchConnection conn) {
		debug("acceptNewClient(" + socket + "," + target + "," + data + "," + conn + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		ContainerMessage connectMessage = null;
		ID remoteID = null;
		try {
			connectMessage = (ContainerMessage) data;
			if (connectMessage == null)
				throw new NullPointerException("Connect message cannot be null"); //$NON-NLS-1$
			remoteID = connectMessage.getFromContainerID();
			if (remoteID == null)
				throw new NullPointerException("fromID cannot be null"); //$NON-NLS-1$
			final ContainerMessage.JoinGroupMessage jgm = (ContainerMessage.JoinGroupMessage) connectMessage.getData();
			if (jgm == null)
				throw new NullPointerException("Join group message cannot be null"); //$NON-NLS-1$
			ID memberIDs[] = null;
			synchronized (getGroupMembershipLock()) {
				if (isClosing) {
					final Exception e = new IllegalStateException("Server container is closing"); //$NON-NLS-1$
					throw e;
				}
				// Now check to see if this request is going to be allowed
				//checkJoin(socket.getRemoteSocketAddress(), remoteID, target, jgm.getData());

				// Here we check to see if the given remoteID is already
				// connected,
				// if it is, then we close the old connection and cleanup
				final ISynchConnection oldConn = getSynchConnectionForID(remoteID);
				if (oldConn != null)
					handleLeave(remoteID, oldConn);
				// Now we add the new connection
				if (addNewRemoteMember(remoteID, conn)) {
					// Notify existing remotes about new member
					try {
						forwardExcluding(getID(), remoteID, ContainerMessage.createViewChangeMessage(getID(), remoteID, getNextSequenceNumber(), new ID[] {remoteID}, true, null));
					} catch (final IOException e) {
						traceStack("Exception in acceptNewClient sending view change message", e); //$NON-NLS-1$
					}
					// Get current membership
					memberIDs = getGroupMemberIDs();
					
					// Start messaging to new member
					conn.start();
				} else {
					final ConnectException e = new ConnectException("server refused connection"); //$NON-NLS-1$
					throw e;
				}
			}
			// notify listeners
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), remoteID));

			return ContainerMessage.createViewChangeMessage(getID(), remoteID, getNextSequenceNumber(), memberIDs, true, null);
		} catch (final Exception e) {
			traceStack("Exception in acceptNewClient(" + socket + "," //$NON-NLS-1$ //$NON-NLS-2$
					+ target + "," + data + "," + conn, e); //$NON-NLS-1$ //$NON-NLS-2$
			// And then return leave group message...which means refusal
			return ContainerMessage.createViewChangeMessage(getID(), remoteID, getNextSequenceNumber(), null, false, e);
		}
	}
	
	protected Serializable getConnectDataFromInput(Serializable input) throws Exception {
		return input;
	}

}