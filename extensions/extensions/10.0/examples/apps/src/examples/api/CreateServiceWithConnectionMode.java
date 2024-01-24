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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.ejb.service.InvalidOperationException;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.provisioning.ManualWorkOrder;
import com.ibm.itim.apps.provisioning.Participant;
import com.ibm.itim.apps.provisioning.ServiceMO;
import com.ibm.itim.apps.provisioning.ServiceManager;
import com.ibm.itim.apps.provisioning.ManualWorkOrder.OperationType;
import com.ibm.itim.apps.provisioning.Participant.ParticipantType;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.NotificationTemplate;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.dataservices.model.domain.Service.ConnectionMode;

/**
 * Example purpose: Create a service specifying the connection mode along with
 *                  a default ManualWorkOrder.
 */
public class CreateServiceWithConnectionMode {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String PROFILE_NAME = "profile";
	private static final String CONNECTION_MODE = "connectionmode";
	private static final String ATTRIBUTE = "attribute";

	private static final String[][] utilParams = new String[][] {
			{ PROFILE_NAME, "No service profile specified." },
			{ CONNECTION_MODE, "No connection mode specified." },
			{ ATTRIBUTE, "No attribute specified." },
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
			String profileName = (String)arguments.get(PROFILE_NAME);
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
			utils.print("\n\n****** Create a new service...");
			AttributeValues avs = new AttributeValues();
			
			// create the ManualWorkOrder to be created for the service
			// used default Participant and Escalation Participant
			Participant participant = new Participant(ParticipantType.ADMINISTRATOR, false);
			Participant escalationParticipant = new Participant(ParticipantType.ADMINISTRATOR, true);
			HashMap<OperationType, NotificationTemplate> operationNotification = new HashMap<OperationType, NotificationTemplate>();
			for (OperationType opType : ManualWorkOrder.getAllOperations()) {
			    NotificationTemplate blankTemplate = 
			        new NotificationTemplate(opType.toString(), "", "", "");
			    operationNotification.put(opType, blankTemplate);
			    
			}
			long escalationPeriod = 0;
			boolean useDefaultNotification = true;
			ManualWorkOrder manualWorkOrder = new ManualWorkOrder(participant, escalationPeriod, escalationParticipant, useDefaultNotification, operationNotification);

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
			
			String connMode = (String)arguments.get(CONNECTION_MODE);
	        ConnectionMode cm = ConnectionMode.AUTOMATIC;
	        cm = connMode == null ? ConnectionMode.AUTOMATIC : 
	                ConnectionMode.valueOf(connMode.toUpperCase());
			
			Service service = new Service(profileName, avs);
			service.setConnectionMode(cm);
			ServiceMO serviceMO = serviceManager.createService(containerMO, service, manualWorkOrder);
				
			utils.print("\n\nService created successfully: "+ serviceMO.getDistinguishedName());
			
			
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
		} catch(InvalidOperationException e) {
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
		usage.append("\ncreateServiceWithConnectionMode: " + msg + "\n");
		usage.append("usage: createServiceWithConnectionMode -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-profile\tProfile name of the new service\n");
		usage.append("-connectionmode\tConnection mode of the new service\n");
		usage.append("-attribute\tAn attribute name and value\n");
		usage.append("\n");
		usage.append("Example: createServiceWithConnectionMode -profile?\"WinLocalProfile\" -connectionmode?\"MANUAL\" ");
		usage.append("-attribute?\"erservicename=WinLocal\" ");
		usage.append("-attribute?\"erurl=http:\\\\localhost:45580\" ");
		usage.append("-attribute?\"eruid=agent\" ");
		usage.append("-attribute?\"erpassword=agent\"");
		return usage.toString();
	}
}

