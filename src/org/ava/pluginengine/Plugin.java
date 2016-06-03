package org.ava.pluginengine; 

/**
 * Interface for Plugins. 
 * 
 * @author Kevin
 * @version 2.0
 * 
 * Changelog:
 * 2016-03-19 Constantin v2.0
 * 		-- Renamed interface to Plugin to conform to naming conventions.
 * 		-- Removed getter for PluginState, name and version. They will be moved
 * 		   to class PluginWrapper.
 * 		-- Renamed continue and interrupt method, because continue is a reserved keyword.
 */
public interface Plugin {

	/**
	 * This method will be called when the plugin is loaded by Ava. 
	 * Any pre-execution configuration can be done here.
	 * 
	 * Keep this method as short as possible.
	 */
	public void start(); 
	
	/**
	 * This method will be called when the plugin is unloaded by Ava.
	 * Any post-execution action can be done here, to ensure a smooth
	 * shutdown of the plugin.
	 */
	public void stop();
	
	/**
	 * Try to continue everything that has been stopped by the interruptExecution() method.
	 */
	public void continueExecution(); 
	
	/**
	 * Interrupt any action the plugin is doing, i.e. listening to input, playing music.
	 * If possible, it should be able to continue the interrupted action 
	 * via the continueExectuion() method.
	 */
	public void interruptExecution(); 

}
