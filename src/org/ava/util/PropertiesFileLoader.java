package org.ava.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * With this PropertiesFileLoader class can varibale propertie files be loaded.
 * While using this class, the attributes propertiesFilePath and propertiesFileName have to be valid.
 *
 * @author Kevin
 * @version 1.2
 * @since 13.03.2016
 *
 * Changelog:
 * 2016-03-20 Constantin v1.1
 * 		-- Added constructor to support nio file api.
 * 2016-03-26 Constantin v1.2
 * 		-- Added constant for a header comment.
 * 		-- Added support for removing properties.
 * 		-- Added a separate method to store Properties in file.
 * 		-- Override keys() method of Properties object, so that properties
 * 		   are stored alphabetically
 *
 */
public class PropertiesFileLoader {

	private final static Logger log = LogManager.getLogger(PropertiesFileLoader.class);

	/** This string will be printed as a header comment to the properteries file. */
	private static final String PROPERTY_FILE_HEADER_COMMENT =
			  "#####################################################################\n"
			+ "#                                                                    #\n"
			+ "# Configuration file for Ava, the speech assistant for your home.    #\n"
			+ "#                                                                    #\n"
			+ "######################################################################";

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
		log.info("PropertiesFilePath is set to: " + this.propertiesFilePath);
		log.info("PropertiesFileName is set to: " + this.propertiesFileName);
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
		log.info("PropertiesFilePath is set to: " + this.propertiesFilePath);
		log.info("PropertiesFileName is set to: " + this.propertiesFileName);
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
			log.info("Properties file path was set to: " + path);
		}

		log.error("Path for the property file is not valid.");

	}


	public void setPropertiesFileName(String name) {
		log.debug("setPropertiesFileName is called with argument path '" + name + "'.");
		if( name != null || name != "") {
			this.propertiesFileName = name;
			log.info("Properties file name was set to: " + name);
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
		log.info("Key with value '" + key + " : " + value + "' added/modified to the Properties file "
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
	@SuppressWarnings("serial")
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

		// create properties object and override keys(), so that properties will be stored in
		// an alphabetically fashion
		// code snippet from: http://stackoverflow.com/a/17011319
		properties = new Properties() {
			@Override
		    public synchronized Enumeration<Object> keys() {
		        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		    }
		};

		if( propertiesFile.exists() ) {

			BufferedInputStream bis;
			try {
				bis = new BufferedInputStream(new FileInputStream(propertiesFile));
				properties.load(bis);
				bis.close();
				log.info("Properties file '" + this.propertiesFilePath + this.propertiesFileName + "' loaded.");

			} catch (IOException e) {
				log.catching(Level.DEBUG, e);
				return false;
			}
		} else {
			log.error("Properties file does not exist or can't be found. Filepath: " + this.propertiesFilePath + this.propertiesFileName);
			return false;
		}
		return true;
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
		log.info("Validating " + this.propertiesFileName);
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

		log.info("Properties file '" + this.propertiesFileName + "' ok.");
		return null;
	}


	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}


	public String getPropertiesFileName() {
		return propertiesFileName;
	}
}
