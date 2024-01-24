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
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Encapsulates a map to translate from TIM attribute values to remote values
 * and in the opposite direction. This is normally needed to translate values
 * for status on the managed resource into a value that TIM can use. The map is
 * case insensitive. Entries in the map should have the form:
 * 
 * attributeName.timValue=remoteValue
 * 
 * where
 * 
 * attributeName = the name of the attribute in the TIM schema timValue = the
 * value in the TIM data store remoteValue = the value in the relational
 * database
 * 
 * For example,
 * 
 * erAccountStatus.0=ENABLED erAccountStatus.1=DISABLED
 * 
 * Attributes values that have the same name in TIM as they do in the relational
 * database do not need an entry in this map.
 */
class ValueMap {

	private static final String UTF8 = "UTF-8";

	private static final char PERIOD = '.';

	private static final String VALUE_MAP_PROP_NAME = "erRDBMSValueMap";

	// key is attributeName.timValue
	private final Properties timToRemoteMap = new Properties();

	// key is attributeName.remoteValue
	private final Properties remoteToTIMMap = new Properties();

	ValueMap(ServiceProviderInformation serviceProviderInfo) {
		String valueMapString = serviceProviderInfo.getProperties()
				.getProperty(VALUE_MAP_PROP_NAME);
		if (valueMapString != null) {
			parsePropertiesString(valueMapString);
		}
	}

	/**
	 * Given the TIM attribute name and the remote value, find the equivalent
	 * TIM value. If no value is found then the remote attribute name will be
	 * returned.
	 * 
	 * @param attributeName
	 *            the name of the attribute in the TIM data store
	 * @param remoteValue
	 *            the value in the relational database
	 * @return the TIM value of the attribute
	 */
	String getTIMValue(String attributeName, String remoteValue) {
		if ((attributeName != null) && (remoteValue != null)) {
			String key = attributeName.toLowerCase() + PERIOD
					+ remoteValue.toLowerCase();
			if (remoteToTIMMap.containsKey(key)) {
				return remoteToTIMMap.getProperty(key);
			}
		}
		return remoteValue;
	}

	/**
	 * Parses the properties string entered in the service definition.
	 * 
	 * @param propertiesString
	 *            in the form timName = remoteName
	 */
	private void parsePropertiesString(String propertiesString) {
		try {
			Properties properties = new Properties();
			byte[] bytes = propertiesString.getBytes(UTF8);
			InputStream is = new ByteArrayInputStream(bytes);
			properties.load(is);
			Set keySet = properties.keySet();
			Iterator keyIT = keySet.iterator();
			while (keyIT.hasNext()) {
				String timAttributeNameValue = (String) keyIT.next();
				String remoteAttributeName = (String) properties
						.get(timAttributeNameValue);

				int index = timAttributeNameValue.lastIndexOf(PERIOD);
				String timAttrName = null;
				String timValue = null;
				if (index > 0 && index < timAttributeNameValue.length() - 1) {
					timAttrName = timAttributeNameValue.substring(0, index);
					timValue = timAttributeNameValue.substring(index + 1,
							timAttributeNameValue.length());
					timAttrName = timAttrName.toLowerCase();
					String remoteValue = remoteAttributeName.toLowerCase();
					timToRemoteMap.put(timAttributeNameValue.toLowerCase(),
							remoteValue);
					remoteToTIMMap.put(timAttrName + PERIOD + remoteValue,
							timValue);
					SystemLog.getInstance().logInformation(
							this,
							"[parsePropertiesString] timAttrName: "
									+ timAttrName + ", timValue: " + timValue
									+ ", remoteValue: " + remoteValue);
				} else {
					SystemLog.getInstance().logError(
							this,
							"[parsePropertiesString] no value found for "
									+ timAttributeNameValue);
				}
			}
		} catch (UnsupportedEncodingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		} catch (IOException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		}
	}

}
