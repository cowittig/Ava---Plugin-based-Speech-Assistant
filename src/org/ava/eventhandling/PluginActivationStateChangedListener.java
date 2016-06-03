package org.ava.eventhandling;

/**
 * This listener will be notified when the user activated or deactivated a plugin.
 * 
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public interface PluginActivationStateChangedListener {

	/**
	 * Called if the user activated or deactivated a plugin via console or gui.
	 * 
	 * @param event PluginActivationStateChangedEvent containing a plugin id and the new state of the plugin.
	 */
	public void changeActivationStateOfPlugin(PluginActivationStateChangedEvent event);

}
