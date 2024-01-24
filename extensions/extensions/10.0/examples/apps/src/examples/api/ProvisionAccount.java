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

package examples.api;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.provisioning.AccountManager;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.Person;
import com.ibm.itim.dataservices.model.domain.Service;

/**
 * Sample command-line Java class to provision an account.
 */
public class ProvisionAccount {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PROFILE_NAME = "profile";

	private static final String SERVICE_PROFILE = "serviceprofile";

	private static final String UID = "uid";

	private static final String PWD = "password";

	private static final String ATTRIBUTE = "attribute";

	private static final String SERVICE_FILTER = "servicefilter";

	private static final String OWNER_FILTER = "ownerfilter";

	private static final String CATEGORY = "category";

	private static final String CATEGORY_PROFILE = "categoryprofile";

	private static final String[][] utilParams = new String[][] {
			{ PROFILE_NAME, "No profile specified" },
			{ SERVICE_PROFILE, "No Service profile specified" },
			{ UID, "No uid specified" },
			{ SERVICE_FILTER, "No servicefilter specified" },
			{ OWNER_FILTER, "No ownerfilter specified" },
			{ PWD, "No password specified" }, };

	/**
	 * main method.
	 */
	public static void main(String[] args) {
		run(args, true);
	}

	/**
	 * Run the example.
	 * 
	 * @param args
	 *            Arguments passed in, usually from the command line. See usage
	 *            from more information.
	 * @param verbose
	 *            Should the program print out lots of information.
	 * @return true if run() completes successfully, false otherwise.
	 */
	public static boolean run(String[] args, boolean verbose) {

		Utils utils = null;
		Hashtable<String, Object> arguments = null;
		PlatformContext platform = null;

		try {
			utils = new Utils(utilParams, verbose);
			arguments = utils.parseArgs(args);
		} catch (IllegalArgumentException ex) {
			if (verbose) {
				System.err.println(getUsage(ex.getMessage()));
			}
			return false;
		}

		try {
			String tenantId = utils.getProperty(Utils.TENANT_ID);
			String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);

			platform = utils.getPlatformContext();
			Subject subject = utils.getSubject();

			utils.print("Searching for Owner \n");
			// Use the Search API to locate the Account owner
			String personFilter = (String) arguments.get(OWNER_FILTER);
			SearchMO searchMO = new SearchMO(platform, subject);
			String category = (String) arguments.get(CATEGORY);
			if (category == null || category.trim().equals("")) {
				category = ObjectProfileCategory.PERSON;
			}
			searchMO.setCategory(category);
			String dn = "ou=" + tenantId + "," + ldapServerRoot;
			searchMO.setContext(new CompoundDN(new DistinguishedName(dn)));
			String categoryProfile = (String) arguments.get(CATEGORY_PROFILE);
			if (categoryProfile == null || categoryProfile.trim().equals("")) {
				if (category.equals("BPPerson")) {
					categoryProfile = "BPPerson";
				} else {
					categoryProfile = "Person";
				}
			}
			searchMO.setProfileName(categoryProfile);
			searchMO.setFilter(personFilter);
			SearchResultsMO searchResultsMO = null;
			Collection people = null;
			try {
				searchResultsMO = searchMO.execute();
				people = searchResultsMO.getResults();
				if (people.size() == 0) {
					utils.print("Unable to find account owner. \n");
					return false;
				}
			} finally {
				// close SearchResultsMO
				if(searchResultsMO != null) {
					try {
						searchResultsMO.close();
						searchResultsMO = null;
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			Person owner = (Person) people.iterator().next();

			// create a PersonMO with the owner's DistinguishedName
			PersonMO ownerMO = new PersonMO(platform, subject, owner
					.getDistinguishedName());

			// create required AttributeValues for this
			// type of account
			String userID = (String) arguments.get(UID);
			AttributeValue uidAttr = new AttributeValue(
					Account.ACCOUNT_ATTR_USERID, userID);
			String password = (String) arguments.get(PWD);
			AttributeValue passwordAttr = new AttributeValue(
					Account.ACCOUNT_ATTR_PASSWORD, password);

			utils.print("Searching for Service \n");
			// Use the Search API to locate the the Service instance
			// on which to provision the account
			searchMO.setCategory(ObjectProfileCategory.SERVICE);
			String serviceFilter = (String) arguments.get(SERVICE_FILTER);
			searchMO.setFilter(serviceFilter);
			String serviceProfile = (String) arguments.get(SERVICE_PROFILE);
			searchMO.setProfileName(serviceProfile);
			Collection services = null;
			try {
				searchResultsMO = searchMO.execute();
				services = searchResultsMO.getResults();
				if (services.size() == 0) {
					utils.print("Unable to find Service.\n");
					return false;
				}
			} finally {
				// close SearchResultsMO
				if(searchResultsMO != null) {
					try {
						searchResultsMO.close();
						searchResultsMO = null;
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			Service service = (Service) services.iterator().next();

			ServiceMO serviceMO = new ServiceMO(platform, subject, service
					.getDistinguishedName());

			// Add attributes to a new AttributeValues
			AttributeValues acctAttrs = new AttributeValues();
			acctAttrs.put(uidAttr);
			acctAttrs.put(passwordAttr);

			// include any other attributes specified on the command-line
			Object attrs = arguments.get(ATTRIBUTE);
			if (attrs instanceof Vector) {
				Vector attributes = (Vector) attrs;
				Iterator it = attributes.iterator();
				while (it.hasNext()) {
					acctAttrs.put(Utils
							.createAttributeValue((String) it.next()));
				}
			} else if (attrs instanceof String) {
				String nameValue = (String) attrs;
				acctAttrs.put(Utils.createAttributeValue(nameValue));
			}

			utils.print("Creating AccountManager \n");
			// create an AccountManager
			AccountManager accountManager = new AccountManager(platform,
					subject);
			String profile = (String) arguments.get(PROFILE_NAME);
			// create the Account DirectoryObject
			Account account = new Account(profile);
			account.setAttributes(acctAttrs);

			utils.print("Submitting request to create Account\n");
			// Submit the request to provision the account
			Request request = accountManager.createAccount(ownerMO, serviceMO,
					account, null);

			utils.print("Request submitted. Process Id: " + request.getID());

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (SchemaViolationException e) {
			e.printStackTrace();
		} catch (AuthorizationException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
			e.printStackTrace();
		} finally {
			if(platform != null) {
				platform.close();
				platform = null;
			}
		}

		return true;
	}

	public static String getUsage(String msg) {
		StringBuffer buf = new StringBuffer();
		buf.append("\nprovisionAccount: " + msg + "\n");
		buf.append("usage: provisionAccount -[argument] ? \"[value]\"\n");
		buf.append("\n");
		buf.append("-profile\tObject Profile Name for the Account to ");
		buf.append("provision (e.g., NT40Account)\n");
		buf.append("-serviceprofile\tObject Profile Name for the Service\n");
		buf.append("-uid\t\tUserId to assign to the new Account\n");
		buf.append("-password\tPassword to assign to the new Account\n");
		buf.append("-attribute\tAny attribute name/value pair (e.g., ");
		buf.append("description=NT Account)\n");
		buf.append("-servicefilter\tLdap Filter to search for a Service ");
		buf.append("instance on which to provision the Account\n");
		buf.append("-ownerfiler\tLdap Filter to search for a Person to ");
		buf.append("own the Account\n");
		buf.append("-[category]\tCategory for a Person to own");
		buf.append(" the Account (e.g., BPPerson). Default value=\"Person\"");
		buf.append("\n-[categoryprofile]\tCategory profile name for a Person");
		buf.append(" to own the Account ");
		buf.append(" (e.g., MyPerson(custom class). Default value=\"Person\"");
		buf.append("\n\nArguments enclosed in [] are optional\n\n");
		buf.append("Example: provisionAccount -profile?NT40Account ");
		buf.append("-serviceprofile?NT40Profile ");
		buf.append("-uid?JSmith -password?secret ");
		buf.append("-servicefilter?\"(erservicename=NT Service)\" ");
		buf.append("-ownerfilter?\"(cn=John Smith)\" ");
		buf.append("-category?Person -categoryprofile?MyPerson ");

		return buf.toString();
	}

}
