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
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.policy.RecertificationPolicyMO;
import com.ibm.itim.apps.policy.RecertificationPolicyManager;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.policy.recert.RecertificationPolicy;
import com.ibm.itim.scheduling.RecurringTimeSchedule;

/**
 * Example purpose: Change a recertification policy schedule in the default organization
 * container.
 */
public class ChangeRecertificationPolicy {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String POLICY_NAME = "name";
	private static final String MINUTE = "minute";
	private static final String HOUR = "hour";
	private static final String DAY_OF_MONTH = "dayOfMonth";
	private static final String MONTH = "month";
	private static final String DAY_OF_WEEK = "dayOfWeek";
	private static final String DAY_OF_QUARTER = "dayOfQuarter";
	private static final String DAY_OF_SEMI_ANNUAL = "dayOfSemiAnnual";

	private static final String[][] utilParams = new String[][] {
			{ POLICY_NAME, "No name specified." },
			{ MINUTE, "No minute specified." }, { HOUR, "No hour specified" },
			{ DAY_OF_MONTH, "No day of month specified" },
			{ MONTH, "No month specified" },
			{ DAY_OF_WEEK, "No day of week specified" }, };

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
			Collection collection = manager.getPoliciesByName(containerMO,
					policyName, true);
			if (collection == null || collection.isEmpty()) {
				utils
						.print("The policy named " + policyName
								+ " is not found.");
				return false;
			}
			RecertificationPolicyMO policyMO = (RecertificationPolicyMO) collection
					.iterator().next();
			RecertificationPolicy policy = policyMO.getData();

			int minute = Integer.parseInt((String) arguments.get(MINUTE));
			int hour = Integer.parseInt((String) arguments.get(HOUR));
			int dayOfMonth = Integer.parseInt((String) arguments
					.get(DAY_OF_MONTH));
			int month = Integer.parseInt((String) arguments.get(MONTH));
			int dayOfWeek = Integer.parseInt((String) arguments
					.get(DAY_OF_WEEK));
			int dayOfQuarter = 0;
			if (arguments.get(DAY_OF_QUARTER) != null) {
				dayOfQuarter = Integer.parseInt((String) arguments
						.get(DAY_OF_QUARTER));
			}
			int dayOfSemiAnnual = 0;
			if (arguments.get(DAY_OF_SEMI_ANNUAL) != null) {
				dayOfSemiAnnual = Integer.parseInt((String) arguments
						.get(DAY_OF_SEMI_ANNUAL));
			}

			RecurringTimeSchedule schedule = new RecurringTimeSchedule(minute,
					hour, dayOfMonth, month, dayOfWeek, dayOfQuarter,
					dayOfSemiAnnual);

			policy.setScheduleMode(RecertificationPolicy.CALENDAR_SCHEDULING_MODE);
			policy.setSchedule(schedule);

			policyMO.update(policy);

			utils.print("Request submitted.");
		} catch (NumberFormatException e) {
			e.printStackTrace();
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
		usage.append("\nchangeRecertificationPolicy: " + msg + "\n\n");
		usage.append("usage: changeRecertificationPolicy -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-name\tthe name of the recertification policy to change\n");
		usage.append("-minute\tthe minute of the schedule\n");
		usage.append("-hour\tthe hour of the schedule\n");
		usage.append("-dayOfMonth\tthe day of month of the schedule\n");
		usage.append("-month\tthe month of the schedule\n");
		usage.append("-dayOfWeek\tthe day of week of the schedule\n");
		usage.append("-[dayOfQuarter]\tthe day of quarter of the schedule. Default value=0\n");
		usage.append("-[dayOfSemiAnnual]\tthe day of semi-annual of the schedule. Default value=0\n");
		usage.append("\nArguments enclosed in [] are optional\n");
		usage.append("");
		usage.append("\n");
		usage.append("Example : changeRecertificationPolicy -name?\"Test Policy Example\" -minute?\"30\" -hour?\"6\" -month?\"4\" -dayOfMonth?\"1\" -dayOfWeek?\"0\"");
		return usage.toString();
	}
}
