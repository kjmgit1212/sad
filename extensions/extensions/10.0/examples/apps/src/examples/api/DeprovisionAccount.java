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
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Account;

/**
 * Sample command-line Java class to deprovision an account.
 */
public class DeprovisionAccount {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PROFILE = "profile";

	private static final String ACCOUNT_FILTER = "accountfilter";

	private static final String[][] utilParams = new String[][] {
			{ PROFILE, "No profile specified" },
			{ ACCOUNT_FILTER, "No accountfilter specified" } };

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

			utils.print("Searching for Account to deprovision \n");
			// Use the Search API to locate the Account to deprovision
			String accountProfile = (String) arguments.get(PROFILE);
			String accountFilter = (String) arguments.get(ACCOUNT_FILTER);

			SearchMO searchMO = new SearchMO(platform, subject);
			searchMO.setCategory(ObjectProfileCategory.ACCOUNT);

			String dn = "ou=" + tenantId + "," + ldapServerRoot;
			searchMO.setContext(new CompoundDN(new DistinguishedName(dn)));
			searchMO.setProfileName(accountProfile);
			searchMO.setFilter(accountFilter);
			SearchResultsMO searchResultsMO = null;
			Collection accounts = null;
			try {
				searchResultsMO = searchMO.execute();
				accounts = searchResultsMO.getResults();
				if (accounts.size() == 0) {
					utils.print("No matching account found.");
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
			Account account = (Account) accounts.iterator().next();

			utils.print("Creating AccountMO \n");
			// create an AccountMO
			AccountMO accountMO = new AccountMO(platform, subject, account
					.getDistinguishedName());

			utils.print("Submitting request to deprovision account\n");
			// Submit the request to provision the account
			Request request = accountMO.remove(null);

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
		usage.append("\ndeprovisionAccount: " + msg + "\n");
		usage.append("usage: deprovisionAccount -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-profile\tObject Profile Name for the Account to ");
		usage.append("deprovision (e.g., NT40Account)\n");
		usage.append("-accountfilter\tLdap Filter to search for the Account ");
		usage.append("to deprovision\n");
		usage.append("\n");
		usage.append("Example: deprovisionAccount -profile?NT40Account ");
		usage.append("-accountfilter?\"(eruid=JSmith)\" ");

		return usage.toString();
	}

}
