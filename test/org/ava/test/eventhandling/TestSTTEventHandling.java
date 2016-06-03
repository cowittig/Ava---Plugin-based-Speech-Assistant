package org.ava.test.eventhandling;

import org.ava.eventhandling.STTEventBus;
import org.ava.eventhandling.UtteranceRecognizedEvent;
import org.ava.eventhandling.UtteranceRecognizedListener;

public class TestSTTEventHandling {

	public static void main(String[] args) {
		STTEventBus seb = STTEventBus.getInstance();
		
		UtteranceRecognizedEvent ure = new UtteranceRecognizedEvent("Hello World.");
		
		UtteranceRecognizedListener ucl = new UtteranceRecognizedListener() {
			@Override
			public void processRecognizedUtterance(UtteranceRecognizedEvent event) {
				System.out.println("Utterance '" + event.getUtterance() + "' processed.");
			}};
			
		seb.registerUtteranceRecognizedListener(ucl);
		seb.fireUtteranceRecognizedEvent(ure);
		seb.unregisterUtteranceRecognizedListener(ucl);
	}

}
