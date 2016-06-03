package org.ava.eventhandling;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This event bus handles events fired by the STT engine. Listeners can be registered
 * and unregistered on specific events. Each registered listener will be fired when a specific
 * event has been fired. 
 * 
 * Supported events:
 * 		-- UtteranceRecognizedEvent: Fired if STT engine recognized an event.
 * 
 * @author Constantin
 * @since 2016-03-15
 * @version 0.1
 */
public class STTEventBus {

	private final static Logger logger = LogManager.getLogger(STTEventBus.class);
	
	/** The singleton instance of the event bus. */
	private static STTEventBus instance = new STTEventBus();
	
	/** List of registered UtteranceRecognizedListeners. */
	private List<UtteranceRecognizedListener> urell;
	
	/** List of registered UtteranceRequestedListeners. */
	private List<UtteranceRequestedListener> ureqll;
	
	/** 
	 * Private constructor to ensure singleton functionality. 
	 */
	private STTEventBus() {
		logger.debug("STTEventBus created.");
		urell = new ArrayList<UtteranceRecognizedListener>();
		ureqll = new ArrayList<UtteranceRequestedListener>();
	}
	
	/**
	 * Returns the singleton instance of the STTEventBus.
	 * 
	 * @return STTEventBus The instance of the event bus.
	 */
	public static STTEventBus getInstance() {
		return instance;
	}
	
	/**
	 * Register an UtteranceRecognizedListener.
	 * 
	 * @param listener The listener to be registered. 
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerUtteranceRecognizedListener(UtteranceRecognizedListener listener) {
		logger.debug("Add UtteranceRecognizedListener. Listener: " + listener.toString());
		boolean success = urell.add(listener);
		logger.debug("Adding UtteranceRecognizedListener: " + success);
		return success;
	}
	
	/**
	 * Remove an UtteranceRecognizedListener.
	 * 
	 * @param listener The listener to be removed. 
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterUtteranceRecognizedListener(UtteranceRecognizedListener listener) {
		logger.debug("Remove UtteranceRecognizedListener. Listener: " + listener.toString());
		boolean success = urell.remove(listener);
		logger.debug("Removing UtteranceRecognizedListener: " + success);
		return success;
	}
	
	/**
	 * Fire an UtteranceRecognizedEvent. Each listener that has been registered on this event
	 * will be notified.
	 * 
	 * @param event The UtteranceRecognizedEvent that wraps the recognized utterance and that has been 
	 * 					fired by the STT engine.
	 */
	public void fireUtteranceRecognizedEvent(UtteranceRecognizedEvent event) {
		for(UtteranceRecognizedListener urel : urell) {
			logger.debug("Fire UtteranceRecognizedEvent. Event: " + event.toString() 
					+ ", listener: " + urel.toString());
			urel.processRecognizedUtterance(event);
		}
	}
	
	/**
	 * Register an UtteranceRequestedListener.
	 * 
	 * @param listener The listener to be registered. 
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerUtteranceRequestedListener(UtteranceRequestedListener listener) {
		logger.debug("Add UtteranceRequestedListener. Listener: " + listener.toString());
		boolean success = ureqll.add(listener);
		logger.debug("Adding UtteranceRequestedListener: " + success);
		return success;
	}
	
	/**
	 * Remove an UtteranceRequestedListener.
	 * 
	 * @param listener The listener to be removed. 
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterUtteranceRequestedListener(UtteranceRequestedListener listener) {
		logger.debug("Remove UtteranceRequestedListener. Listener: " + listener.toString());
		boolean success = ureqll.remove(listener);
		logger.debug("Removing UtteranceRequestedListener: " + success);
		return success;
	}
	
	/**
	 * Fire an UtteranceRequestedEvent. Each listener that has been registered on this event
	 * will be notified.
	 */
	public String fireUtteranceRequestedEvent() {
		String res = null;
		for(UtteranceRequestedListener urel : ureqll) {
			logger.debug("Fire UtteranceRequestedEvent. Listener: " + urel.toString());
			res = urel.requestUtterance();
		}
		return res;
	}
}
