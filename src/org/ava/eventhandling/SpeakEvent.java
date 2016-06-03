package org.ava.eventhandling;

public class SpeakEvent {

	private String textToSay;

	public SpeakEvent(String textToSay) {
		this.textToSay = textToSay;
	}
	
	public String getTextToSay() {
		return this.textToSay;
	}
}
