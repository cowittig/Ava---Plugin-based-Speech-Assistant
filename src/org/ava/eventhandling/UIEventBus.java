package org.ava.eventhandling;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This event bus handles events fired by the UI (console and graphical. Listeners can be registered
 * and unregistered on specific events. Each registered listener will be fired when a specific
 * event has been fired.
 *
 * Supported events:
 * 		-- ShutdownTriggeredEvent: Fired if the user triggered an application shutdown.
 * 		-- PluginActivationStateChangedEvent: Fired if a specific plugin should be activated or deactivated.
 * 		-- CommandEnteredEvent: Fired if the user entered a command via console or gui.
 *
 * @author Constantin
 * @since 2016-03-16
 * @version 0.1
 */
public class UIEventBus {

	private final static Logger logger = LogManager.getLogger(UIEventBus.class);

	/** The singleton instance of the event bus. */
	private static UIEventBus instance = new UIEventBus();

	/** List of registered ShutdownTriggeredListeners. */
	private List<ShutdownTriggeredListener> stll;

	/** List of registered PluginActivationStateChangedListeners. */
	private List<PluginActivationStateChangedListener> pascll;

	/** List of registered CommandEnteredListeners. */
	private List<CommandEnteredListener> cell;

	/**
	 * Private constructor to ensure singleton functionality.
	 */
	private UIEventBus() {
		stll = new ArrayList<ShutdownTriggeredListener>();
		pascll = new ArrayList<PluginActivationStateChangedListener>();
		cell = new ArrayList<CommandEnteredListener>();

		logger.debug("UIEventBus created.");
	}

	/**
	 * Returns the singleton instance of the UIEventBus.
	 *
	 * @return UIEventBus The instance of the event bus.
	 */
	public static UIEventBus getInstance() {
		return instance;
	}

	/**
	 * #########################################################
	 * ################# Register listeneres ###################
	 * #########################################################
	 */

	/**
	 * Register an ShutdownTriggeredListener.
	 *
	 * @param listener The listener to be registered.
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerShutdownTriggeredListener(ShutdownTriggeredListener listener) {
		logger.debug("Add ShutdownTriggeredListener. Listener: " + listener.toString());
		boolean success = stll.add(listener);
		logger.debug("Adding ShutdownTriggeredListener: " + success);
		return success;
	}

	/**
	 * Register an PluginActivationStateChangedListener.
	 *
	 * @param listener The listener to be registered.
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerPluginActivationStateChangedListener(PluginActivationStateChangedListener listener) {
		logger.debug("Add PluginActivationStateChangedListener. Listener: " + listener.toString());
		boolean success = pascll.add(listener);
		logger.debug("Adding PluginActivationStateChangedListener: " + success);
		return success;
	}

	/**
	 * Register an CommandEnteredListener.
	 *
	 * @param listener The listener to be registered.
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerCommandEnteredListener(CommandEnteredListener listener) {
		logger.debug("Add CommandEnteredListener. Listener: " + listener.toString());
		boolean success = cell.add(listener);
		logger.debug("Adding CommandEnteredListener: " + success);
		return success;
	}

	/**
	 * #########################################################
	 * ################# Unregister listeneres #################
	 * #########################################################
	 */

	/**
	 * Remove an ShutdownTriggeredListener.
	 *
	 * @param listener The listener to be removed.
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterShutdownTriggeredListener(ShutdownTriggeredListener listener) {
		logger.debug("Remove ShutdownTriggeredListener. Listener: " + listener.toString());
		boolean success = stll.remove(listener);
		logger.debug("Removing ShutdownTriggeredListener: " + success);
		return success;
	}

	/**
	 * Remove an PluginActivationStateChangedListener.
	 *
	 * @param listener The listener to be removed.
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterPluginActivationStateChangedListener(PluginActivationStateChangedListener listener) {
		logger.debug("Remove PluginActivationStateChangedListener. Listener: " + listener.toString());
		boolean success = pascll.remove(listener);
		logger.debug("Removing PluginActivationStateChangedListener: " + success);
		return success;
	}

	/**
	 * Remove an CommandEnteredListener.
	 *
	 * @param listener The listener to be removed.
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterCommandEnteredListener(CommandEnteredListener listener) {
		logger.debug("Remove CommandEnteredListener. Listener: " + listener.toString());
		boolean success = cell.remove(listener);
		logger.debug("Removing CommandEnteredListener: " + success);
		return success;
	}

	/**
	 * #########################################################
	 * ################# fireEvents ############################
	 * #########################################################
	 */

	/**
	 * Fire an ShutdownTriggeredEvent. Each listener that has been registered on this event
	 * will be notified.
	 */
	public void fireShutdownTriggeredEvent() {
		for(ShutdownTriggeredListener stl : stll) {
			logger.debug("Fire ShutdownTriggeredEvent. Listener: " + stl.toString());
			stl.shutdownApplication();
		}
	}

	/**
	 * Fire an PluginActivationStateChangedEvent indicating that the user wants to activate or deactivate a plugin.
	 * Each listener that has been registered on this event will be notified.
	 *
	 * @param event The PluginActivationStateChangedEvent that indicates which plugin changed its state.
	 */
	public void firePluginActiavtionStateChangedEvent(PluginActivationStateChangedEvent event) {
		for(PluginActivationStateChangedListener pascl : pascll) {
			logger.debug("Fire ActivationStateChangedEvent. Event: " + event + ", Listener: " + pascl.toString());
			pascl.changeActivationStateOfPlugin(event);
		}
	}

	/**
	 * Fire an CommandEnteredEvent. Each listener that has been registered on this event
	 * will be notified.
	 *
	 * @param event The CommandEnteredEvent that wraps the command entered by the user via console or gui.
	 */
	public void fireCommandEnteredEvent(CommandEnteredEvent event) {
		for(CommandEnteredListener cel : cell) {
			logger.debug("Fire CommandEnteredEvent. Event: " + event + ", Listener: " + cel.toString());
			cel.executeEnteredCommand(event);
		}
	}

}
