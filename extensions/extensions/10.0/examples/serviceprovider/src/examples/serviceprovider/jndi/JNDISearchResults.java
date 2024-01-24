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

import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchResult;
import com.ibm.itim.remoteservices.provider.SearchResults;

public class JNDISearchResults implements SearchResults {
	private static final String ACCOUNT_OBJECTCLASS = "erJNDISampleUserAccount";

	private static final String GROUP_OBJECTCLASS = "erJNDISampleGroup";

	private NamingEnumeration _enum;

	private RequestStatus status;

	private ServiceProviderDataConverter converter;

	public JNDISearchResults(RequestStatus status, NamingEnumeration _enum) {
		this.converter = new ServiceProviderDataConverter();
		this.status = status;
		this._enum = _enum;
	}

	public void close() throws RemoteServicesException {
		try {
			this._enum.close();
		} catch (NamingException ne) {
			throw new RemoteServicesException(ne.getMessage());
		}
	}

	public RequestStatus getRequestStatus() {
		return status;
	}

	public boolean hasNext() throws RemoteServicesException {
		try {
			return this._enum.hasMore();
		} catch (NamingException ne) {
			throw new RemoteServicesException(ne.getMessage());
		}
	}

	public SearchResult next() throws RemoteServicesException {
		try {
			// get next result from NamingEnumeration
			javax.naming.directory.SearchResult result = (javax.naming.directory.SearchResult) this._enum
					.next();

			// convert javax.naming attributes to compatible format
			AttributeValues attributes = this.converter
					.createModelAttributes(result.getAttributes());

			// get name of result
			String name = result.getName();
			if (name == null) // if root node
				name = "";

			// get classes of result
			Vector<String> classes = new Vector<String>();
			Attributes jndiAttributes = result.getAttributes();
			Attribute classAttr = jndiAttributes.get("objectclass");
			if (classAttr != null) {
				NamingEnumeration values = classAttr.getAll();
				while (values.hasMore())
					classes.add(values.next().toString());
			}

			for (String className : classes) {
				if (className.equalsIgnoreCase("groupOfNames")) {
					name = (String)attributes.get("cn").getValueString().replaceAll(",", "");
					String itemName = "erJNDIGroupName=" + name;
					attributes = new AttributeValues();
					attributes.put(new AttributeValue("erJNDIGroupName", name));
					String objClassName = GROUP_OBJECTCLASS;
					return new SearchResult(itemName, objClassName, attributes);
				} else if (className.equalsIgnoreCase("inetOrgPerson")) {
					name = (String)attributes.get("cn").getValueString().replaceAll(",", "");
					String itemName = "erUid=" + name;
					attributes = new AttributeValues();
					attributes.put(new AttributeValue("erUid", name));
					String objClassName = ACCOUNT_OBJECTCLASS;
					return new SearchResult(itemName, objClassName, attributes);
				}
			}

			// return the compatible SearchResult
			return new SearchResult(name, classes, attributes);
		} catch (NamingException ne) {
			throw new RemoteServicesException(ne.getMessage());
		}
	}
}
