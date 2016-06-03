package org.ava.eventhandling;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TTSEventBus {

	private final static Logger logger = LogManager.getLogger(TTSEventBus.class);
	
	/** The singleton instance of the event bus. */
	private static TTSEventBus instance = new TTSEventBus();
	
	/** List of registered SpeakListeners. */
	private List<SpeakListener> sl;
	
	/** 
	 * Private constructor to ensure singleton functionality. 
	 */
	private TTSEventBus() {
		sl = new ArrayList<SpeakListener>();
		logger.debug("TTSEventBus created.");
	}
	
	/**
	 * Returns the singleton instance of the TTSEventBus.
	 * 
	 * @return TTSEventBus The instance of the event bus.
	 */
	public static TTSEventBus getInstance() {
		return instance;
	}
	
	/**
	 * Register a SpeakListener.
	 * 
	 * @param listener The listener to be registered. 
	 * @return boolean True if adding was successful, false if not.
	 */
	public boolean registerSpeakListener(SpeakListener listener) {
		logger.debug("Add SpeakListener. Listener: " + listener.toString());
		boolean success = sl.add(listener);
		logger.debug("Adding SpeakListener: " + success);
		return success;
	}
	
	/**
	 * Remove a SpeakListener.
	 * 
	 * @param listener The listener to be unregistered. 
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean unregisterSpeakListener(SpeakListener listener) {
		logger.debug("Remove SpeakListener. Listener: " + listener.toString());
		boolean success = sl.remove(listener);
		logger.debug("Removing SpeakListener: " + success);
		return success;
	}
	
	/**
	 * Fire a SpeakEvent. Each listener that has been registered on this event
	 * will be notified.
	 */
	public void fireSspeakEvent(SpeakEvent event) {
		for(SpeakListener s : sl) {
			logger.debug("Fire SspeakEvent. Listener: " + s.toString());
			s.speak(event);
		}
	}
}
