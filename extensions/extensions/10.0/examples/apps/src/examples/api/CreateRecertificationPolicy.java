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
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.policy.RecertificationPolicyManager;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.provisioning.ServiceManager;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.policy.IPolicyTarget;
import com.ibm.itim.dataservices.model.policy.ServiceTarget;
import com.ibm.itim.dataservices.model.policy.recert.RecertificationParticipant;
import com.ibm.itim.dataservices.model.policy.recert.RecertificationPolicy;
import com.ibm.itim.scheduling.RecurringTimeSchedule;

/**
 * Example purpose: Create a new recertification policy with target type as
 * account in the default organization container.
 */
public class CreateRecertificationPolicy {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String POLICY_NAME = "name";
	private static final String TARGET_SERVICE_NAME = "targetService";

	private static final String[][] utilParams = new String[][] {
			{ POLICY_NAME, "No name specified." },
			{ TARGET_SERVICE_NAME, "No service name specified" },

	};

	public static void main(String[] args) {
		run(args, true);
	}

	/**
	 * Run the example.
	 * 
	 * @param args
	 *            Arguments passed in, usually from the command line. See usage
	 *            for more information.
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
			RecertificationPolicyManager manager = new RecertificationPolicyManager(
					platform, subject);
			// recertification policy parameters
			String policyName = (String) arguments.get(POLICY_NAME);
			String desc = "This policy is created via example code using recertification policy API.";
			boolean enable = true;
			int scope = RecertificationPolicy.SUBTREE_SCOPE;
			String policyType = RecertificationPolicy.ACCOUNT_TYPE;

			// service target
			Object targetServiceName = arguments.get(TARGET_SERVICE_NAME);
			Collection<IPolicyTarget> targets = new ArrayList<IPolicyTarget>();
			ServiceManager serviceManager = new ServiceManager(platform,
					subject);
			if (targetServiceName instanceof String) {
				String serviceName = (String) targetServiceName;
				Collection collection = serviceManager.getServices(containerMO,
						serviceName);
				if (collection == null || collection.isEmpty()) {
					utils.print("The service named " + targetServiceName
							+ " is not found.");
					return false;
				}
				ServiceMO serviceMO = (ServiceMO) collection.iterator().next();
				ServiceTarget serviceTarget = new ServiceTarget(
						ServiceTarget.TYPE_SERVICE_NAME, serviceMO
								.getDistinguishedName().toString());
				targets.add(serviceTarget);
			}
			if (targetServiceName instanceof Vector) {
				Vector<String> serviceNames = (Vector) targetServiceName;
				Iterator<String> serviceIterator = serviceNames.iterator();
				while (serviceIterator.hasNext()) {
					String serviceName = serviceIterator.next();
					Collection collection = serviceManager.getServices(
							containerMO, serviceName);
					if (collection == null || collection.isEmpty()) {
						utils.print("The service named " + targetServiceName
								+ " is not found.");
						return false;
					}
					ServiceMO serviceMO = (ServiceMO) collection.iterator()
							.next();
					ServiceTarget serviceTarget = new ServiceTarget(
							ServiceTarget.TYPE_SERVICE_NAME, serviceMO
									.getDistinguishedName().toString());
					targets.add(serviceTarget);
				}
			}

			// Recertification Schedule : Daily at 6.30 AM.
			RecurringTimeSchedule schedule = new RecurringTimeSchedule(30, 6,
					0, 0, -1, 0, 0);

			// Reccertification Participant
			RecertificationParticipant participant = RecertificationParticipant
					.create(RecertificationParticipant.TYPE_REQUESTEE, null,
							null);

			// User Type
			String userClass = RecertificationPolicy.ALL_USER_CLASS;

			// Timeout period in days
			int timeoutPeriod = RecertificationPolicy.DEFAULT_PARTICIPANT_RESPONSE_TIMEOUT;

			// Reject Action
			String rejectAction = RecertificationPolicy.SUSPEND_REJECT_ACTION;

			// Timeout Action
			String timeoutAction = RecertificationPolicy.APPROVE_TIMEOUT_ACTION;

			RecertificationPolicy policy = new RecertificationPolicy(
					policyName, scope, enable, policyType, targets, schedule,
					participant, userClass, timeoutPeriod, rejectAction,
					timeoutAction);
			policy.setDescription(desc);

			manager.createPolicy(containerMO, policy);

			utils.print("Request submitted.");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (AuthorizationException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
			e.printStackTrace();
		} finally {
			if (platform != null) {
				platform.close();
				platform = null;
			}
		}

		return true;
	}

	public static String getUsage(String msg) {
		StringBuffer usage = new StringBuffer();
		usage.append("\ncreateRecertificationPolicy: " + msg + "\n\n");
		usage.append("usage: createRecertificationPolicy -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-name\t name of the new Recertification Policy to create\n");
		usage.append("-targetService\t name of the target Service for the new Recertification Policy to create\n");
		usage.append("\n");
		usage.append("Example: createRecertificationPolicy -name?\"Test Policy Example\" -targetService?\"Test Service\" ");
		return usage.toString();
	}
}
