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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.provisioning.PasswordManager;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Person;

public class SynchPassword {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PASSWORD = "password";

	private static final String OWNER_FILTER = "ownerfilter";

	private static final String[][] utilParams = new String[][] {
			{ PASSWORD, "No password specified" },
			{ OWNER_FILTER, "No ownerfilter specified" }, };

	public static void main(String args[]) {
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
	public static boolean run(String args[], boolean verbose) {

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
			searchMO.setCategory(ObjectProfileCategory.PERSON);
			
			String dn = "ou=" + tenantId + "," + ldapServerRoot;
			searchMO.setContext(new CompoundDN(new DistinguishedName(dn)));
			searchMO.setProfileName("Person");
			searchMO.setFilter(personFilter);
			SearchResultsMO searchResultsMO = null;
			Collection people = null;
			try {
				searchResultsMO = searchMO.execute();
				people = searchResultsMO.getResults();
				if (people.size() == 0) {
					utils.print("Unable to find Person\n");
					return false;
				}
			} finally {
				// close SearchResultsMO
				if(searchResultsMO != null) {
					try {
						searchResultsMO.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			Person owner = (Person) people.iterator().next();

			// create a PersonMO with the owner's DistinguishedName
			PersonMO ownerMO = new PersonMO(platform, subject, owner
					.getDistinguishedName());

			String password = (String) arguments.get(PASSWORD);

			PasswordManager pManager = new PasswordManager(platform, subject);

			utils.print("Submitting request to synch passwords\n");

			Request request = pManager.synchPasswords(ownerMO, password, null);

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
		StringBuffer usage = new StringBuffer();
		usage.append("\nsynchPassword: " + msg + "\n");
		usage.append("usage: synchPassword -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-ownerfilter\tLdap Filter to search for the Person to ");
		usage.append("do password synch on all the accounts the person owns\n");
		usage.append("-password\tNew password\n");
		usage.append("\n");
		usage.append("Example: synchPassword -ownerfilter?\"(cn=Joe Smith)\" ");
		usage.append("-password?\"secret\" ");

		return usage.toString();
	}
}
