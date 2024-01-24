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

package examples.serviceprovider.jndi;

import java.util.Properties;

import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.remoteservices.provider.ProviderConfigurationException;
import com.ibm.itim.remoteservices.provider.ServiceProvider;
import com.ibm.itim.remoteservices.provider.ServiceProviderFactory;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

public class JNDIServiceProviderFactory implements ServiceProviderFactory {
	private static final String PROVIDER_URL_PROP = "erjndiproviderurl";

	private static final String FACTORY_PROP = "erjndifactory";

	private static final String PRINCIPAL_PROP = "erjndiprincipal";

	private static final String CREDENTIALS_PROP = "erjndicredentials";

	private static final String ROOT_PROP = "erjndiroot";

	public JNDIServiceProviderFactory() {
	}

	public ServiceProvider getServiceProvider(
			ServiceProviderInformation contextInformation)
			throws ProviderConfigurationException {
		Properties props = contextInformation.getProperties();

		String providerURL = props.getProperty(PROVIDER_URL_PROP);
		if (providerURL == null)
			throw new ProviderConfigurationException("URL is required");

		String factory = props.getProperty(FACTORY_PROP);
		if (factory == null)
			throw new ProviderConfigurationException("JNDI factory is required");

		String principal = props.getProperty(PRINCIPAL_PROP);
		String credentials = props.getProperty(CREDENTIALS_PROP);

		String rootDNString = props.getProperty(ROOT_PROP);
		DistinguishedName rootDN;
		if (rootDNString == null)
			rootDN = new DistinguishedName("");
		else
			rootDN = new DistinguishedName(rootDNString);

		return new JNDIServiceProvider(contextInformation, providerURL,
				factory, principal, credentials, rootDN);
	}
}
