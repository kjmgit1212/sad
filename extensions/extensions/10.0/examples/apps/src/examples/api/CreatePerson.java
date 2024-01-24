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
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.Request;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.identity.PersonManager;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.Person;

public class CreatePerson {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PROFILE_NAME = "profile";

	private static final String ATTRIBUTE = "attribute";

	private static final String[][] utilParams = new String[][] {
			{ PROFILE_NAME, "No person profile specified." },
			{ ATTRIBUTE, "No attribute specified" }, };

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

			utils.print("Searching for Person \n");
			// Use the Search API to locate the Account owner
			String profileName = (String) arguments.get(PROFILE_NAME);

			String defaultOrg = DEFAULT_ORG_ID + ",ou=" + tenantId + ","
					+ ldapServerRoot;

			OrganizationalContainerMO containerMO = new OrganizationalContainerMO(
					platform, subject, new DistinguishedName(defaultOrg));

			AttributeValues avs = new AttributeValues();

			// include any other attributes specified on the command-line
			Object attrs = arguments.get(ATTRIBUTE);
			if (attrs instanceof Vector) {				
				Vector attributes = (Vector) attrs;
				Map<String, AttributeValue> AttributeValueMap= Utils.createAttributeValueMap(attributes);
				Set<String> set=AttributeValueMap.keySet();
            		for (String str : set) {
					avs.put(AttributeValueMap.get(str));
				}						
			} else if (attrs instanceof String) {				
				String nameValue = (String) attrs;
				avs.put(Utils.createAttributeValue(nameValue));
			}
			
			utils.print("Submitting request to create Person\n");

			Person person = new Person(profileName);
			person.setAttributes(avs);
			PersonManager manager = new PersonManager(platform, subject);
			Request request = manager.createPerson(containerMO, person,
					null);

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
		usage.append("\ncreatePerson: " + msg + "\n");
		usage.append("usage: createPerson -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-profile\tProfile name of the new Person to create\n");
		usage.append("-attribute\tAn attribute name and value\n");
		usage.append("\n");
		usage.append("Example: createPerson -profile?\"Person\" ");
		usage.append("-attribute?\"cn=Jenny Brown\" ");
		usage.append("-attribute?\"sn=Brown\"");

		return usage.toString();
	}
}
