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
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.system.SystemRoleMO;
import com.ibm.itim.apps.system.SystemRoleManager;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.system.SystemRole;

/**
 * Example purpose: Create the named ITIM group with the attributes supplied in
 * the default org container.
 */
public class CreateItimGroup {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String ITIM_GROUP_NAME = "name";
	private static final String ITIM_GROUP_CAT = "category";
	private static final String ATTRIBUTE = "attribute";

	private static final String[][] utilParams = new String[][] {
			{ ITIM_GROUP_NAME, "No ITIM group name specified." },
			{ ITIM_GROUP_CAT, "No ITIM group category specified."},
	};

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
			OrganizationalContainerMO containerMO = new OrganizationalContainerMO(
					platform, subject, new DistinguishedName(defaultOrg));
			
			String itimGroupName = (String)arguments.get(ITIM_GROUP_NAME);
			int category = Integer.parseInt((String)arguments.get(ITIM_GROUP_CAT));
			
			AttributeValues avs = new AttributeValues();
			// include any other attributes specified on the command-line
			Object attrs = arguments.get(ATTRIBUTE);
			if (attrs instanceof Vector) {
				Vector attributes = (Vector) attrs;
				Iterator it = attributes.iterator();
				while (it.hasNext()) {
					avs.put(Utils.createAttributeValue((String) it.next()));
				}
			} else if (attrs instanceof String) {
				String nameValue = (String) attrs;
				avs.put(Utils.createAttributeValue(nameValue));
			}

			utils.print("Submitting request to create ITIM Group\n");
			SystemRole itimGroup = new SystemRole(avs);
			itimGroup.setName(itimGroupName);
			itimGroup.setCategory(category);
			SystemRoleManager manager = new SystemRoleManager(platform, subject);
			SystemRoleMO itimGroupMO = manager.createRole(containerMO, itimGroup);
			utils.print("ITIM group is created. The ITIM group DN is: " + itimGroupMO.getDistinguishedName());
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
		usage.append("\ncreateItimGroup: " + msg + "\n");
		usage.append("usage: createItimGroup -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-name\tITIM group name of the new group to create\n");
		usage.append("-category\tITIM group level of 1=End User, 2=Supervisor, 3=System Admin, 4=Helpdesk, 5=Service Owner, 6=Auditor\n");		
		usage.append("-attribute\tAn attribute name and value\n");
		usage.append("\n");
		usage.append("Example: createItimGroup -name?\"ITIM Group example\" -category?\"1\" ");
		usage.append("-attribute?\"description=This is test ITIM group created through example.\" ");
		return usage.toString();
	}
}
