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
import java.util.Iterator;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.provisioning.GroupMO;
import com.ibm.itim.apps.provisioning.GroupManager;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.provisioning.ServiceManager;
import com.ibm.itim.apps.search.SearchResultsMO;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.AccessInfo;
import com.ibm.itim.dataservices.model.domain.Group;

/**
 * Example purpose: Delete a group.
 */
public class CreateAccess {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String SERVICE_NAME = "service";

	private static final String PROFILE_NAME = "profile";

	private static final String GROUP_NAME = "groupname";
	
	private static final String ACCESS_NAME = "accessname";
	
	private static final String ACCESS_TYPE = "accesstype";
	
	private static final String ACCESS_OPTION = "accessoption";

	private static final String[][] utilParams = new String[][] {
			{ SERVICE_NAME, "No service name specified." },
			{ PROFILE_NAME, "No group profile specified." },
			{ GROUP_NAME, "No group name specified." },
			{ ACCESS_NAME, "No access name specified." },
			{ ACCESS_TYPE, "No access type specified." },
			{ ACCESS_OPTION, "No access option specified." },

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
		try {
			utils = new Utils(utilParams, verbose);
			arguments = utils.parseArgs(args);
		} catch (IllegalArgumentException ex) {
			if (verbose) {
				System.err.println(getUsage(ex.getMessage()));
			}
			return false;
		}
		PlatformContext platform = null;
		try {
			String serviceName = (String) arguments.get(SERVICE_NAME);
			String profileName = (String) arguments.get(PROFILE_NAME);
			String groupName = (String) arguments.get(GROUP_NAME);
			String accessName = (String) arguments.get(ACCESS_NAME);
			String accessType = (String) arguments.get(ACCESS_TYPE);
			String accessOption = (String) arguments.get(ACCESS_OPTION);
			utils.print("------ serviceName:" + serviceName);
			utils.print("------ profileName:" + profileName);
			utils.print("------ groupName:" + groupName);
			utils.print("------ accessName:" + accessName);
			utils.print("------ accessType:" + accessType);
			utils.print("------ accessOption:" + accessOption);

			int option = getOption(accessOption);
			String tenantId = utils.getProperty(Utils.TENANT_ID);
			String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);
			platform = utils.getPlatformContext();
			Subject subject = utils.getSubject();
			String defaultOrg = DEFAULT_ORG_ID + ",ou=" + tenantId + ","
					+ ldapServerRoot;
			OrganizationalContainerMO containerMO = new OrganizationalContainerMO(
					platform, subject, new DistinguishedName(defaultOrg));

			ServiceManager serviceManager = new ServiceManager(platform,
					subject);
			Collection services = serviceManager.getServices(containerMO,
					serviceName);
			Iterator serviceIte = services.iterator();
			if (serviceIte.hasNext()) {

				ServiceMO serviceMO = (ServiceMO) serviceIte.next();
				utils.print("Found service: "
						+ serviceMO.getDistinguishedName().toString());

				GroupManager groupManager = new GroupManager(platform, subject);
				SearchResultsMO results = new SearchResultsMO(platform, subject);
				utils.print("\n\n****** Search for group... ");
				if (profileName == null || profileName.length() == 0) {
					groupManager.getGroups(serviceMO, groupName, results,
							Locale.getDefault());
				} else {
					groupManager.getGroups(serviceMO, profileName, groupName,
							results, Locale.getDefault());
				}

				Iterator groupIte = results.getResults().iterator();
				if (groupIte.hasNext()) {

					Group group = (Group) groupIte.next();
					utils.print(" ****** Found group: " + group.getId());

					utils.print("\n\n****** Create the access...");
					GroupMO groupMO = new GroupMO(platform, subject, group
							.getDistinguishedName());
					AccessInfo accessInfo = new AccessInfo(accessName, accessType);
					accessInfo.setAccessOption(option);
					utils.print("\n\n****** Set the access...");
					groupMO.setAccessInfo(accessInfo);
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (AuthorizationException e) {
			e.printStackTrace();
		} catch (SchemaViolationException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(platform != null) {
				platform.close();
				platform = null;
			}
		}
		return true;
	}

	public static String getUsage(String msg) {
		StringBuffer usage = new StringBuffer();
		usage.append("\ncreateAccess: " + msg + "\n");
		usage.append("usage: createAccess -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-service\tService name which the group is under\n");
		usage.append("-profile\tProfile name of the group\n");
		usage.append("-groupname\tName of the group to create an access for\n");
		usage.append("-accessname\tName of the access\n");
		usage.append("-accesstype\tType of the access\n");
		usage.append("-accessoption\tStatus of the access\n");
		usage.append("\n");
		usage.append("Example: createAccess -service?\"AIX Service\" -profile?\"PosAixGroupProfile\" ");
		usage.append("-groupname?\"test\" ");
		usage.append("-accessname?\"Test Access\" ");
		usage.append("-accesstype?\"Application\" ");
		usage.append("-accessoption?\"ACCESS_ENABLED\"");
		return usage.toString();
	}
	
	public static int getOption(String option) {
	    int rtn = AccessInfo.NO_ACCESS;
	    
	    if (option.equalsIgnoreCase("ACCESS_ENABLED")) {
	    	rtn = AccessInfo.ACCESS_ENABLED;
	    } else if (option.equalsIgnoreCase("COMMON_ACCESS")) {
	    	rtn = AccessInfo.COMMON_ACCESS;
	    }
	    return rtn;
	}
}
