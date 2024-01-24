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

package examples.serviceprovider.rdbms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.ibm.itim.logging.SystemLog;

/**
 * Encapsulates the attribute map to translate from TIM attribute names to
 * remote names and in the opposite direction. Map is case insensitive.
 * Attributes that have the same name in TIM as they do in the relational
 * database do not need an entry in this map.
 */
class RDBMSAttributeMap {

	private final Properties timToRemoteMap = new Properties();

	private final Properties remoteToTIMMap = new Properties();

	private static final String UTF8 = "UTF-8";

	/**
	 * Given the remote attribute name find the equivalent TIM attribute name.
	 * If no TIM attribute name is found then the remote attribute name will be
	 * returned.
	 * 
	 * @param remoteName
	 *            the name of the attribute in the database
	 * @return the name of the attribute in TIM
	 */
	String getTIMAttributeName(String remoteAttributeName) {
		String name = remoteAttributeName.toLowerCase();
		if (remoteToTIMMap.containsKey(name)) {
			return remoteToTIMMap.getProperty(name);
		}
		return name;
	}

	/**
	 * Given the TIM attribute name find the equivalent remote attribute name.
	 * If no remote attribute name is found then the TIM attribute name will be
	 * returned.
	 * 
	 * @param timAttributeName
	 *            the name of the attribute in the database
	 * @return the name of the attribute in the database system
	 */
	String getRemoteAttributeName(String timAttributeName) {
		String name = timAttributeName.toLowerCase();
		if (timToRemoteMap.containsKey(name)) {
			return timToRemoteMap.getProperty(name);
		}
		return name;
	}

	/**
	 * Parses the properties string entered in the service definition.
	 * 
	 * @param propertiesString
	 *            in the form timName = remoteName
	 */
	void parsePropertiesString(String propertiesString) {
		try {
			Properties properties = new Properties();
			byte[] bytes = propertiesString.getBytes(UTF8);
			InputStream is = new ByteArrayInputStream(bytes);
			properties.load(is);
			Set keySet = properties.keySet();
			Iterator keyIT = keySet.iterator();
			while (keyIT.hasNext()) {
				String timAttributeName = (String) keyIT.next();
				String remoteAttributeName = (String) properties
						.get(timAttributeName);
				String timAttrName = timAttributeName.toLowerCase();
				String remoteAttrName = remoteAttributeName.toLowerCase();
				timToRemoteMap.put(timAttrName, remoteAttrName);
				remoteToTIMMap.put(remoteAttrName, timAttrName);
			}
		} catch (UnsupportedEncodingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		} catch (IOException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		}
	}

}
