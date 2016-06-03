package org.ava.pluginengine;

/**
 * This interface abstracts a TTS engine. The TTS system will 
 * be used by Ava as plugin.
 * 
 * The TTS system is responsible for synthesizing text and out-
 * putting it.
 * 
 * @author Constantin
 * @since 2016-03-20
 * @version 0.1
 */
public interface TTSPlugin extends Plugin{

	/**
	 * Synthesize the given text and play the result.
	 * 
	 * @param msg The text to synthesize.
	 */
	public void sayText(String msg);
}
