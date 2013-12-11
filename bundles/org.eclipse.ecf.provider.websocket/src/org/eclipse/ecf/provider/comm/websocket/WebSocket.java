/****************************************************************************
 * Copyright (c) 2013 videoNEXT Federal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package org.eclipse.ecf.provider.comm.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ecf.provider.websocket.WebSocketServerSOContainer;
import org.eclipse.jetty.websocket.WebSocket.OnFrame;


public class WebSocket implements OnFrame {

	Connection connection;
	WebSocketInputStream is;
	WebSocketOutputStream os;
	WebSocketServerSOContainer socontainer;

	public WebSocket() {	
	}

	public WebSocket(WebSocketServerSOContainer socontainer) {
		this.socontainer = socontainer;
	}

	public InputStream getInputStream() throws IOException {
		return is;
	}

	public OutputStream getOutputStream() throws IOException {
		return os;
	}

	public void onClose(int arg0, String arg1) {
		try {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void onOpen(Connection arg0) {
		if (connection == null) {
			connection = arg0;
			is = new WebSocketInputStream();
			os = new WebSocketOutputStream(connection);

			if (socontainer != null) {
				new Thread(new Runnable() {
					public void run() {
						try {
							socontainer.handleAccept(WebSocket.this);
						} catch (Exception e) {
							e.printStackTrace();
							connection.close();
						}
					}
				}).start();
			}	
		}
	}

	boolean isConnected() {
		return connection != null && connection.isOpen();
	}

	void close() {
		if (connection != null)
			connection.close();
	}

	private static final byte CONTINUATION_FRAME = 0; 
	private static final byte BINARY_FRAME = 2; 
	public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
		if (opcode == BINARY_FRAME || opcode == CONTINUATION_FRAME) {
			try {
				is.onFrame(data, offset, length);
			} catch (IOException e) {
				e.printStackTrace();
				connection.close();
			}
			return true;
		}

		return false;
	}

	public void onHandshake(FrameConnection arg0) {
	}

}
