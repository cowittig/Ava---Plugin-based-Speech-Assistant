package org.ava.util;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This utility class provides helper functions that do not really belong to any
 * other class.
 *
 * @author Constantin
 * @since 2016-06-05
 * @version 1
 *
 */
public class AvaUtil {

	private final static Logger log = LogManager.getLogger(AvaUtil.class);

	/**
	 * Play a sound located at a given file path. The sound should be in .wav file format
	 *
	 * The method blocks until playback of the sound has finished.
	 * Call this method in a Thread if you want to keep Ava running.
	 *
	 * I don't even know why it's so complicated to play a simple sound.
	 * Thanks to Stackoverflow for making this happen!
	 *
	 * http://stackoverflow.com/a/26318
	 * http://stackoverflow.com/a/577926
	 */
	public static void playSound(String soundFilePath) {
		try{

			class AudioListener implements LineListener {
				private boolean done = false;

				@Override
				public synchronized void update(LineEvent event) {
					Type eventType = event.getType();
				    if (eventType == Type.STOP || eventType == Type.CLOSE) {
				    	done = true;
				        notifyAll();
				      }
				    }

				public synchronized void waitUntilDone() throws InterruptedException {
				      while (!done) { wait(); }
				}
			};

			AudioListener listener = new AudioListener();
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(
	        		Files.newInputStream(Paths.get(soundFilePath))));
	        DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
	        Clip clip = (Clip) AudioSystem.getLine(info);
	        clip.open(inputStream);
	        clip.addLineListener(listener);
	        try{
	        	log.debug("Play sound: " + soundFilePath);
	        	clip.start();
	        	listener.waitUntilDone();
	        	log.debug("Play  sound done.");
	        } finally {
	        	clip.close();
	        	inputStream.close();
	        }
		} catch(Exception ex) {
			log.catching(Level.DEBUG, ex);
		}
	}

}
