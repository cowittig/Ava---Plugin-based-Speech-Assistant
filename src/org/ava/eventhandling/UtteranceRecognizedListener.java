package org.ava.eventhandling;

/**
 * This listener will be notified when an utterance has been recognized by the
 * STT engine. 
 * 
 * @author Constantin
 * @since 2016-03-15
 * @version 0.1
 */
public interface UtteranceRecognizedListener {

	/**
	 * Called if an utterance has been recognized by the STT engine and an event 
	 * has been fired by the STTEventBus.
	 * 
	 * @param event The UtteranceRecognizedEvent fired by the STTEventBus.
	 */
	public void processRecognizedUtterance(UtteranceRecognizedEvent event);

}
