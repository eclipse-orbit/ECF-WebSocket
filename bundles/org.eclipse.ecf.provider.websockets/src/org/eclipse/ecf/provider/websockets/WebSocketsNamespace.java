package org.eclipse.ecf.provider.websockets;

import java.net.URI;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.URIID;

public class WebSocketsNamespace extends Namespace {

	private static final long serialVersionUID = -3848279615939604280L;
	public static final String NAME = "ecf.namespace.websockets";
	public static WebSocketsNamespace INSTANCE;

	public WebSocketsNamespace() {
		super(NAME, "WebSocket Namespace");
		INSTANCE = this;
	}

	@Override
	public ID createInstance(Object[] parameters) throws IDCreateException {
		try {
			URI uri = null;
			if (parameters[0] instanceof URI)
				uri = (URI) parameters[0];
			else if (parameters[0] instanceof String)
				uri = URI.create((String) parameters[0]);
			if (uri == null)
				throw new IllegalArgumentException("the first parameter must be of type String or URI");
			return new URIID(INSTANCE, uri);
		} catch (Exception e) {
			throw new IDCreateException("Could not create WebSocket ID", e); //$NON-NLS-1$
		}
	}

	@Override
	public String getScheme() {
		return "ecf.websockets";
	}

}
