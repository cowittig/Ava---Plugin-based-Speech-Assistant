package org.ava.pluginengine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Constantin
 * @since 2016-03-20
 * @version 1
 */
public class PluginProperties {

	private final Logger log = LogManager.getLogger(PluginProperties.class);
	
	/** The ID of the plugin. */
	private int id;
	
	/** The name of the plugin. */
	private String name;
	
	/** The version of the plugin. */
	private String version;
	
	/** The full qualified name of the plugin class. */
	private String fqnPluginClass;
	
	/**
	 * @param id
	 * @param name
	 * @param version
	 * @param fqnPluginClass
	 */
	public PluginProperties(int id, String name, String version, String fqnPluginClass) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.fqnPluginClass = fqnPluginClass;
		
		log.debug("Created PluginProperties instance (" + this.toString() + "):\n"
				+ "\tid = " + id + "\n" 
				+ "\tname = " + name + "\n"
				+ "\tversion = " + version + "\n"
				+ "\tfqnPluginClass = " + fqnPluginClass);
	}

	/**
	 * Return the name of the plugin.
	 * 
	 * @return String The name of the plugin.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the ID of the plugin.
	 * 
	 * @return int The ID of the plugin.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Return the version of the plugin.
	 * 
	 * @return String The version of the plugin.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Return the full qualified name of the plugin class.
	 * 
	 * @return String The full qualified name of the plugin class.
	 */
	public String getFqnPluginClass() {
		return fqnPluginClass;
	}
	
	/**
	 * Check if the given object is equal to this PluginProperties.
	 * Equality is determined by checking if the given object is
	 * an instance of PluginProperties and then comparing all properties.
	 * 
	 * @param pluginProperties The given object.
	 * @return True if both objects are equal, false if not.
	 */
	@Override
	public boolean equals(Object pluginProperties) {
		boolean isEqual = false;
		
		if(pluginProperties instanceof PluginProperties) {
			PluginProperties pp = (PluginProperties) pluginProperties;
			if(this.name.equals(pp.getName())
					&& this.version.equals(pp.getVersion())
					&& this.fqnPluginClass.equals(pp.getFqnPluginClass())
					) {
				isEqual = true;
			}
		}
		
		return isEqual;
	}
}
