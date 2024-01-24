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

import java.util.Hashtable;
import java.util.Random;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountSearch;
import com.ibm.itim.remoteservices.provider.ProviderConfigurationException;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchCriteria;
import com.ibm.itim.remoteservices.provider.SearchResults;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;
import com.ibm.itim.remoteservices.provider.ServiceProviderV2;
import com.ibm.itim.remoteservices.provider.RequestStatus.Status;

/**
 * Provisions an LDAP directory using JNDI. Any LDAP classes can be managed
 * through the connector if identified to the provisioning platform. When
 * implementing the basic provisioning functions, to keep things simple, this
 * connector has made a few assumptions, 1) the name attribute of a user is
 * eruid, 2) the password attribute of a user is userPassword, and 3) no
 * inheritance tree is supported when adding users. In addition to these
 * assumptions, the connector must also deal with a couple of limitations of
 * enRole, 1) enRole expects to represent the user id of an account managed by a
 * connector to be erUid, 2) enRole expects to represent the password of an
 * account managed by a connector to be erPassword. For the password attribute
 * limitation, a mapping is performed by the connector. For the user id, since
 * the same directory server representing the resource could be used for enRole,
 * the only way to keep the schema consistent is to just user eruid as the
 * assumed user id attribute in both contexts. Therefore no mapping is required.
 */
public class JNDIServiceProvider implements ServiceProviderV2 {
	private static final String NAME_ATTRIBUTE = "eruid";

	private static final String PASSWORD_ATTRIBUTE = "userpassword";

	private static final String CLASS_ATTRIBUTE = "objectclass";

	private static final String PROVIDER_URL_PROPERTY = "java.naming.provider.url";

	private static final String FACTORY_PROPERTY = "java.naming.factory.initial";

	private static final String PRINCIPAL_PROPERTY = "java.naming.security.principal";

	private static final String CREDENTIALS_PROPERTY = "java.naming.security.credentials";

	private ServiceProviderInformation contextInformation;

	private InitialDirContext dirContext;

	private Random passwordGenerator;

	private DistinguishedName rootDN;

	/**
	 * Creates the connection to the directory.
	 */
	JNDIServiceProvider(ServiceProviderInformation contextInformation,
			String providerURL, String factoryClass, String principal,
			String credentials, DistinguishedName rootDN)
			throws ProviderConfigurationException {
		this.contextInformation = contextInformation;
		this.rootDN = rootDN;

		Hashtable<String, String> contextEnvironment = new Hashtable<String, String>();

		contextEnvironment.put(PROVIDER_URL_PROPERTY, providerURL);
		contextEnvironment.put(FACTORY_PROPERTY, factoryClass);

		if (principal != null)
			contextEnvironment.put(PRINCIPAL_PROPERTY, principal);

		if (credentials != null)
			contextEnvironment.put(CREDENTIALS_PROPERTY, credentials);

		try {
			this.dirContext = new InitialDirContext(contextEnvironment);
		} catch (NamingException ne) {
			throw new ProviderConfigurationException(ne.getExplanation());
		}

		this.passwordGenerator = new Random();
	}

	/**
	 * Adds a user to the directory. First maps any enRole specific attributes
	 * to directory attributes. Uses the root dn property of the connector to be
	 * the node the user is added to.
	 */
	public RequestStatus add(String entryClass, AttributeValues attributes,
			String requestId) {
		RequestStatus status = new RequestStatus(RequestStatus.SUCCESSFUL);

		// find uid attribute in given attributes to create DN with
		AttributeValue uidValue = attributes.get(NAME_ATTRIBUTE);

		if (uidValue == null) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(NAME_ATTRIBUTE + " is required");
		} else {
			// create class name attribute
			// NOTE: this example does not handle inheritance, only one
			// classname is used
			BasicAttribute classAttribute = new BasicAttribute(CLASS_ATTRIBUTE,
					entryClass);

			// convert given attributes to JNDI form
			Attributes entryAttributes = new ServiceProviderDataConverter()
					.createJNDIAttributes(attributes);

			// add class name attribute to list
			entryAttributes.put(classAttribute);

			try {
				// create entry
				this.dirContext.bind(this.createEntryDN(uidValue
						.getValueString()), null, entryAttributes);
			} catch (NamingException ne) {
				status.setStatus(RequestStatus.UNSUCCESSFUL);
				status.setReasonMessage(ne.getExplanation());
			}
		}

