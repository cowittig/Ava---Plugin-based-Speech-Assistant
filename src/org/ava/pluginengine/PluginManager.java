package org.ava.pluginengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.util.ApplicationConfig;
import org.ava.util.JarPropertyFileSearcher;
import org.ava.util.JarPropertyFileSearcher.JarPropertyFilepathPair;
import org.ava.util.PropertiesFileLoader;

/**
 *
 * @author Constantin
 * @since 2016-03-20
 * @version 1
 */
public class PluginManager {

	private final static String LOADED_PLUGINS_PROPS_PREFIX = "plugins.lastsessionloaded.";

	private final static Logger log = LogManager.getLogger(PluginManager.class);

	/** A Map to provide easy retrieval of plugins. */
	private Map<Integer, PluginWrapper> pluginList;

	private List<String> pluginsLoadedLastSession;

	private List<Plugin> loadedPlugins;

	/** Current highest plugin ID. */
	private int counterPluginID;

	public PluginManager() {
		pluginList = new HashMap<Integer, PluginWrapper>();
		pluginsLoadedLastSession = new ArrayList<String>();
		loadedPlugins = new ArrayList<Plugin>();
		counterPluginID = 0;

		log.debug("Created PluginManager.");
	}

	public void initialize() {
		log.debug("Initializing PluginManager.");
		loadPluginsFromLastSession();
		discoverPlugins();
		cleanPropertyFile();
		startAllActivatedPlugins();
		log.debug("PluginManager initialized.");
	}

	public void shutdown() {
		log.debug("PluginManager shutdown triggered.");
		for(PluginWrapper pw : pluginList.values()) {
			if( pw.getPluginState() == PluginState.RUNNING) {
				stopPlugin(pw);
			}
		}
		log.debug("PluginManager shut down.");
	}

	private void startAllActivatedPlugins() {
		for(PluginWrapper pw : pluginList.values()) {
			if( pw.getPluginActivationState() == PluginActivationState.ACTIVATED ) {
				startPlugin(pw);
			}
		}
	}

	/**
	 * Loads a plugin by creating a plugin instace via the PluginWrapper.
	 * The start() method of the plugin is called.
	 *
	 * @param pluginID The plugin to load.
	 * @return Plugin An instance of the loaded plugin. Null if start() failed.
	 */
	public Plugin startPlugin(int pluginID) {
		return startPlugin(pluginList.get(pluginID));
	}

