package org.ava.pluginengine;

/**
 * Enum encoding the type of a plugin. 
 * 
 * Possible values:
 * 		-- STT_Plugin
 * 		-- TTS_Plugin
 * 		-- APPLICATION_Plugin
 * 		-- TYPE_NOT_SPECIFIED (indicates that the
 * 			PluginManager has not yet determinde the 
 * 			type of the plugin)
 * 
 * @author Constantin
 * @since 2016-03-26
 * @version 1
 */
public enum PluginType {
	STT_PLUGIN,
	TTS_PLUGIN,
	APPLICATION_PLUGIN,
	TYPE_NOT_SPECIFIED
}
