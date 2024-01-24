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
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Account;

/**
 * Sample command-line Java class to change an account.
 */
public class ChangeAccount {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PROFILE = "profile";

	private static final String ACCOUNT_FILTER = "accountfilter";

	private static final String ATTRIBUTE = "attribute";

	private static final String[][] utilParams = new String[][] {
			{ PROFILE, "No profile specified" },
			{ ACCOUNT_FILTER, "No accountfilter specified" },
			{ ATTRIBUTE, "No attribute specified" } };
	
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

			utils.print("Searching for Account \n");
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

			// set attributes specified on the command-line
			Object attrs = arguments.get(ATTRIBUTE);
			if (attrs instanceof Vector) {
				Vector attributes = (Vector) attrs;
				Iterator it = attributes.iterator();
				while (it.hasNext()) {
					account.setAttribute(Utils.createAttributeValue((String) it
							.next()));
				}
			} else if (attrs instanceof String) {
				String nameValue = (String) attrs;
				account.setAttribute(Utils.createAttributeValue(nameValue));
			}

			// Submit the request to change the account
			utils.print("Submitting request to change Account\n");

			Request request = accountMO.update(account, null);

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
		usage.append("\nchangeAccount: " + msg + "\n");
		usage.append("usage: changeAccount -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-profile\tObject Profile Name for the Account to change");
		usage.append(" (e.g., NT40Account)\n");
		usage.append("-accountfilter\tLdap Filter to search for the Account ");
		usage.append("to change\n");
		usage.append("-attribute\tAttribute name and new value\n");
		usage.append("\n");
		usage.append("Example: changeAccount -profile?NT40Account ");
		usage.append("-accountfilter?\"(eruid=JSmith)\" ");
		usage.append("-attribute?\"description=NT Account\" ");

		return usage.toString();
	}

}