	private Plugin startPlugin(PluginWrapper pluginWrapper) {
		log.debug("Trying to start plugin: '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		Plugin p = pluginWrapper.getPluginInstance();
		try {
			p.start();
			pluginWrapper.setPluginState(PluginState.RUNNING);
			loadedPlugins.add(p);
			log.debug("Successfully started plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		} catch(Exception e) {
			log.error("Failed to start plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
			log.catching(Level.DEBUG, e);
			return null;
		}
		return p;
	}

	/**
	 * Stops a plugin by destroying the plugin instance via the PluginWrapper.
	 * The stop() method of the plugin is called.
	 *
	 * @param pluginID The plugin to unload.
	 */
	public void stopPlugin(int pluginID) {
		stopPlugin(pluginList.get(pluginID));
	}

	public void stopPlugin(Plugin pluginInstance) {
		PluginWrapper pw;
		if( (pw = findWrapperFromPluginInstance(pluginInstance)) != null ) {
			stopPlugin(pluginList.get(pw));
		}
	}

	private void stopPlugin(PluginWrapper pluginWrapper) {
		log.debug("Trying to stop plugin: '" + pluginWrapper.getProperties().getName()
				+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		try {
			loadedPlugins.remove(pluginWrapper.getPluginInstance());
			pluginWrapper.getPluginInstance().stop();
			pluginWrapper.destroyPluginInstance();
			pluginWrapper.setPluginState(PluginState.STOPPED);
			log.debug("Successfully stopped plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		} catch(Exception e) {
			log.error("Failed to stop plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
			log.catching(Level.DEBUG, e);
			return;
		}
	}

	private void loadPluginsFromLastSession() {
		log.debug("Load plugins that had activation status 'ACTIVATED' last session.");
		PropertiesFileLoader ppl = new PropertiesFileLoader(
				Paths.get(ApplicationConfig.getConfigDir() + ApplicationConfig.getConfigName()));
		if(!ppl.readPropertiesFile()) {
			log.error("Could not read properties, abort loading of plugins from last session.");
		} else {
			for(Object key : ppl.getPropertiesObject().keySet() ) {
				String keyString = (String) key;
				if( ((String) key).startsWith(LOADED_PLUGINS_PROPS_PREFIX) ) {
					String pluginPath = ppl.getPropertie(keyString);
					pluginsLoadedLastSession.add(pluginPath);
					log.debug("Plugin from last session found: " + pluginPath);
				}
			}
		}

	}

	public void discoverPlugins() {
		log.debug("Search plugin directory '" + ApplicationConfig.getPluginDir() + "' for plugins.");

		// search plugin directory for jar files with corresponding property files
		Path pluginDir = Paths.get(ApplicationConfig.getPluginDir());
		JarPropertyFileSearcher fileSearcher = new JarPropertyFileSearcher();
		try {
			Files.walkFileTree(pluginDir, fileSearcher);
		} catch (IOException e) {
			log.catching(Level.DEBUG, e);
		}
		List<JarPropertyFilepathPair> filePairs = fileSearcher.getJarPropertyFilepathPairs();

		if(filePairs.isEmpty()) {
			log.info("No plugins in directory '" + ApplicationConfig.getPluginDir() + "' found.");
			return;
		} else {
			log.info("Discoverd " + filePairs.size() + " plugins.");
		}

		// for each discoverd plugin, build PluginWrapper and PluginProperties
		for(JarPropertyFilepathPair singleFilePair : filePairs) {
			int pluginID = generateIntegerID();
			PluginProperties pluginProps = buildPluginProperties(singleFilePair.getPropertyFilepath(), pluginID);
			PluginWrapper pluginWrapper = buildPluginWrapper(singleFilePair.getJarFilepath(), pluginProps);

			// filter duplicate plugin by comparing the property files
			if(!isPluginAlreadyDiscovered(pluginProps)) {
				pluginList.put(pluginID, pluginWrapper);
			}

			// check if the discovered plugin has been in 'ACTIVATED' state in last session
			Iterator<String> iter = pluginsLoadedLastSession.iterator();
			while(iter.hasNext()) {
				String path = iter.next();
				Path pluginFromLastSessionPath = Paths.get(path).toAbsolutePath();
				Path discoverdPluginPath = pluginWrapper.getJarFilePath().toAbsolutePath();
				if( pluginFromLastSessionPath.equals(discoverdPluginPath)) {
					pluginWrapper.setPluginActivationState(PluginActivationState.ACTIVATED);
					iter.remove();
				}
			}

			log.debug("Plugin '" + pluginProps.getName() + "' (ID: " + pluginProps.getID() + ") discovered.");
		}

	}

	private PluginWrapper buildPluginWrapper(Path jarFilepath, PluginProperties pluginProps) {
		return new PluginWrapper(jarFilepath, pluginProps);
	}

	private PluginWrapper findWrapperFromPluginInstance(Plugin pluginInstance) {
		for(PluginWrapper pw : pluginList.values()) {
			if( pw.isAlreadyInstantiated() && (pluginInstance == pw.getPluginInstance()) ) {
				return pw;
			}
		}
		return null;
	}

	private boolean isPluginAlreadyDiscovered(PluginProperties pp) {
		boolean isDiscovered = false;
		for(PluginWrapper pw2 : pluginList.values()) {
			isDiscovered = pp.equals(pw2);
		}
		return isDiscovered;
	}

	private int generateIntegerID(){
		int generatedPluginID = counterPluginID++;
		log.debug("Generated plugin id: " + generatedPluginID);
		return generatedPluginID;
	}

	/**
	 *
	 * @param propertyFilepath
	 * @param pluginID
	 * @return null if not loaded
	 */
	private PluginProperties buildPluginProperties(Path propertyFilepath, int pluginID) {
		PluginProperties pluginProps= null;

		try {
			PropertiesFileLoader pfl = new PropertiesFileLoader(propertyFilepath);

			if(!pfl.readPropertiesFile()) {
				log.error("Failed to read plugin property file '" + propertyFilepath + "'");
				return null;
			}

			pluginProps = new PluginProperties(
					pluginID,
					pfl.getPropertie("plugin.name"),
					pfl.getPropertie("plugin.version"),
					pfl.getPropertie("plugin.fqnpluginclass")
				);
		} catch (IllegalArgumentException e) {
			log.catching(Level.DEBUG, e);
			return null;
		}

		return pluginProps;
	}

	// interrupt convient methods
	public boolean interruptPlugin(int pluginID) {
		boolean success = false;
		PluginWrapper pw = pluginList.get(pluginID);
		if(pw == null) {
			success = false;
		} else {
			success = this.interruptPlugin(pw);
		}
		return success;
	}

	public boolean interruptPlugin(Plugin pluginInstance) {
		boolean success = false;
		PluginWrapper pw;
		if( (pw = findWrapperFromPluginInstance(pluginInstance)) != null ) {
			success = this.interruptPlugin(pw);
		}
		return success;
	}

	public boolean interruptPlugin(PluginWrapper pluginWrapper) {
		log.debug("Trying to interrupt plugin '" + pluginWrapper.getProperties().getName()
				+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		try {
			pluginWrapper.getPluginInstance().interruptExecution();
			log.debug("Successfully interrupted plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		} catch(Exception e) {
			log.error("Failed to interrupt plugin '" + pluginWrapper.getProperties().getName()
				+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
			log.catching(Level.DEBUG, e);
			return false;
		}
		pluginWrapper.setPluginState(PluginState.INTERRUPTED);
		return true;
	}

	// continue convient methods
	public boolean continuePlugin(int pluginID) {
		boolean success = false;
		PluginWrapper pw = pluginList.get(pluginID);
		if(pw == null) {
			success = false;
		} else {
			success = this.continuePlugin(pluginList.get(pluginID));
		}
		return success;
	}

	public boolean continuePlugin(Plugin pluginInstance) {
		boolean success = false;
		PluginWrapper pw;
		if( (pw = findWrapperFromPluginInstance(pluginInstance)) != null ) {
			success = this.continuePlugin(pw);
		}
		return success;
	}

	public boolean continuePlugin(PluginWrapper pluginWrapper) {
		log.debug("Try to continue plugin '" + pluginWrapper.getProperties().getName()
				+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		try {
			pluginWrapper.getPluginInstance().continueExecution();
			log.debug("Successfully continued plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
		} catch(Exception e) {
			log.error("Failed to continue plugin '" + pluginWrapper.getProperties().getName()
					+ "' (ID: " + pluginWrapper.getProperties().getID() + ")");
			log.catching(Level.DEBUG, e);
			return false;
		}
		pluginWrapper.setPluginState(PluginState.RUNNING);
		return true;
	}

	public Plugin activatePlugin(int pluginID) {
		PluginWrapper pw = pluginList.get(pluginID);
		pw.setPluginActivationState(PluginActivationState.ACTIVATED);
		log.debug("PluginActivationState of plugin '" + pluginID + "' changed to 'ACTIVATED'.");
		this.startPlugin(pw);
		writeLoadedPluginToPropertyFile(pluginList.get(pluginID));
		return pw.getPluginInstance();
	}

	public PluginType deactivatePlugin(int pluginID) {
		PluginWrapper pw = pluginList.get(pluginID);
		pw.setPluginActivationState(PluginActivationState.DEACTIVATED);
		log.debug("PluginActivationState of plugin '" + pluginID + "' changed to 'DEACTIVATED'.");
		this.stopPlugin(pw);
		removeLoadedPluginFromPropertyFile(pluginList.get(pluginID));
		return pw.getPluginType();
	}

	public PluginType deactivatePlugin(Plugin instance) {
		PluginWrapper pw = findWrapperFromPluginInstance(instance);
		if( pw == null ) {
			log.fatal("Could not locate PluginWrapper. Deactivation of plugin failed.");
			return null;
		} else {
			return deactivatePlugin(pw.getProperties().getID());
		}
	}

	// pluginProperties convient methods.
	public PluginProperties getPluginProperties(int pluginID) {
		return pluginList.get(pluginID).getProperties();
	}

	public PluginProperties getPluginProperties(Plugin pluginInstance) {
		PluginWrapper pw;
		if( (pw = findWrapperFromPluginInstance(pluginInstance)) != null ) {
			return pw.getProperties();
		}
		return null;
	}

	public Map<Integer, PluginWrapper> getPluginList() {
		return pluginList;
	}

	public List<Plugin> getLoadedPlugins() {
		return loadedPlugins;
	}

	public Plugin getLoadedSTTPlugin() {
		for(Plugin p : loadedPlugins) {
			if(p instanceof STTPlugin) {
				return p;
			}
		}
		return null;
	}

	public Plugin getLoadedTTSPlugin() {
		for(Plugin p : loadedPlugins) {
			if(p instanceof TTSPlugin) {
				return p;
			}
		}
		return null;
	}

	public List<Plugin> getLoadedAppPlugins() {
		List<Plugin> pl = new ArrayList<Plugin>();
		for(Plugin p : loadedPlugins) {
			if(p instanceof AppPlugin) {
				pl.add(p);
			}
		}
		return pl;
	}

	private void cleanPropertyFile() {
		log.debug("Clean ava configuration file.");
		PropertiesFileLoader ppl = new PropertiesFileLoader(
				Paths.get(ApplicationConfig.getConfigDir() + ApplicationConfig.getConfigName()));
		ppl.readPropertiesFile();

		Iterator<Object> iter = ppl.getPropertiesObject().keySet().iterator();
		while(iter.hasNext()) {
			Object key = iter.next();
			if( ((String) key).startsWith(LOADED_PLUGINS_PROPS_PREFIX) ) {
				iter.remove();
				log.debug("Removed key '" + (String) key + "'.");
			}
		}

		for(PluginWrapper pw : pluginList.values()) {
			if( pw.getPluginActivationState() == PluginActivationState.ACTIVATED ) {
				writeLoadedPluginToPropertyFile(pw);
			}
		}

		ppl.storePropertiesFile();
		log.debug("Finished cleaning ava configuration file.");
	}

	private void writeLoadedPluginToPropertyFile(PluginWrapper pw) {
		PropertiesFileLoader ppl = new PropertiesFileLoader(
				Paths.get(ApplicationConfig.getConfigDir() + ApplicationConfig.getConfigName()));
		ppl.readPropertiesFile();
		ppl.setProperty(LOADED_PLUGINS_PROPS_PREFIX + pw.getProperties().getID(),
				pw.getJarFilePath().toString());
		log.debug("Wrote activated plugin to ava configuration file:\n"
				+ "\tkey: " + LOADED_PLUGINS_PROPS_PREFIX + pw.getProperties().getID() + "\n"
				+ "\tvalue: " + pw.getJarFilePath().toString());
	}

	private void removeLoadedPluginFromPropertyFile(PluginWrapper pw) {
		PropertiesFileLoader ppl = new PropertiesFileLoader(
				Paths.get(ApplicationConfig.getConfigDir() + ApplicationConfig.getConfigName()));
		ppl.readPropertiesFile();
		ppl.removeProperty(LOADED_PLUGINS_PROPS_PREFIX + pw.getProperties().getID());
		log.debug("Removed deactivated plugin to ava configuration file:\n"
				+ "\tkey: " + LOADED_PLUGINS_PROPS_PREFIX + pw.getProperties().getID() + "\n"
				+ "\tvalue: " + pw.getJarFilePath().toString());
	}
}
