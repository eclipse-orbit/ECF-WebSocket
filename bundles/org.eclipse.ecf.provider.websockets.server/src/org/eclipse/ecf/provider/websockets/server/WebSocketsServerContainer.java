package org.eclipse.ecf.provider.websockets.server;

import java.util.Dictionary;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.ecf.provider.websockets.WebSocketsNamespace;
import org.eclipse.ecf.provider.websockets.server.WebSocketsServerContainer.WebSocketsServerRemoteServiceContainerAdapter.WebSocketsServerRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.AbstractRSAContainer;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceRegistrationImpl;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public abstract class WebSocketsServerContainer extends AbstractRSAContainer {

	public static final String SERVLET_PROPERTIES_PARAM = ".servletProperties"; // expected
																				// value
																				// type=Dictionary
	public static final String SERVLET_HTTPCONTEXT_PARAM = ".servletHttpContext"; // expected
																					// value
																					// type
																					// =
																					// HttpContext

	protected static final String SLASH = "/";

	private final String urlContext;
	private final String alias;
	
	public WebSocketsServerContainer(String urlContext, String alias) {
		super(WebSocketsNamespace.INSTANCE.createInstance(new Object[] { urlContext + alias }));
		this.urlContext = urlContext;
		this.alias = alias;
	}

	public class WebSocketsServerRemoteServiceContainerAdapter extends RSARemoteServiceContainerAdapter {

		public WebSocketsServerRemoteServiceContainerAdapter(AbstractRSAContainer container) {
			super(container);
		}
		@Override
		protected RemoteServiceRegistrationImpl createRegistration() {
			return new WebSocketsServerRemoteServiceRegistration();
		}
		
		public class WebSocketsServerRemoteServiceRegistration extends RSARemoteServiceRegistration {

			private static final long serialVersionUID = 2376479911719219503L;
			
			private String servletAlias;
			
			public String getServletAlias() {
				return this.servletAlias;
			}
			
			public void setServletAlias(String servletAlias) {
				this.servletAlias = servletAlias;
			}
		}
	}
	
	protected RSARemoteServiceContainerAdapter createContainerAdapter() {
		return new WebSocketsServerRemoteServiceContainerAdapter(this);
	}

	protected String getAlias() {
		return alias;
	}

	protected String getUrlContext() {
		return urlContext;
	}

	@SuppressWarnings("rawtypes")
	protected Dictionary createServletProperties(WebSocketsServerRemoteServiceRegistration registration) {
		return getKeyEndsWithPropertyValue(registration, SERVLET_PROPERTIES_PARAM, Dictionary.class);
	}

	protected HttpContext createServletContext(WebSocketsServerRemoteServiceRegistration registration) {
		return getKeyEndsWithPropertyValue(registration, SERVLET_HTTPCONTEXT_PARAM, HttpContext.class);
	}

	protected String createServletAlias(WebSocketsServerRemoteServiceRegistration registration) {
		//return getAlias();
		return (String) registration.getProperty("ecf.WebSockets.jetty.server.alias");
	}

	protected abstract Servlet createServlet(WebSocketsServerRemoteServiceRegistration registration);
	
	@SuppressWarnings("unchecked")
	private <T> T getKeyEndsWithPropertyValue(WebSocketsServerRemoteServiceRegistration registration, String keyEndsWith, Class<T> valueType) {
		for (String key: registration.getPropertyKeys()) {
				if (key.endsWith(keyEndsWith)) {
					Object v = registration.getProperty(key);
					if (valueType.isInstance(v))
						return (T) v;
				}
		}
		return null;
	}

	protected abstract HttpService getHttpService();

	@Override
	protected Map<String, Object> exportRemoteService(RSARemoteServiceRegistration registration) {
		
		WebSocketsServerRemoteServiceRegistration reg = (WebSocketsServerRemoteServiceRegistration) registration;
		// Create Servlet Alias
		String servletAlias = createServletAlias(reg);
		// Create Servlet
		Servlet servlet = createServlet(reg);
		// Create servletProps
		@SuppressWarnings("rawtypes")
		Dictionary servletProps = createServletProperties(reg);
		// Create HttpContext
		HttpContext servletContext = createServletContext(reg);

		try {
			getHttpService().registerServlet(servletAlias, servlet, servletProps, servletContext);
		} catch (ServletException | NamespaceException e) {
			throw new RuntimeException("Cannot register servlet with alias=" + getAlias(), e);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error registering servlet with alias=" + getAlias(), e);
		}

		((WebSocketsServerRemoteServiceRegistration) registration).setServletAlias(servletAlias);

		return createExtraProperties(registration);
	}

	protected Map<String, Object> createExtraProperties(RSARemoteServiceRegistration registration) {
		return null;
	}

	@Override
	protected void unexportRemoteService(RSARemoteServiceRegistration registration) {
		WebSocketsServerRemoteServiceRegistration reg = (WebSocketsServerRemoteServiceRegistration) registration;
		String servletAlias = reg.getServletAlias();
		if (servletAlias != null) {
			HttpService httpService = getHttpService();
			if (httpService != null)
				httpService.unregister(servletAlias);
		}
	}

}
