/************************************************************************
* IBM Confidential
* OCO Source Materials
* *** IBM Security Identity Manager ***
*
* (C) Copyright IBM Corp. 2015  All Rights Reserved.
*
* The source code for this program is not published or otherwise  
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office.
*************************************************************************/
package examples.dataservices;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates application settings for the role loader application.
 * The settings are loaded from a properties file.
 */
public class PropertiesFileApplicationSettings implements ApplicationSettings {

	/**
	 * The configuration file for the program
	 */
	public static String CONFIG_FILE = "bulk_role.properties";
	/**
	 * The default data file to load comma separated variable data from
	 */
	public static String DEFAULT_DATA_FILE = "roles.csv";
	/**
	 * The property attribute name for the data file
	 */
	public static String ATTRIBUTE_DATA_FILE = "data.file";
	/**
	 * The default distinguished name of the organization for the data to be loaded into 
	 */
	public static String DEFAULT_ORG_DN = "erglobalid=00000000000000000000, ou=org, dc=com";
	/**
	 * The property attribute name for the distinguished name of the organization 
	 */
	public static String ATTRIBUTE_ORG_DN = "organization.dn";
	/**
	 * The default regular expression for the file delimiter 
	 */
	public static String DEFAULT_DELIMITER = ",";
	/**
	 * The property attribute name for the regular expression for the file delimiter 
	 */
	public static String ATTRIBUTE_DELIMITER = "delimiter";
	
	private Properties properties;
	private String configFileName = CONFIG_FILE;
	
	/**
	 * Uses the default configuration file CONFIG_FILE for configuration settings.
	 */
	public PropertiesFileApplicationSettings() {
	}
	
	/**
	 * Uses the given configuration file for configuration settings.
	 */
	public PropertiesFileApplicationSettings(String configFileName) {
		this.configFileName = configFileName;
	}
	
	/**
	 * Get the name of the file to load the data from
	 * @return The name of the file or DEFAULT_DATA_FILE if there is no such property
	 */
	public String getDataFileName() {
		if (properties != null) {
			return properties.getProperty(ATTRIBUTE_DATA_FILE, DEFAULT_DATA_FILE);
		}
		return DEFAULT_DATA_FILE;
	}
	/**
	 * Get a regular expression for the file delimiter
	 * @return The regular expression or a default value (,)
	 */
	public String getDelimiter() {
		if (properties != null) {
			return properties.getProperty(ATTRIBUTE_DELIMITER, DEFAULT_DELIMITER);
		}
		return DEFAULT_DELIMITER;
	}
	
	/**
	 * Get the distinguished name of the organization for the data to be loaded into 
	 * @return The DN of the organization
	 */
	public String getOrganizationDN() {
		if (properties != null) {
			return properties.getProperty(ATTRIBUTE_ORG_DN, DEFAULT_ORG_DN);
		}
		return DEFAULT_ORG_DN;
	}
	
	public void load() {
		try {
			System.out.println("Loading configuration file from " + configFileName);
			properties = new Properties();
			FileInputStream is = new FileInputStream(configFileName);
			properties.load(is);
		} catch(IOException ex) {
			System.err.println("Could not load file " + configFileName + " from current working directory(" +
					System.getProperty("user.dir") + "). Using default values");
		}
	}

}
