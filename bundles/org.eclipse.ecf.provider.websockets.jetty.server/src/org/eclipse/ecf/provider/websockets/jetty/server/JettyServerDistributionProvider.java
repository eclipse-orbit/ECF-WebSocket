package org.eclipse.ecf.provider.websockets.jetty.server;

import java.util.Map;

import javax.servlet.Servlet;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.websockets.WebSocketsContainerInstantiator;
import org.eclipse.ecf.provider.websockets.server.WebSocketsServerContainer;
import org.eclipse.ecf.provider.websockets.server.WebSocketsServerContainer.WebSocketsServerRemoteServiceContainerAdapter.WebSocketsServerRemoteServiceRegistration;
import org.eclipse.ecf.provider.websockets.server.WebSocketsServerDistributionProvider;
import org.osgi.service.http.HttpService;

public class JettyServerDistributionProvider extends WebSocketsServerDistributionProvider {
	
	public static final String JETTY_SERVER_CONFIG_NAME = "ecf.websockets.jetty.server";

	public static final String URL_CONTEXT_PARAM = "urlContext";
	public static final String URL_CONTEXT_DEFAULT = System
			.getProperty(JettyServerContainer.class.getName() + ".defaultUrlContext", "http://localhost:8080");
	public static final String ALIAS_PARAM = "alias";
	public static final String ALIAS_PARAM_DEFAULT = "/org.eclipse.ecf.provider.websockets.jetty.server";
	
	public JettyServerDistributionProvider() {
		super();
	}
	
	public void activate() throws Exception {
		setName(JETTY_SERVER_CONFIG_NAME);
		setInstantiator(new WebSocketsContainerInstantiator(JETTY_SERVER_CONFIG_NAME) {
			@Override
			public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters/*, Configuration configuration*/) {
				String urlContext = getParameterValue(parameters, URL_CONTEXT_PARAM, URL_CONTEXT_DEFAULT);
				String alias = getParameterValue(parameters, ALIAS_PARAM, ALIAS_PARAM_DEFAULT);
				return new JettyServerContainer(urlContext, alias/*, (ResourceConfig) ((configuration instanceof ResourceConfig) ? configuration : null)*/);
			}
		});
		setDescription("Jetty WebSockets Server Provider");
		setServer(true);
	}
	
	public class JettyServerContainer extends WebSocketsServerContainer {

//		private ResourceConfig configuration;

		public JettyServerContainer(String urlContext, String alias/*, ResourceConfig configuration*/) {
			super(urlContext, alias);
//			this.configuration = configuration;
		}

		/*protected ResourceConfig createResourceConfig(final RSARemoteServiceRegistration registration) {
			if (this.configuration == null) {
				return ResourceConfig.forApplication(new Application() {*/
					/*@Override
					public Set<Class<?>> getClasses() {
						Set<Class<?>> results = new HashSet<Class<?>>();
						results.add(registration.getService().getClass());
						return results;
					}*/
					
/*					@Override
					public Set<Object> getSingletons() {
						Set<Object> results = new HashSet<Object>();
						results.add(registration.getService());
						return results;
					}
				});
			}
			return this.configuration;
		}*/

		@Override
		protected Servlet createServlet(WebSocketsServerRemoteServiceRegistration registration) {
/*			ResourceConfig rc = createResourceConfig(registration);
			
			Class<?> implClass = registration.getService().getClass();
			for (Class<?> clazz : implClass.getInterfaces()) {
				if(clazz.getAnnotation(Path.class) == null) {
					final Resource.Builder resourceBuilder = Resource.builder();
					ResourceMethod.Builder methodBuilder;
					Resource.Builder childResourceBuilder;
					String serviceResourcePath;
					String methodResourcePath;
					String methodName;
					
					//class
					serviceResourcePath = "/" + clazz.getSimpleName().toLowerCase();
					resourceBuilder.path(serviceResourcePath);
					resourceBuilder.name(implClass.getName());
					
					//methods
					for(Method method : clazz.getMethods()) {
						if(Modifier.isPublic(method.getModifiers())) {
							methodName = method.getName().toLowerCase();
							methodResourcePath = "/" + methodName;
							childResourceBuilder = resourceBuilder.addChildResource(methodResourcePath);
							
							if(method.getAnnotation(Path.class) == null) {
								if(method.getParameterCount() == 0) {
									methodBuilder = childResourceBuilder.addMethod("GET");
								}
								else {
									if(methodName.contains("delete")){
										methodBuilder = childResourceBuilder.addMethod("DELETE");
									}
									else {
										methodBuilder = childResourceBuilder.addMethod("POST");
									}
									methodBuilder.consumes(MediaType.APPLICATION_JSON);//APPLICATION_JSON)TEXT_PLAIN_TYPE
								}
								methodBuilder.produces(MediaType.APPLICATION_JSON)//APPLICATION_JSON)
									//.handledBy(implClass, method)
									.handledBy(registration.getService(), method)
									.handlingMethod(method)
									.extended(false);
							}
						}
					}
					final Resource resource = resourceBuilder.build();
					rc.registerResources(resource);
				}
			}*/
			return null;//(rc != null) ? new ServletContainer(rc) : new ServletContainer();
		}

		@Override
		protected HttpService getHttpService() {
			return getHttpServices().get(0);
		}
	}
}
