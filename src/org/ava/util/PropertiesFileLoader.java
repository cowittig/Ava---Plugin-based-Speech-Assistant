package org.ava.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * With this PropertiesFileLoader class can varibale propertie files be loaded.
 * While using this class, the attributes propertiesFilePath and propertiesFileName have to be valid.
 *
 * @author Kevin
 * @version 1.3
 * @since 13.03.2016
 *
 */
public class PropertiesFileLoader {

	private final static Logger log = LogManager.getLogger(PropertiesFileLoader.class);

	/** This string will be printed as a header comment to the properteries file. */
	private static final String PROPERTY_FILE_HEADER_COMMENT =
			  "###########################################################################################\n"
			+ "#                                                                                          #\n"
			+ "#           Configuration file for Ava, the speech assistant for your home.                #\n"
			+ "#                                                                                          #\n"
			+ "# Possible options:                                                                        #\n"
			+ "#                                                                                          #\n"
			+ "#    -- ACTIVATION PHRASE    = The phrase or word that activates Ava from idle mode.       #\n"
			+ "#    -- CONFIGDIR            = The path to the configuration directory containing this     #\n"
			+ "#                              file. Default directory is: ./res/                          #\n"
			+ "#    -- CUI_ACTIVE           = Wether the CUI is activated at startup. Possible values:    #\n"
			+ "#                              true or false.                                              #\n"
			+ "#    -- LOGLEVEL             = How verbose Ava's output is. Possible values:               #\n"
			+ "#                                  OFF: no logging                                         #\n"
			+ "#                                  FATAL: errors that impact core functionality            #\n"
			+ "#                                  ERROR: less harmful errors, application might continue  #\n"
			+ "#                                  INFO: general information messages                      #\n"
			+ "#                                  DEBUG: verbose output for debugging purposes            #\n"
			+ "#                              Each level will incorparte the messages from the level      #\n"
			+ "#                              above. So INFO will include: INFO + ERROR + FATAL.          #\n"
			+ "#    -- MATCHING_TRESHOLD    = The threshold above which matches will be accepted by       #\n"
			+ "#                              the matching engine. For reliable results the treshold      #\n"
			+ "#                              should be above 0.9.                                        #\n"
			+ "#    -- PLUGINDIR            = The path to the plugin directory. Default directory is:     #\n"
			+ "#                              ./plugins/                                                  #\n"
			+ "#                                                                                          #\n"
			+ "# All other options are automatically created and maintaned by Ava.                        #\n"
			+ "#                                                                                          #\n"
			+ "############################################################################################\n";

	private String propertiesFilePath = "./"; /*src/com/sttPlugin.properties"; */
	private String propertiesFileName = "";
	private Properties properties = null;


	public PropertiesFileLoader(String propertiesFilePath, String propertiesFileName) throws NullPointerException {
		if( propertiesFilePath == null || propertiesFileName == null || propertiesFileName == "" ) {
			throw new NullPointerException("A argument is null or empty.");
		}

		this.propertiesFilePath = propertiesFilePath;
		this.propertiesFileName = propertiesFileName;
		log.debug("PropertiesFileLoader instance created.");
		log.debug("PropertiesFilePath is set to: " + this.propertiesFilePath);
		log.debug("PropertiesFileName is set to: " + this.propertiesFileName);
	}

	public PropertiesFileLoader(Path propertiesFilePath) throws IllegalArgumentException {
		if(propertiesFilePath == null) {
			log.error("Path to properties file is empty");
			throw new IllegalArgumentException("Path to properties file is null.");
		}

		String tmp = propertiesFilePath.toString();
		this.propertiesFilePath = tmp.substring(0, tmp.length() - propertiesFilePath.getFileName().toString().length());
		this.propertiesFileName = propertiesFilePath.getFileName().toString();
		log.debug("PropertiesFileLoader instance created.");
		log.debug("PropertiesFilePath is set to: " + this.propertiesFilePath);
		log.debug("PropertiesFileName is set to: " + this.propertiesFileName);
	}


	/**
	 * With this method the path for the property file can be set.
	 *
	 * @param path Path to the property file including the filename.
	 */
	public void setPropertiesFilePath(String path) {
		log.debug("setPropertiesFilePath is called with argument path '" + path + "'.");
		if( path != null || path != "" ) {
			this.propertiesFilePath = path;
			log.debug("Properties file path was set to: " + path);
		}

		log.error("Path for the property file is not valid.");

	}


	public void setPropertiesFileName(String name) {
		log.debug("setPropertiesFileName is called with argument path '" + name + "'.");
		if( name != null || name != "") {
			this.propertiesFileName = name;
			log.debug("Properties file name was set to: " + name);
		}
	}


