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

import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Gathers query metadata for searching for group data. Group data is gathered
 * from the ServiceProviderInformation object, which originated in either the
 * service entity or the resource.def file.
 */
class GroupQueryMetaDataFactory {

	// name of service attribute for SQL to search for group data
	private static final String SEARCH_GROUP_SQL_PROP_NAME = "erRDBMSSearchGroupSQL";

	// name of service attribute to name group entries
	private static final String GROUP_NAMING_ATTR_PROP_NAME = "erRDBMSGroupDNAttr";

	// name of group object class (from property in resource.def)
	private static final String GROUP_OBJECT_CLASS_PROP_NAME = "groupObjectClass";

	/**
	 * Constructs a QueryMetaData for searching for accounts in the database
	 * 
	 * @param serviceProviderInfo
	 * @return data for searching for accounts in the database
	 * @throws RemoteServicesException
	 *             if the meta data object could not be constructed
	 */
	QueryMetaData createQueryMetaData(
			ServiceProviderInformation serviceProviderInfo) {
		String searchGroupSQL = (String) serviceProviderInfo.getProperties()
				.getProperty(SEARCH_GROUP_SQL_PROP_NAME);
		String groupNamingAttrProp = (String) serviceProviderInfo
				.getProperties().getProperty(GROUP_NAMING_ATTR_PROP_NAME);
		String groupObjectClass = (String) serviceProviderInfo.getProperties()
				.getProperty(GROUP_OBJECT_CLASS_PROP_NAME);
		return new QueryMetaData(searchGroupSQL, groupNamingAttrProp,
				groupObjectClass);
	}

}
