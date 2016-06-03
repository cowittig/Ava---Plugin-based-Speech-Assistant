package org.ava.pluginengine;

/**
 * This class provides an abstraction for application commands.
 * Applications plugins are supposed to implement this interface and
 * provide commands the Ava core can match to recognized input.
 * Subsequently the commands will be executed.
 *
 * @author Constantin
 * @since 2016-03-20
 * @version 2
 */
public interface AppCommand {

	public void execute(String arg);

	public String getCommand();

}
