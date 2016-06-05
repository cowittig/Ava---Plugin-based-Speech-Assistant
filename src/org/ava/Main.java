package org.ava;

import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.ava.util.ApplicationConfig;
import org.ava.util.PropertiesFileLoader;

/**
 * Class Main including the main entry point for the application Ava.
 *
 * The command line arguments have a higher priority than the data from the properties file. Are there some
 * arguments given, the data from the properties file will be overwritten.
 * Valid command line arguments are:
 * 		-configDir 	{validConfigDir} 			Has to end with a "/"
 * 		-configName {validConfigName} 			Has to be located in the directory {validConfigDir}
 * 		-pluginDir 	{validDirectoryName} 		Has to end with a "/"
 * 		-logLevel 	{INFO|DEBUG|ERROR|FATAL} 	Set the log level of the Logging mechanism.
 * 		-cui_active {true|false} 				True if the console user interface should be started.
 *
 * @author Kevin
 * @version 1.0
 * @since 17.03.2016
 */
public class Main {

	final static Logger log = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		boolean isCommandLineValid = true;

		log.info(ApplicationConfig.appName + " started");
		log.info("Version " + ApplicationConfig.version);

		PropertiesFileLoader loader = null;

		try {
			loader = new PropertiesFileLoader(ApplicationConfig.getConfigDir(), ApplicationConfig.getConfigName());
		} catch (NullPointerException e) {
			log.catching(Level.DEBUG, e);
			return;
		}

		if( !loader.readPropertiesFile() ) {
			log.fatal("No properties file to initialize Ava found. Application will terminate.");
			return;
		}

		ArrayList<String> propList = new ArrayList<String>();
		propList.add("PLUGINDIR");
		propList.add("CONFIGDIR");
		propList.add("LOGDIR");
		propList.add("LOGLEVEL");
		propList.add("LOGNAME");
		propList.add("CUI_ACTIVE");

		String tmp = null;
		if( (tmp = loader.isPropertiesFileValid(propList)) != null ) {
			log.fatal("There are missing properties in " + ApplicationConfig.getConfigName() + ". " + tmp + " is missing. ");
			return;
		}

		initApplicationConfig(loader);

		log.debug(args.length + " arguments are given with the program start form command line.");
		if( args.length > 0 ) {
			isCommandLineValid = handleArguments(args);
		}
		setLogConfiguration();

		if ( isCommandLineValid ) {
			new AvaControl();
		} else {
			log.error("Provide valid command line arguments.");
			log.error("Ava is shuting down.");
		}
	}

	private static boolean handleArguments(String[] args) {
		log.debug("Handling command line arguments");

		int len = args.length;
		String configDir = null;
		String configName = null;


		for(int i = 0; i < len; i++) {

			//if( i+1 == args.length ) {
			//	log.fatal("Missing parameter at the command line after '" + args[i] + "'. Programm will terminate.");
			//	return false;
			//}

			switch(args[i]) {
				case "-configDir":
					configDir = args[i+1];
					i++;
					log.debug("ConfigDir changed by command line to " + configDir);
					break;
				case "-configName":
					configName = args[i+1];
					i++;
					log.debug("ConfigName changed by command line to " + configName);
					break;
				case "-pluginDir":
					ApplicationConfig.setPluginDir(args[i+1]);
					i++;
					log.info("pluginDir changed by command line to " +  ApplicationConfig.getPluginDir());
					break;
				case "-logLevel":
					ApplicationConfig.setLogLevel(args[i+1]);
					i++;
					log.info("logLevel changed by command line to " +  ApplicationConfig.getLogLevel());
					break;
				case "-cui_active":
					String cui_active = args[i+1];
					cui_active = cui_active.toLowerCase();
					if( cui_active.equals("true") )
						ApplicationConfig.setCui_active(true);
					i++;
					log.info("Cui_active changed by command line to " + cui_active);
					break;
			}
		}

		if( configDir != null || configName != null ) {
			ApplicationConfig.setConfigDir(configDir);
			ApplicationConfig.setConfigName(configName);
		}

		log.debug("Command line arguments valid");
		return true;
	}


	private static void initApplicationConfig(PropertiesFileLoader loader) {
		log.debug("Initialze ApplicationConfig object with data from the properties file.");

		ApplicationConfig.setConfigDir(loader.getPropertie("CONFIGDIR"));
		ApplicationConfig.setPluginDir(loader.getPropertie("PLUGINDIR"));
		ApplicationConfig.setLogLevel(loader.getPropertie("LOGLEVEL"));
		try {
			ApplicationConfig.setMatchingTreshold(Double.parseDouble(loader.getPropertie("MATCHING_TRESHOLD")));
		} catch(NullPointerException ex) {
			ApplicationConfig.setMatchingTreshold(0.0);
		}
		ApplicationConfig.setActivationPhrase(loader.getPropertie("ACTIVATION_PHRASE"));


		String cui_active = loader.getPropertie("CUI_ACTIVE");
		cui_active = cui_active.toLowerCase();
		if ( cui_active.equals("true") )
			ApplicationConfig.setCui_active(true);
		else
			ApplicationConfig.setCui_active(false);
	}

	private static void setLogConfiguration() {
		log.debug("Setting Log4j log settings.");
	    LoggerContext ctx = (LoggerContext)LogManager.getContext(false);

	    // change root logger
	    Configuration config = ctx.getConfiguration();
	    Level l = Level.getLevel(ApplicationConfig.getLogLevel());
	    if( l == null ) {
	    	log.error("The given log level is not valid. Log level will be set to INFO.");
	    	l = Level.INFO;
	    }
	    log.info("Ava log level set to " + ApplicationConfig.getLogLevel());
	    config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(l);

		ctx.updateLoggers(config);
	}
}
