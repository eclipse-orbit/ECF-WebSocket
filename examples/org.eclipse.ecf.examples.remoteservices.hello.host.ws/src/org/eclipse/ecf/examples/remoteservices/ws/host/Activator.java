package org.eclipse.ecf.examples.remoteservices.ws.host;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.examples.remoteservices.hello.impl.Hello;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		Dictionary props = new Properties();
        props.put("service.exported.interfaces", "*");
        props.put("service.exported.configs", "ecf.generic.ws.server");
        
        context.registerService(IHello.class.getName(),
        		new Hello(),
        		props);
		
		// Starting embedded jetty and set our web socket handler
		new Thread(new Runnable() {
			public void run() {
				try {
		            Server server = new Server(8081);
		            MyWebSocketHandler h = new MyWebSocketHandler();
		            h.setHandler(new DefaultHandler());
		            server.setHandler(h);
		            server.start();
		            server.join();
				} catch (Throwable e) {
		            e.printStackTrace();
		        }
			}
		}).start();
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
