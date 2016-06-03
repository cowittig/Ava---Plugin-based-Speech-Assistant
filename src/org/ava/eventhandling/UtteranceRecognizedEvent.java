package org.ava.eventhandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class wraps an utterance that has been recognized by the STT engine.
 * The event is constructed by the STT engine and then passed to the STTEventBus,
 * which handles the event processing.
 * 
 * @author Constantin
 * @since 2016-03-15
 * @version 0.1
 */
public class UtteranceRecognizedEvent {

	private final static Logger logger = LogManager.getLogger(UtteranceRecognizedEvent.class);
	
	/** Utterance that has been recognized by the STT engine. */
	private String utterance;
	
	/**
	 * Create a new UtteranceRecognizedEvent with the given utterance.
	 * 
	 * @param utterance The utterance that has been recognized by the STT engine.
	 */
	public UtteranceRecognizedEvent(String utterance) {
		this.utterance = utterance;
		logger.debug("UtteranceRecognizedEvent created. Utterance: " + this.utterance);
	}
	
	/**
	 * Returns the recognized utterance.
	 * 
	 * @return String The recognized utterance.
	 */
	public String getUtterance() {
		return this.utterance;
	}
}
