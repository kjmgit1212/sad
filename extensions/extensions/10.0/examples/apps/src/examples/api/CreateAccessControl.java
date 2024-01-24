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
import java.util.Hashtable;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.acl.AccessControlListManager;
import com.ibm.itim.dataservices.model.AccessRight;
import com.ibm.itim.dataservices.model.AttributeRight;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.Permission;

public class CreateAccessControl {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String TARGET = "target";

	private static final String SCOPE = "scope";

	private static final String PRINCIPAL = "principal";
	
	private static final String[][] utilParams = new String[][] {
			{ TARGET, "No ACI target specified." },
			{ SCOPE, "No ACI scope specified." },
			{ PRINCIPAL, "No ACI principal specified" }, };
	
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

			String defaultOrg = DEFAULT_ORG_ID + ",ou=" + tenantId + ","
					+ ldapServerRoot;

			DistinguishedName orgDN = new DistinguishedName(defaultOrg);

			utils.print("Creating a new AccessRight \n");
			AccessRight accessRight = new AccessRight();

			// give the ACI a unique name
			accessRight.setName("ACI For Account Category, Grant All to Supervisor");

			// set the protection category, or ACI target
			String target = (String) arguments.get(TARGET);
			accessRight.setTarget(target);

			// set the ACI's scope
			String scope = (String) arguments.get(SCOPE);
			accessRight.setScope(scope);
			
			// add a principal to the ACI
			String principal = (String) arguments.get(PRINCIPAL);
			accessRight.getPrincipals().add(principal);
			
			// create an AttributeRight 
			AttributeRight attributeRight = new AttributeRight();

			// grant read and write to all attributes
			attributeRight.setOperations(AttributeRight.RW);
			attributeRight.setForAllAttributes(true);

			// create a Permission that grants the "add" class operation
			Permission permission = new Permission();
			permission.getClassRights().add("add");
			permission.setAction(Permission.GRANT);
			
			// attach the new AttributeRight to the Permission
			permission.getAttributeRights().add(attributeRight);
			
			// attach the new Permission to the AccessRight
			accessRight.getPermissions().add(permission);
			
			// finally, add this ACI to the root organization using the AccessControlListManager
			AccessControlListManager manager = new AccessControlListManager(platform, subject, orgDN);

			utils.print("Adding the new AccessRight to the default Organization\n");
			
			manager.addAccessRight(accessRight);

			utils.print("AccessRight successfully added\n");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
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
		usage.append("\ncreateAccessControl: " + msg + "\n");
		usage.append("usage: createAccessControl -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-target\tThe name of an LDAP objectclass\n");
		usage.append("-scope\tOne of single or subtree\n");
		usage.append("-principal\tOne of self, supervisor, or administrator\n");
		usage.append("\n");
		usage.append("Example: createAccessControl -target?\"erAccountItem\" ");
		usage.append("-scope?\"subtree\" ");
		usage.append("-principal?\"supervisor\"");

		return usage.toString();
	}
}
