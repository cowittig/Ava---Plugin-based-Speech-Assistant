package org.ava.eventhandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.PluginActivationState;

/**
 * This class contains information about a plugin that has changed its activation state. The event
 * is triggered by console or gui, if the user activates or deactivates a plugin. 
 * The event will be passed to the UIEventBus, which handles event processing.
 * 
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public class PluginActivationStateChangedEvent {
	
	private final static Logger logger = LogManager.getLogger(UtteranceRecognizedEvent.class);
	
	/** The new activation state of the plugin. */
	private PluginActivationState newState;

	/** The ID of the plugin that has changed its activation state. */
	private String pluginID;
	
	/**
	 * Create a new PluginActivationStateChangedEvent.
	 * 
	 * @param pluginID The pluginID of the plugin.
	 * @param newState The new activation state of the plugin.
	 */
	public PluginActivationStateChangedEvent(String pluginID, PluginActivationState newState) {
		this.pluginID = pluginID;
		this.newState = newState;
		logger.debug("PluginActivationStateChangedEvent created. PluginID: " + pluginID + ", new state: " + this.newState);
	}
	
	/**
	 * Returns the new activation state of the plugin.
	 * 
	 * @return PluginActivationState The new activation state of the plugin.
	 */
	public PluginActivationState getNewPluginActivationState() {
		return this.newState;
	}
	
	/**
	 * Returns the ID of the plugin that has changed its activation state.
	 * 
	 * @return String The ID of the plugin.
	 */
	public String getPluginID() {
		return this.pluginID;
	}

}
