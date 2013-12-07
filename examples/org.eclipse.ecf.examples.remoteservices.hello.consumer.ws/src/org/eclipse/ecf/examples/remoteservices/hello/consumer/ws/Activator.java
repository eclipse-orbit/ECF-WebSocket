package org.eclipse.ecf.examples.remoteservices.hello.consumer.ws;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.websocket.WebSocketClientSOContainer;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisteredEvent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private static BundleContext context;
	private ServiceTracker serviceTracker;
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		ISharedObjectContainerConfig config = new SOContainerConfig(IDFactory.getDefault().createGUID());
	    WebSocketClientSOContainer container = new WebSocketClientSOContainer(config); 
	    ID serverID = IDFactory.getDefault().createStringID("ws://localhost:8081/");
	
	 	// Add remote service listener
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container
					.getAdapter(IRemoteServiceContainerAdapter.class);
		adapter.addRemoteServiceListener(new RemoteServiceListener(adapter));

	    container.connect(serverID, null);
	    
	    System.out.println("Connected!");

		serviceTracker = new ServiceTracker(bundleContext, createRemoteFilter(), this);
		serviceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	
	private Filter createRemoteFilter() throws InvalidSyntaxException {
		// This filter looks for ObjectRegistry instances that have the
		// 'service.imported' property set, as specified by OSGi 4.2
		// remote services spec (Chapter 13)
		return context.createFilter("(&("
				+ org.osgi.framework.Constants.OBJECTCLASS + "="
				+ IHello.class.getName() + ")(service.imported=*))");

	}
	
	private class RemoteServiceListener implements IRemoteServiceListener {
		Map<IRemoteServiceReference, ServiceRegistration> refmap;
		IRemoteServiceContainerAdapter adapter;

		public RemoteServiceListener(IRemoteServiceContainerAdapter adapter) {
			this.adapter = adapter;
			refmap = new HashMap<IRemoteServiceReference, ServiceRegistration>();
		}

		@Override
		public synchronized void handleServiceEvent(IRemoteServiceEvent event) {

			IRemoteServiceReference ref = event.getReference();

			if (event instanceof IRemoteServiceRegisteredEvent) {
				System.out.println("IRemoteServiceRegisteredEvent: "
						+ event.getReference());

				Dictionary<String, Object> props = new java.util.Hashtable<String, Object>();
				for (String key : ref.getPropertyKeys()) {
					props.put(key, ref.getProperty(key));
				}
				props.put(RemoteConstants.ENDPOINT_SERVICE_ID,
						ref.getProperty("ecf.rsvc.id"));
				props.put(RemoteConstants.ENDPOINT_ID, event.getContainerID()
						.getName());
				props.put("objectClass", ref.getProperty("ecf.robjectClass"));
				props.put("service.imported", "*");
				
				IRemoteService rs = adapter.getRemoteService(ref);

				ServiceRegistration reg = null;
				try {
					reg = Activator.getContext().registerService(
							(String[]) ref.getProperty("ecf.robjectClass"),
							rs.getProxy(), props);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}

				refmap.put(ref, reg);
				
			} else {
				System.out.println("IRemoteServiceUnregisteredEvent: "
						+ event.getReference());
				ServiceRegistration reg = refmap.get(ref);
				if (reg != null)
					reg.unregister();
				refmap.remove(ref);
			}

		}
	}


	@Override
	public Object addingService(ServiceReference reference) {
		System.out.println("addingService()");

		IHello hello = (IHello) context.getService(reference);

		System.out.println("calling...");
		String result = hello.hello("Hello websocket!");
		System.out.println("hello.hello() result : " + result);
		
		
		return hello;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub
		
	}

}
