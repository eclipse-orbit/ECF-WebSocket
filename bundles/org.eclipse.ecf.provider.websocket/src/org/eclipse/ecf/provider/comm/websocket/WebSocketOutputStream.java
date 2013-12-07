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
