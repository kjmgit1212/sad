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

import com.ibm.itim.remoteservices.provider.ProviderConfigurationException;
import com.ibm.itim.remoteservices.provider.ServiceProvider;
import com.ibm.itim.remoteservices.provider.ServiceProviderFactory;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Factory class to create an instance of a RDBMSConnector class. TIM will call
 * the method getServiceProvider every time it needs to communicate to the
 * database for account management.
 */
public class RDBMSConnectorFactory implements ServiceProviderFactory {

	/**
	 * Instantiate and initialize a RDBMSConnector. The serviceProviderInfo
	 * should be used to determine the type of service instance.
	 * 
	 * @param serviceProviderInfo
	 *            Encapsulates information about the service instance
	 * @return a class implementing the ServiceProvider for the corresponding
	 *         service instance
	 * @throws ProviderConfigException
	 *             if there is a configuration or lookup problem
	 */
	public ServiceProvider getServiceProvider(
			ServiceProviderInformation serviceProviderInfo)
			throws ProviderConfigurationException {
		return new RDBMSConnector(serviceProviderInfo);
	}

}
