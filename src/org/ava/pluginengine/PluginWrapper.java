package org.ava.pluginengine;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Constantin
 * @since 2016-03-20
 * @version 3
 *
 * Changelog:
 * 2016-03-21 Constantin v2
 * 		-- Added PluginState and PluginActivationState fields.
 * 2016-03-26 Constantin v3
 * 		-- Added support for PluginType.
 * 		-- Initialize enum values in constructor.
 */
public class PluginWrapper {

	private final static Logger log = LogManager.getLogger(PluginWrapper.class);

	/** An instance of the wrapped plugin. */
	private Plugin instance;

	private boolean isInstantiated;

	/** The properties of the wrapped plugin. */
	private PluginProperties properties;

	/** The path to the jar file containing the plugin. */
	private Path jarFilePath;

	/** The activation state of the plugin. */
	private PluginActivationState pluginActivationState;

	/** The running state of the plugin. */
	private PluginState pluginState;

	/** The type of the plugin (STT, TTS, Application). */
	private PluginType pluginType;

	/**
	 * Create PluginWrapper with given values.
	 *
	 * @param jarFilepath
	 * @param properties
	 */
	public PluginWrapper(Path jarFilepath, PluginProperties properties) {
		// make sure plugin instance is empty
		this.instance = null;
		this.isInstantiated = false;
		this.properties = properties;
		this.jarFilePath = jarFilepath;
		this.pluginActivationState = PluginActivationState.DEACTIVATED;
		this.pluginState = PluginState.STOPPED;
		this.pluginType = PluginType.TYPE_NOT_SPECIFIED;

		log.debug("PluginWrapper for plugin '" + properties.getName() + "' created, with properties "
				+ properties.toString() + ". Instance: " + this.toString());
	}

	/**
	 * Instantiate the wrapped plugin.
	 *
	 * @return Plugin The instance if creation was successful,
	 * 					null if not.
	 */
	public Plugin getPluginInstance(){
		if(instance == null) {
			URL[] urlArr = new URL[1];
			try {
				urlArr[0] = jarFilePath.toUri().toURL();
			} catch (MalformedURLException e){
				log.catching(Level.DEBUG, e);
				instance = null;
			}

			ClassLoader pluginClassLoader = new URLClassLoader(urlArr, this.getClass().getClassLoader());
			
			// dirty fix for MaryTTS Plugin
			// MaryTTS code relies on the System Class Loader to locate some classes. Therefore when we try to
			// load it with the URLClassLoader MayTTS cannot locate its classes.
			// The issue has been already reported to the MaryTTS team by other people in May 2016
			// https://github.com/marytts/marytts/issues/524
			if( properties.getName().equals("MaryTTS") ) {
				Thread.currentThread().setContextClassLoader(pluginClassLoader);
			}
			
			try {
				//instance = (Plugin) pluginClassLoader.loadClass(properties.getFqnPluginClass()).newInstance();
				instance = (Plugin) Class.forName(properties.getFqnPluginClass(), true, pluginClassLoader).newInstance();
				if( instance instanceof STTPlugin ) {
					pluginType = PluginType.STT_PLUGIN;
				} else if( instance instanceof TTSPlugin ) {
					pluginType = PluginType.TTS_PLUGIN;
				} else if( instance instanceof AppPlugin ) {
					pluginType = PluginType.APPLICATION_PLUGIN;
				}
				isInstantiated = true;
				log.debug("Instance for plugin '" + properties.getName() + "' created.");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				log.catching(Level.DEBUG, e);
				instance = null;
				isInstantiated = false;
			} finally {
				/*try {
					//pluginClassLoader.close();
				} catch (IOException e) {
					log.catching(Level.DEBUG, e);
				}*/
			}

		}

		return instance;
	}

	/**
	 * Destroy the loaded instance of the plugin.
	 */
	public void destroyPluginInstance() {
		this.instance = null;
		this.isInstantiated = false;
	}

	/**
	 * Return the properties of the wrapped plugin.
	 *
	 * @return PluginProperties The properties of the wrapped plugin.
	 */
	public PluginProperties getProperties() {
		return properties;
	}

	/**
	 * Returns the file path to the jar file of the wrapped plugin.
	 *
	 * @return Path The path to the jar file of the plugin.
	 */
	public Path getJarFilePath() {
		return jarFilePath;
	}

	/**
	 * Returns the activation state of the plugin.
	 *
	 * @return The activation state of the plugin.
	 */
	public PluginActivationState getPluginActivationState() {
		return pluginActivationState;
	}

	/**
	 * Set a new activation state.
	 *
	 * @param pluginActivationState The new activation state of the plugin.
	 */
	public void setPluginActivationState(PluginActivationState pluginActivationState) {
		this.pluginActivationState = pluginActivationState;
	}

	/**
	 * Returns the running state of the plugin.
	 *
	 * @return The running state of the plugin.
	 */
	public PluginState getPluginState() {
		return pluginState;
	}

	/**
	 * Set a new running state.
	 *
	 * @param pluginState The new running state of the plugin.
	 */
	public void setPluginState(PluginState pluginState) {
		this.pluginState = pluginState;
	}

	/**
	 * Returns the type of the plugin.
	 *
	 * @return PluginType The type of the plugin.
	 */
	public PluginType getPluginType() {
		return pluginType;
	}

	/**
	 * Check whether the plugin has already been instantiated.
	 *
	 * @return boolean True if the plugin has already been instantiated, false if not.
	 */
	public boolean isAlreadyInstantiated() {
		return this.isInstantiated;
	}
}
