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
import java.util.Vector;

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
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.Group;

/**
 * Example purpose: Create a group.
 */
public class CreateGroup {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String SERVICE_NAME = "service";
	private static final String PROFILE_NAME = "profile";
	private static final String ATTRIBUTE = "attribute";

	private static final String[][] utilParams = new String[][] {
			{ SERVICE_NAME, "No service name specified." },
			{ PROFILE_NAME, "No group profile specified." },
			{ ATTRIBUTE, "No attribute specified" },
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
			String serviceName = (String)arguments.get(SERVICE_NAME);
			String profileName = (String)arguments.get(PROFILE_NAME);
			utils.print("------ serviceName:"+ serviceName);
			utils.print("------ profileName:"+ profileName);
			
			String tenantId = utils.getProperty(Utils.TENANT_ID);
			String ldapServerRoot = utils.getProperty(Utils.LDAP_SERVER_ROOT);
			platform = utils.getPlatformContext();
			Subject subject = utils.getSubject();
			String defaultOrg = DEFAULT_ORG_ID + ",ou=" + tenantId + ","
					+ ldapServerRoot;
			OrganizationalContainerMO containerMO = new OrganizationalContainerMO(
					platform, subject, new DistinguishedName(defaultOrg));
			
			ServiceManager serviceManager = new ServiceManager(platform, subject);
			Collection services = serviceManager.getServices(containerMO, serviceName);
			Iterator serviceIte = services.iterator();
			if (serviceIte.hasNext()) {
			
				ServiceMO serviceMO = (ServiceMO)serviceIte.next();
				utils.print("Found service: "+serviceMO.getDistinguishedName().toString());
				
				utils.print("\n\n****** Create a new group...");
				GroupManager groupManager = new GroupManager(platform, subject);
				
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

				Group group = new Group(profileName, avs);
				GroupMO groupMO = groupManager.createGroup(serviceMO, group);
				
				utils.print("\n\nGroup created successfully: "+ groupMO.getDistinguishedName());
			}
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (AuthorizationException e) {
			e.printStackTrace();
		}  catch (SchemaViolationException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
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
		usage.append("\ncreateGroup: " + msg + "\n");
		usage.append("usage: createGroup -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-service\tService name the new group to be created\n");
		usage.append("-profile\tProfile name of the new group\n");
		usage.append("-attribute\tAn attribute name and value\n");
		usage.append("\n");
		usage.append("Example: createGroup -service?\"AIX Service\" -profile?\"PosAixGroupProfile\" ");
		usage.append("-attribute?\"erPosixGroupName=test\" ");
		usage.append("-attribute?\"erPosixGroupId=121\"");
		return usage.toString();
	}
}