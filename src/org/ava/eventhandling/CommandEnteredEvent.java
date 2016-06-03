package org.ava.eventhandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class wraps an command that has been entered by the user via the user interface.
 * The event is constructed by the UI and then passed to the UIEventBus,
 * which handles the event processing.
 * 
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public class CommandEnteredEvent {

private final static Logger logger = LogManager.getLogger(UtteranceRecognizedEvent.class);
	
	/** Command that has been entered by the user. */
	private String command;
	
	/**
	 * Create a new CommandEnteredEvent with the given command.
	 * 
	 * @param command The command that has been entered by the user.
	 */
	public CommandEnteredEvent(String command) {
		this.command = command;
		logger.debug("CommandEnteredEvent created. Entered command: " + this.command);
	}
	
	/**
	 * Return the command entered by the user
	 * 
	 * @return String The command entered by the user.
	 */
	public String getCommand() {
		return this.command;
	}

}
