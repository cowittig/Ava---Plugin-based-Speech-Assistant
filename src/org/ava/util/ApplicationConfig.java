package org.ava.util;


/**
 * This class contains all data to initialize Ava.
 * Moreover there some other data, the system is using while runtime.
 *
 * @author Kevin
 * @version 1.4
 * @since 15.03.2016
 *
 * Changelog:
 * 2016-03-20 Constantin v1.1
 * 		-- Added default plugins directory './plugins/'
 * 2016-05-24 Constantin v1.2
 * 		-- Added matching treshold
 * 2016-05-28 Constantin v1.3
 * 		-- Added activation phrase
 * 2016-06-03 Constantin v1.4
 * 		-- Removed logDir setting
 */
public class ApplicationConfig {

	private static ApplicationConfig storage = new ApplicationConfig();

	/**
	 * General informations: Application name and version.
	 */
	public final static String appName 	= "Ava";
	public final static String version 	= "1.0";

	private static String pluginDir		= "./plugins/";
	private static String configDir 	= "./res/";					// Config directory. Init with default directory: ./
	private static String configName 	= "ava.properties";		// Config name. Init with default name: ava.properties

	/**
	 * Variables for log setting.
	 */
	private static String logLevel;

	/**
	 * Matching settings.
	 */
	private static double matchingTreshold;

	private static boolean cui_active = false;

	private static String activationPhrase = "Ava";

	private ApplicationConfig() {}

	public ApplicationConfig getInstance() {
		return storage;
	}

	public static String getPluginDir() {
		return pluginDir;
	}

	public static void setPluginDir(String pluginDir) {
		ApplicationConfig.pluginDir = pluginDir;
	}

	public static String getConfigDir() {
		return configDir;
	}

	public static void setConfigDir(String configDir) {
		ApplicationConfig.configDir = configDir;
	}

	public static String getConfigName() {
		return configName;
	}

	public static void setConfigName(String configName) {
		ApplicationConfig.configName = configName;
	}

	public static String getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(String logLevel) {
		ApplicationConfig.logLevel = logLevel;
	}

	public static String getAppname() {
		return appName;
	}

	public static String getVersion() {
		return version;
	}

	public static boolean isCui_active() {
		return cui_active;
	}

	public static void setCui_active(boolean cui_active) {
		ApplicationConfig.cui_active = cui_active;
	}

	public static double getMatchingTreshold() {
		return matchingTreshold;
	}

	public static void setMatchingTreshold(double matchingTreshold) {
		ApplicationConfig.matchingTreshold = matchingTreshold;
	}

	public static void setActivationPhrase(String activationPhrase) {
		ApplicationConfig.activationPhrase  = activationPhrase;
	}

	public static String getActivationPhrase() {
		return activationPhrase;
	}
}
