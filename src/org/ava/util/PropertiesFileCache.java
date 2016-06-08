package org.ava.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesFileCache {
	
	private final static Logger log = LogManager.getLogger(PropertiesFileCache.class);

	private static Map<String, Properties> cache = new HashMap<String, Properties>();
	
	@SuppressWarnings("serial")
	public static Properties getProperties(File propertiesFile) {
		if( cache.containsKey(propertiesFile.getAbsolutePath()) ) {
			return cache.get(propertiesFile.getAbsolutePath());
		} else {
			// create properties object and override keys(), so that properties will be stored in
			// an alphabetically fashion
			// code snippet from: http://stackoverflow.com/a/17011319
			Properties properties = new Properties() {
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
					log.info("Properties file '" + propertiesFile.getAbsolutePath() + "' loaded.");
					cache.put(propertiesFile.getAbsolutePath(), properties);
					return properties;
				} catch (IOException e) {
					log.catching(Level.DEBUG, e);
					return null;
				}
			} else {
				log.error("Properties file does not exist or can't be found. Filepath: " + propertiesFile.getAbsolutePath());
				return null;
			}
		}
	}
}