		return status;
	}

	/**
	 * Changes the password of the user. First finds the account to determine
	 * how to identify the entry to be modified in the directory. This is done
	 * by constructing the dn in the directory based on the user id of the
	 * account.
	 */
	public RequestStatus changePassword(String entryDN, byte[] newPassword,
			String requestId) {
		RequestStatus status = new RequestStatus(RequestStatus.SUCCESSFUL);

		try {
			// find account matching entryDN to figure out parameters (uid in
			// this case) needed to construct the DN.
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entryDN));
			Account targetAccount = (Account) accountEntity
					.getDirectoryObject();

			String name = createEntryDN(targetAccount.getUserId());

			BasicAttributes passwordAttributes = new BasicAttributes();
			passwordAttributes.put(PASSWORD_ATTRIBUTE, newPassword);

			this.dirContext.modifyAttributes(name,
					DirContext.REPLACE_ATTRIBUTE, passwordAttributes);
		} catch (NamingException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getExplanation());
		} catch (ObjectNotFoundException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getMessage());
		} catch (ModelCommunicationException me) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(me.getMessage());
		}

		return new RequestStatus(RequestStatus.SUCCESSFUL);
	}

	/**
	 * Deletes the user. First finds the account to determine how to identify
	 * the entry to be modified in the directory. This is done by constructing
	 * the dn in the directory based on the user id of the account.
	 */
	public RequestStatus delete(String entryDN, String requestId) {
		RequestStatus status = new RequestStatus(RequestStatus.SUCCESSFUL);

		try {
			// find account matching entryDN to figure out parameters (uid in
			// this case) needed to construct the DN.
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entryDN));
			Account targetAccount = (Account) accountEntity
					.getDirectoryObject();

			String name = createEntryDN(targetAccount.getUserId());

			this.dirContext.destroySubcontext(name);
		} catch (NamingException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getExplanation());
		} catch (ObjectNotFoundException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getMessage());
		} catch (ModelCommunicationException me) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(me.getMessage());
		}

		return status;
	}

	public ServiceProviderInformation getServiceProviderInfo() {
		return this.contextInformation;
	}

	/**
	 * Changes the attributes of the user. First finds the account to determine
	 * how to identify the entry to be modified in the directory. This is done
	 * by constructing the dn in the directory based on the user id of the
	 * account.
	 */
	public RequestStatus modify(String entryDN, AttributeChanges changes,
			String requestId) {
		RequestStatus status = new RequestStatus(RequestStatus.SUCCESSFUL);

		ModificationItem[] mods = new ServiceProviderDataConverter()
				.createJNDIModifications(changes);

		try {
			// find account matching entryDN to figure out parameters (uid in
			// this case) needed to construct the DN.
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entryDN));
			Account targetAccount = (Account) accountEntity
					.getDirectoryObject();

			String name = createEntryDN(targetAccount.getUserId());

			this.dirContext.modifyAttributes(name, mods);
		} catch (NamingException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getExplanation());
		} catch (ObjectNotFoundException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getMessage());
		} catch (ModelCommunicationException me) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(me.getMessage());
		}

		return status;
	}

	/**
	 * Simply changes the password to the user to the one supplied.
	 */
	public RequestStatus restore(String entryDN, byte[] password,
			String requestId) {
		return this.changePassword(entryDN, password, requestId);
	}

	/**
	 * Performs a JNDI search with the given filter. If no filter is supplied,
	 * all entries are expected to be returned. The root of the search is
	 * determined by the root dn property.
	 */
	public SearchResults search(SearchCriteria criteria, String requestId) {
		RequestStatus status = new RequestStatus(RequestStatus.SUCCESSFUL);

		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setDerefLinkFlag(false);

		SearchResults results = null;

		// if no search base specified, use root of this connector
		String base = criteria.getBase();
		if (base == null || base.length() == 0)
			base = this.rootDN.getAsString();

		// if no filter specified, assume all user and group entries
		String filter = criteria.getFilter();
		if (filter == null || filter.length() == 0)
			filter = "(objectclass=*)";

		try {
			NamingEnumeration _enum = this.dirContext.search(base, filter,
					controls);

			results = new JNDISearchResults(status, _enum);
		} catch (NamingException ne) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(ne.getExplanation());
		}

		return results;
	}

	/**
	 * Simply changes the user's password to a value they don't know. It is
	 * generated randomly.
	 */
	public RequestStatus suspend(String entryDN, String requestId) {
		// create random generated password to lock user out
		byte[] suspendPassword = new byte[8];
		this.passwordGenerator.nextBytes(suspendPassword);

		return this.changePassword(entryDN, suspendPassword, requestId);
	}

	/**
	 * Just gets the root name.
	 */
	public boolean test() {
		return test2().succeeded();
	}
	
	public RequestStatus test2() {
		boolean good = true;

		try {
			this.dirContext.getNameInNamespace();
		} catch (NamingException e) {
			good = false;
		}

		Status res = good ? Status.SUCCESSFUL : Status.UNSUCCESSFUL;
		return new RequestStatus(res);
	}

	private String createEntryDN(String userId) {
		String dn;

		String root = this.rootDN.getAsString();

		if (root.length() > 0)
			dn = NAME_ATTRIBUTE + "=" + userId + "," + root;
		else
			dn = NAME_ATTRIBUTE;

		return dn;
	}
}
