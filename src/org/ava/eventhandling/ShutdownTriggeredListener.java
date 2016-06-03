package org.ava.eventhandling;

/**
 * This listener will be notified when an application shutdown has been triggered by the user. 
 * 
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public interface ShutdownTriggeredListener {

	/**
	 * Called if the user triggered an application shutdown. The application is
	 * supposed to shutdown in an orderly fashion.
	 */
	public void shutdownApplication();
	
}
