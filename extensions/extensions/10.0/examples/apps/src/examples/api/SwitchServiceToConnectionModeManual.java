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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

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
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.NotificationTemplate;

/**
 * Example purpose: Switch the connection mode of a service to manual.
 */
public class SwitchServiceToConnectionModeManual {

	private static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Command line argument names (prefixed by "-")
	 */
	private static final String SERVICE_NAME = "service";

	private static final String[][] utilParams = new String[][] {
            { SERVICE_NAME, "No service name specified." },
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
			utils.print("------ serviceName:"+ serviceName);
			
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
				
				utils.print("\n\n****** Change the connection mode of the service to manual and assign a default work order...");
				
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

	            serviceMO.switchToManualMode(manualWorkOrder);
				
				utils.print("\n\nService connection mode updated to manual successfully: "+ serviceMO.getDistinguishedName());
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
		usage.append("\nswitchServiceToConnectionModeManual: " + msg + "\n");
		usage.append("usage: switchServiceToConnectionModeManual -[argument] ? \"[value]\"\n");
		usage.append("\n");
		usage.append("-service\tName of the service to be updated\n");
		usage.append("\n");
		usage.append("Example: switchServiceToConnectionModeManual -service?\"WinLocal\"");
		return usage.toString();
	}
}

