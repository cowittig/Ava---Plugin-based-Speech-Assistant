package org.ava.pluginengine; 

/**
 * Specialized interface for Speech-To-Text Plugins extends from IPlugin. 
 * STT Plugins have to implement this interface to can be use as Plugin. 
 * 
 * @author Kevin
 * @version 2.0
 * 
 * Changelog:
 * 2016-03-19 Constantin v2.0
 * 		-- Renamed interface to STTPlugin to conform to naming conventions.
 * 		-- Added method 'requestText()'. 
 */
public interface STTPlugin extends Plugin{

	/**
	 * Upon calling this method, the STTPlugin is supposed to listen and
	 * return the next recognized utterance. 
	 * 
	 * The method may be called by an application plugin that needs more input 
	 * from the user, i.e. in a dialog-like situation.
	 * 
	 * @return String The recognized text.
	 */
	public String requestText();
}
