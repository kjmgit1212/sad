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

import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ProfileLocator;
import com.ibm.itim.dataservices.model.ServiceProfile;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Gathers query metadata for searching for accounts.
 */
class AccountQueryMetaDataFactory {

	private static final String TENANT_ATTR_NAME = "com.ibm.itim.remoteservices.ResourceProperties.TENANT_DN";

	private static final String SEARCH_SQL_PROP_NAME = "erRDBMSSearchSQL";

	/**
	 * Constructs a QueryMetaData for searching for accounts in the database
	 * 
	 * @param serviceProviderInfo
	 * @return data for searching for accounts in the database
	 * @throws RemoteServicesException
	 *             if the meta data object could not be constructed
	 */
	QueryMetaData createQueryMetaData(
			ServiceProviderInformation serviceProviderInfo,
			RDBMSAttributeMap rdbmsAttributeMap) throws RemoteServicesException {

		SystemLog.getInstance().logInformation(
				this,
				"[createQueryMetaData] props: "
						+ serviceProviderInfo.getProperties());
		String searchSQL = (String) serviceProviderInfo.getProperties()
				.getProperty(SEARCH_SQL_PROP_NAME);
		String profileName = serviceProviderInfo.getServiceProfileName();
		String tenantDN = serviceProviderInfo.getProperties().getProperty(
				TENANT_ATTR_NAME);
		ServiceProfile serviceProfile = (ServiceProfile) ProfileLocator
				.getProfileByName(new DistinguishedName(tenantDN), profileName);
		String accountClassName = serviceProfile.getAccountClass();
		String namingAttribute = rdbmsAttributeMap
				.getRemoteAttributeName(Account.ACCOUNT_ATTR_USERID);
		return new QueryMetaData(searchSQL, namingAttribute, accountClassName);
	}

}
