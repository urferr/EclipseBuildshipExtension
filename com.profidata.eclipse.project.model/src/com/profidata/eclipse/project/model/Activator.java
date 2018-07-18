package com.profidata.eclipse.project.model;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class Activator extends Plugin {

    private static BundleContext context;
    private static Plugin instance;


    public Activator() {
        instance = this;
    }

    public static BundleContext getContext() {
        return context;
    }

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

    public static void info(String theMessage) {
        instance.getLog().log(new Status(Status.INFO, instance.getBundle().getSymbolicName(), theMessage, null));
    }


    public static void error(String theMessage) {
        error(theMessage, null);
    }

    public static void error(String theMessage, Throwable theException) {
        instance.getLog().log(new Status(Status.ERROR, instance.getBundle().getSymbolicName(), theMessage, theException));
    }
}
