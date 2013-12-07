package org.eclipse.ecf.provider.comm.websocket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class WebSocketInputStream extends PipedInputStream {

	PipedOutputStream pos;
	
	public WebSocketInputStream() {
		super(65536);
		pos = new PipedOutputStream();
		try {
			pos.connect(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if (pos != null) {
			pos.close();
			pos.flush();
		}
	}
	
	void onFrame(byte[] b, int off, int len) throws IOException {
		pos.write(b, off, len);
		pos.flush();
	}

}
