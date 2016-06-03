package org.ava.eventhandling;

/**
 * This listener will be notified when the user entered a command in text form
 * via console or gui.
 * 
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public interface CommandEnteredListener {

	/**
	 * Called if an CommandEnteredEvent has been fired via the UIEventBus. Such an event
	 * will be fired, if the user has entered an command via console or gui.
	 * 
	 * @param event The CommandEnteredEvent that has been triggered, containing the command.
	 */
	public void executeEnteredCommand(CommandEnteredEvent event);

}
