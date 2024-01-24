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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.policy.Entitlement;
import com.ibm.itim.apps.policy.Membership;
import com.ibm.itim.apps.policy.ProvisioningParameters;
import com.ibm.itim.apps.policy.ProvisioningPolicy;
import com.ibm.itim.apps.policy.ProvisioningPolicyManager;
import com.ibm.itim.apps.policy.ServiceTarget;
import com.ibm.itim.dataservices.model.DistinguishedName;

/**
 * Example purpose: Creating a new provisioning policy in the default org 
 * container with ALL membership and service type entitlement target.
 */
public class CreateProvisioningPolicy {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String POLICY_NAME = "name";
	
	private static final String ENTITLEMENT_SERVICE_PROFILE = "serviceProfile";

	private static final String[][] utilParams = new String[][] {
			{ POLICY_NAME, "No name specified." },
			{ ENTITLEMENT_SERVICE_PROFILE, "No service type specified" }, 
			
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
	        ProvisioningPolicyManager manager = new ProvisioningPolicyManager(platform, subject);
	        //provisioning policy parameters
			String policyName = (String)arguments.get(POLICY_NAME);
			String serviceType = (String)arguments.get(ENTITLEMENT_SERVICE_PROFILE);
			String desc = "This policy is created via example code using provisioning policy API with ALL membership and service type entitlement target ";
			String caption = "This is the caption for this policy";
			String keyword = "policy service type";
			boolean enable = true;
			int scope = ProvisioningPolicy.ONELEVEL_SCOPE;
			int priority = 1000;
			Collection memberships = new ArrayList();
			Collection entitlements = new ArrayList();			
			//memberships 
			Membership member = new Membership(Membership.TYPE_ALL_PERSONS,	"");
			memberships.add(member);			
			//entitlement parameters
			int entitlementType = Entitlement.ENTITLEMENT_TYPE_AUTHORIZED;
			ServiceTarget target = new ServiceTarget(ServiceTarget.TYPE_SERVICE_TYPE, serviceType);
			ProvisioningParameters params = new ProvisioningParameters();
			DistinguishedName processDN = null; //entitlement workflow DN here or null // optional
			Entitlement entitlement = new Entitlement (entitlementType, target, params, processDN);
			entitlements.add(entitlement);			
			ProvisioningPolicy newPolicy = new ProvisioningPolicy (
					policyName,
					caption,
					desc,
					keyword,
					enable,
					scope,
					priority,
					memberships,
					entitlements);
			Request request = manager.createPolicy(containerMO, newPolicy, null);

			utils.print("Request submitted. Process Id: " + request.getID());

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
		usage.append("\ncreateProvisioningPolicy: " + msg + "\n");
		usage.append("usage: createProvisioningPolicy -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-name\t name of the new Provisioning Policy to create\n");
		usage.append("-serviceProfile\tProfile name of the entitlement service type to create\n");
		usage.append("\n");
		usage.append("Example: createProvisioningPolicy -name?\"Test Policy Example\" -serviceProfile?\"LdapProfile\" ");
		return usage.toString();
	}
}
