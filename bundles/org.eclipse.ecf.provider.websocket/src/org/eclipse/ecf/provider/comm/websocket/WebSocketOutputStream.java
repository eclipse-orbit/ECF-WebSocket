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

package org.eclipse.ecf.provider.comm.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket.Connection;

public class WebSocketOutputStream extends ByteArrayOutputStream {
		
		Connection connection;
	
		public WebSocketOutputStream(Connection connection) {
			super(65536);
			this.connection = connection;
		}
		
		public void flush() throws IOException {
			if (count > 0) {
				connection.sendMessage(buf, 0, count);
				reset();
			}
		}
}
