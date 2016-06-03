package org.ava.pluginengine; 

/**
 * Enum encoding the running state of a plugin
 * 
 * Possible values:
 * 		-- STOPPED
 * 		-- RUNNING
 * 		-- INTERRUPTED
 * 
 * @author Constantin
 * @since 2016-03-26
 * @version 1  
 */
public enum PluginState {
	STOPPED,
	INTERRUPTED, 
	RUNNING
}
