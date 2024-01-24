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

/**
 * Encapsulates application settings for the role loader application.
 */
public interface ApplicationSettings {
	
	/**
	 * Get the name of the file to load the data from
	 * @return The name of the file or a default value
	 */
	String getDataFileName();
	
	/**
	 * Get a regular expression for the file delimiter
	 * @return The regular expression or a default value (,)
	 */
	String getDelimiter();
	
	/**
	 * Get the distinguished name of the organization for the data to be loaded into 
	 * @return The DN of the organization
	 */
	String getOrganizationDN();
	
	/**
	 * Load the application settings.
	 */
	void load();

}
