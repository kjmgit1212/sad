/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2005, 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/

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
import com.ibm.itim.apps.lifecycle.LifecycleManager;
import com.ibm.itim.apps.search.SearchMO;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.CompoundDN;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.Account;

public class ChangePasswordOperation {
	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PASSWORD = "password";

	private static final String PROFILE = "profile";

	private static final String ACCOUNT_FILTER = "accountfilter";

	private static final String NOTIFY = "notify";

	private static final String[][] utilParams = new String[][] {
			{ PASSWORD, "No password specified" },
			{ PROFILE, "No profile specified" },
			{ ACCOUNT_FILTER, "No accountfilter specified" } };

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

			utils.print("Searching for account \n");
			// Use the Search API to locate the Account to deprovision
			String accountProfile = (String) arguments.get(PROFILE);
			String accountFilter = (String) arguments.get(ACCOUNT_FILTER);
			String newPassword = (String) arguments.get(PASSWORD);
			String notifyPassword = (String) arguments.get(NOTIFY);
			if (notifyPassword == null || notifyPassword.length() == 0) {
				notifyPassword = "false";
			}

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
					utils.print("Account not found, check search filter");
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
			account.setPassword(newPassword.getBytes());

			utils.print("Test Data:\n ");
			utils.print("    account=" + account.getName());
			utils.print("    profile=" + accountProfile);
			utils.print("    password=" + newPassword);

			LifecycleManager lcMgr = new LifecycleManager(platform, subject);
			Object[] params = { notifyPassword, "0" };  //0 is the default transaction id which will be attached to change password operation
			Request request = lcMgr.executeObjectOperation(account,
					"changePassword", params);

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
		usage.append("\nchangePasswordOperation: " + msg + "\n");
		usage.append("usage: changePasswordOperation -[argument] ? ");
		usage.append("\"[value]\"\n\n");
		usage.append("-profile\tObject Profile Name for the Account to");
		usage.append(" change password (e.g., ITIMAccount)\n");
		usage.append("-accountfilter\tLdap Filter to search for the Account ");
		usage.append("to change password\n");
		usage.append("-password\tNew password\n");
		usage.append("-notifypassword\tFlag to identify whether to notify ");
		usage.append("password(true|false)\n\n");
		usage.append("Example: changePasswordOperation -profile?ITIMAccount ");
		usage.append("-accountfilter?\"(eruid=JSmith)\" ");
		usage.append("-password?\"secret\" ");
		usage.append("-notifypassword?\"true\" ");

		return usage.toString();
	}
}
