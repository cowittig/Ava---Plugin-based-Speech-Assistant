package org.ava.pluginengine;

import java.util.List;

/**
 * This class abstracts an application plugin. Application plugins are
 * resonpsible for implementing and providing commands that manipulate 
 * underlying applications like a calendar or Spotify.
 * 
 * Application plugins have to provide a list of commands that can be
 * recognized by the speech engine and then executed by Ava.
 * 
 * @author Constantin
 * @since 2016-03-20
 * @version 1
 */
public interface AppPlugin extends Plugin {

	/**
	 * Return the list of commands this application plugin provides.
	 * 
	 * @return List<AppCommand> The list of commands.
	 */
	public List<AppCommand> getApplicationCommands();
}