	/**
	 * This method should be used to change the value of a key. Does there no key exists in the loaded propertie file
	 * the key will be added to the propertie object and written to the propertie file.
	 * Before you use this method, load a valid properties file.
	 *
	 * @param key The key that should be changed or added
	 * @param value The value for the given key
	 */
	public void setProperty(String key, String value) {
		log.debug("setProperty is called with arguments key '" + key + "' and value '" + value + "'.");
		if( this.properties == null ) {
			log.error("The property file was not loaded.");
			return;
		}

		if( key == null || value == null ) {
			log.error("An argument is NULL and can not be set.");
			return;
		}


		this.properties.setProperty(key, value);
		log.info("Key with value '" + key + " : " + value + "' added/modified in Properties file "
				+ this.propertiesFileName);

		if(storePropertiesFile()) {
			log.info("The updated properties were written to the properties file "
					+ this.propertiesFilePath + this.propertiesFileName);
		} else {
			log.info("Writing updated properties to file failed.");
		}

	}

	/**
	 * Remove a given property from the Properties object. The changes
	 * will be mirrored to the property file.
	 *
	 * @param key The property to remove.
	 * @return boolean True if removing was successful, false if not.
	 */
	public boolean removeProperty(String key) {
		log.debug("Trying to remove property '" + key + "' from the properties file.");
		boolean success = false;

		properties.remove(key);
		if(storePropertiesFile()) {
			log.debug("The property was removed from the properties file.");
		} else {
			log.debug("The property was not removed from the properties file.");
		}

		return success;
	}


	/**
	 * Returns the value corresponding to the given key. If the key is NULL or empty the method returns NULL.
	 * Is the membervariable "properties" null, the method returns NULL, too.
	 *
	 * @param key the key of the wanted value
	 * @return String with the wanted value. Null if an error occured.
	 */
	public String getPropertie(String key) {
		if( this.properties == null )
			return null;

		if( key == null || key == "" )
			return null;

		return this.properties.getProperty(key);
	}

	/**
	 * Returns the Properties object represented by this class.
	 *
	 * @return Properties The Properties object represented by this class.
	 */
	public Properties getPropertiesObject() {
		return this.properties;
	}


	/**
	 * Reads the properties file at the stored propertiesFilePath and propertiesFileName in this object.
	 * If these attributes null or false, nothing will be done.
	 */
	public boolean readPropertiesFile() {
		log.debug("Try to load the properties file: " + this.propertiesFilePath + this.propertiesFileName);
		File propertiesFile = null;
		try {
			propertiesFile = new File(this.propertiesFilePath + this.propertiesFileName);

			if( !propertiesFile.exists() ) {
				log.fatal("Given property file does not exists. <" + this.propertiesFilePath + this.propertiesFileName + ">");
				return false;
			}

			if( propertiesFile.isDirectory() ) {
				log.fatal("Given property file is a directory. <" + this.propertiesFilePath + this.propertiesFileName + ">");
				return false;
			}
		} catch (NullPointerException e) {
			log.catching(Level.DEBUG, e);
			return false;
		}

		properties = PropertiesFileCache.getProperties(propertiesFile);
		if( properties != null ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save the properties object to file.
	 *
	 * @return boolean True if saving was successful, false if not.
	 */
	public boolean storePropertiesFile() {
		boolean success = false;

		String path = propertiesFilePath + propertiesFileName;
		log.debug("Try storing properties file '" + propertiesFilePath + propertiesFileName + "'.");
		try {
			properties.store(Files.newOutputStream(Paths.get(path)), PROPERTY_FILE_HEADER_COMMENT);
			success = true;
		} catch (IOException e) {
			log.catching(Level.DEBUG, e);
			success = false;
		}

		return success;
	}


	/**
	 * Validate the Properties Object in this class, if all properties are included.
	 * The method return null, when all properties are contained. If a property missed the method returns the name of the missing property.
	 * Before this method can be called, call the method readPropertiesFile().
	 *
	 * @exception Throws a NullPointerException if the properties object is not initialized or is NULL.
	 * @param propertyKeys - A ArrayList with all property names for the validation.
	 * @return NULL if everything ok or a String containing the missing property name.
	 */
	public String isPropertiesFileValid(ArrayList<String> propertyKeys) {
		log.debug("Validating " + this.propertiesFileName);
		if( this.properties == null ) {
			log.error("There was no properties file loaded. PropertiesFileLoader.propertiesFileName is NULL");
			return "No file to validate";
		}

		String property = "";
		for(int i = 0; i < propertyKeys.size(); i++) {
			property = this.properties.getProperty(propertyKeys.get(i));
			log.debug("Validate property with key " + propertyKeys.get(i) + ". -> value is '" + property + "'.");
			if(  property == null || property == "" ) { // TODO Wieso geht er nicht ins if rein, obwohl property = "" ist
				return property;
			}
		}

		log.debug("Properties file '" + this.propertiesFileName + "' ok.");
		return null;
	}


	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}


	public String getPropertiesFileName() {
		return propertiesFileName;
	}
}
