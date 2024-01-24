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

package examples.serviceprovider.file;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.ibm.itim.remoteservices.provider.ProviderConfigurationException;
import com.ibm.itim.remoteservices.provider.ServiceProvider;
import com.ibm.itim.remoteservices.provider.ServiceProviderFactory;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Example service provider factory implementation that creates a
 * FileServiceProvider object. Since the TIM framework calls this method to get
 * a ServiceProvider for each request it is the place to put code for doing
 * something more efficient than just creating a new instance of a
 * ServiceProvider implementation. In this case, there is a pool of
 * FileServiceProviders, one for each service with a different log file.
 */
public class FileServiceProviderFactory implements ServiceProviderFactory {

	private static final String DEFAULT_FILE = "fileprovider.log";

	/**
	 * Pool of FileServiceProdiers
	 */
	private final Map<String, ServiceProvider> providers = new HashMap<String, ServiceProvider>();

	/**
	 * Gets an instance of the FileServiceProvider.
	 * 
	 * @param serviceProviderInfo
	 *            encapsulates information about the service type and instance
	 * @return a FileServiceProvider implementing the ServiceProvider interface
	 */
	public ServiceProvider getServiceProvider(
			ServiceProviderInformation serviceProviderInfo)
			throws ProviderConfigurationException {

		String fileName = serviceProviderInfo.getProperties().getProperty(
				"erfilename");
		if (fileName == null) {
			fileName = DEFAULT_FILE;
		}
		ServiceProvider provider = null;
		if (providers.containsKey(fileName)) {
			return providers.get(fileName);
		}
		try {
			System.out.println(getClass().getName() + ": creating a new "
					+ "FileServiceProvider with erFileName: " + fileName);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName, true), "UTF-8"));
			provider = new FileServiceProvider(writer, serviceProviderInfo);
			providers.put(fileName, provider);
		} catch (IOException e) {
			System.err.println(FileServiceProvider.class.getName() + ": "
					+ "Unable to initialize provider log file: "
					+ e.getMessage());
			throw new ProviderConfigurationException(e.getMessage(), e);
		}
		return provider;
	}

}
