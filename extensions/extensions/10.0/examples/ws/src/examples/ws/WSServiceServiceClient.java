/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/
package examples.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.ibm.itim.ws.model.WSAccount;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSService;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.services.WSServiceService;
import com.ibm.itim.ws.services.WSSessionService;


public class WSServiceServiceClient extends GenericWSClient {

	
	private static final String PARAM_NOTIFY_BY_MAIL = "notifyByMail";
	private static final String PARAM_SYNC_PASSWORD = "syncPassword";
	private static final String PARAM_DATE = "date";
	private static final String PARAM_TENANT_ID = "tenantId";
	private static final String PARAM_NEW_PASSWORD = "newPassword";
	private static final String PARAM_RESTORE_ACCOUNTS = "restoreAccounts";
	private static final String PARAM_ITIM_CREDENTIAL = "credential";
	private static final String PARAM_ITIM_PRINCIPAL = "principal";
	private static final String PARAM_PERSON_FILTER = "filter";
	private static final String PARAM_ORG_CONTAINER = "orgContainer";
	private static final String PARAM_PROFILE_NAME  = "profileName";
	private static final String PARAM_SERVICENAME =  "erservicename";	
	static final String OPERATION_NAME = "operationName";
	
	enum WSSERVICESERVICE_OPERATIONS {

		GETSERVICES {

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				WSServiceService wsService = getServiceService();
				List<WSService> listWsService = wsService.getServices(session);
				for (Iterator iterator = listWsService.iterator(); iterator
						.hasNext();) {
					WSService service = (WSService) iterator.next();
					Utils.printMsg(WSServiceServiceClient.class.getName(),
							"execute", this.name(), "\n Service Name : "
									+ service.getName() + " \n "
									+ "\n Service Profile Name : "
									+ service.getProfileName() + " \n "
									+ "\n Service DN : " + service.getItimDN());

				}

				return true;
			}

			@Override
			String getUsage(String errMessage) {

				return "usage: wsServiceService -operationName?getServices -credential?password -principal?username"
				   + "\n for example: wsServiceService -operationName?getServices -credential?secret -principal?\"itim manager\" ";

			}
		},
		
		GETACCOUNTSFORSERVICE {

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				
				WSServiceService wsService = getServiceService();
				
				String serviceName = (String)mpParams.get("serviceName") ;
				String filter="(erservicename="+serviceName+")";
				
				String serviceDN = wsService.searchServices(session, null, filter).get(0).getItimDN();
				
				List<WSAccount> listWsAccount = wsService.getAccountsForService(session,serviceDN , (String)mpParams.get("accountID"));
				
                for (Iterator iterator = listWsAccount.iterator(); iterator
						.hasNext();) {
					WSAccount account = (WSAccount) iterator.next();
					
					Utils.printMsg(WSServiceServiceClient.class.getName(), "execute", this.name(), 
							"\n Account Name : " + account.getName() + " \n " +
							"\n Account Profile Name : " + account.getProfileName() + " \n " +
							"\n Account DN : " + account.getItimDN());
					
				}
				
				return true;

			}

			@Override
			String getUsage(String errMessage) {
                
				return "usage: wsServiceService -operationName?getAccountsForService -credential?password -principal?username -serviceName?servicedn -accountID?accountID"
				   + "\n for example: wsServiceService -operationName?getAccountsForService -credential?secret -principal?\"itim manager\" -serviceName?\"Payroll Service\" -accountID?jhill";
				

			}
		},
		
		SEARCH {

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				
				WSServiceService wsService = getServiceService();
				List<WSService> listWsService = wsService.searchServices(session, null, (String)mpParams.get("filter"));
				for (Iterator iterator = listWsService.iterator(); iterator
						.hasNext();) {
					WSService service = (WSService) iterator.next();
					Utils.printMsg(WSServiceServiceClient.class.getName(),
							"execute", this.name(), "\n Service Name : "
									+ service.getName() + " \n "
									+ "\n Service Profile Name : "
									+ service.getProfileName() + " \n "
									+ "\n Service DN : " + service.getItimDN());

				}

				//Utils.printMsg(WSPersonServiceClient.class.getName(),"execute", this.name(),"Services details" + listWsService.(.[]) collection.toArray(new .[collection.size()])  );
				return true;

			}

			@Override
			String getUsage(String errMessage) {

				return "usage: wsServiceService -operationName?search -credential?password -principal?username -filter?ldapFilter"
				   + "\n for example: wsServiceService -operationName?getAccountsForService -credential?secret -principal?\"itim manager\" -filter?(erservicename=ITIM Service)";
				
				
			}
		},
		
		GETSERVICEFORACCOUNT {
			boolean execute(Map<String, Object> mpParams) throws Exception {

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				String accountID = (String) mpParams.get("accountID");
				WSSession session = loginIntoITIM(principal, credential);

				WSServiceService wsService = getServiceService();
				WSAccountService proxy = getAccountService();
				WSSearchArguments searchArgs = new WSSearchArguments();
				searchArgs.setFilter("eruid=" + accountID);
				String accountDN = proxy.searchAccounts(session, searchArgs)
						.get(0).getItimDN();
				WSService service = wsService.getServiceForAccount(session,
						accountDN);

				Utils.printMsg(WSServiceServiceClient.class.getName(),
						"execute", this.name(), "\n Service Name : "
								+ service.getName() + " \n "
								+ "\n Service Profile Name : "
								+ service.getProfileName() + " \n "
								+ "\n Service DN : " + service.getItimDN());
				return true;

			}

			@Override
			String getUsage(String errMessage) {

				return "usage: wsServiceService -operationName?search -credential?password -principal?username -accountID?accountID"
				   + "\n for example: wsServiceService -operationName?getAccountsForService -credential?secret -principal?\"itim manager\" -accountID?ITIM Manager";
				

			}
		},
		
		
		CREATESERVICE {
			
			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				mpParams.remove(OPERATION_NAME);
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				mpParams.remove(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				mpParams.remove(PARAM_ITIM_CREDENTIAL);
				// PREPARING INPUT PARAMETERS
				String profileName = (String) mpParams.get(PARAM_PROFILE_NAME);
				mpParams.remove(PARAM_PROFILE_NAME);
				if (profileName == null) {
					Utils.printMsg(WSServiceServiceClient.class.getName(),
							"execute", this.name(),
							"No Filter parameter passed for the profile name.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				WSOrganizationalContainer wsContainer = null;
				String containerName = (String) mpParams
						.get(PARAM_ORG_CONTAINER);
				mpParams.remove(PARAM_ORG_CONTAINER);
				if (containerName != null) {
					List<WSOrganizationalContainer> lstWSOrgContainers = wsOrgContainerService
							.searchContainerByName(wsSession, null,
									"Organization", containerName);
					if (lstWSOrgContainers != null
							&& !lstWSOrgContainers.isEmpty()) {
						wsContainer = lstWSOrgContainers.get(0);
					} else {
						System.out.println("No container found matching "
								+ containerName);
						return false;
					}
				} else {
					System.out
							.println("No Filter parameter passed for the container name.");
					return false;
				}
				String containerDN = wsContainer.getItimDN();
				// The remaining input parameters represents service attributes.
				// They will be passed
				// to the underlying web service as a list of WSAttributes.
				List<WSAttribute> serviceAttributes = getWSAttributesList(
						mpParams, "CREATESERVICE", this.name());

				WSServiceService wsServiceServiceObj = getServiceService();
				String nameOfCreatedService = wsServiceServiceObj
						.createService(wsSession, containerDN, profileName,
								serviceAttributes);
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						"execute", this.name(),
						"The Create Service request submitted successfully. Service Name: : "
								+ nameOfCreatedService);
				return true;
			}
			
			@Override
			String getUsage(String errMessage) {

				return "usage: wsServiceService -operationName?createService -principal?username -credential?password -orgContainer?<MyContainer> -profileName?<WinLocalProfile> [[-<attribute1>?<value1>] [-<attribute2>?<value2>] ...]"
				   + "\n for example : wsServiceService -operationName?createService -principal?\"itim manager\" -credential?secret  "
				   + "\n -orgContainer?MyContainer -profileName?WinLocalProfile -erservicename?MyWinLocalTestService" 
				   + "\n -erservicename?MyWinLocalTestService -erUrl?http://hostname:45580 -erUid?agent -erPassword?agent -description?\"Description for winLocal service\"";

			}
			
		},
		
		MODIFYSERVICE {
			
			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				mpParams.remove(OPERATION_NAME);
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				mpParams.remove(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				mpParams.remove(PARAM_ITIM_CREDENTIAL);
				String serviceName = (String) mpParams.get(PARAM_SERVICENAME);
				mpParams.remove(PARAM_SERVICENAME);
				if (serviceName == null) {
					Utils.printMsg(WSServiceServiceClient.class.getName(),
							"execute", this.name(),
							"No filter paramter passed for the service name.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
				WSSession wsSession = loginIntoITIM(principal, credential);

				WSServiceService wsService = getServiceService();
				String filter = "(erservicename=" + serviceName + ")";
				String serviceDN = wsService.searchServices(wsSession, null,
						filter).get(0).getItimDN();
				// The remaining input parameters represents service attributes.
				// They will be passed
				// to the underlying web service as a list of WSAttributes.
				List<WSAttribute> serviceModifiedAttributes = Utils.getWSAttributesList(
						mpParams, "MODIFYSERVICE");

				wsService.modifyService(wsSession, serviceDN,
						serviceModifiedAttributes);
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						"execute", this.name(),
						"The Modify Service request submitted successfully. Service Name: "
								+ serviceName);
				return true;
			}
			
			@Override
			String getUsage(String errMessage) {

				return " usage: wsServiceService -operationName?modifyService -principal?username -credential?password -serviceName?<NameOfServiceToModify> [[-<attribute1>?<value1>] [-<attribute2>?<value2>] ...]"
				   + "\n for example : wsServiceService -operationName?modifyService -principal?\"itim manager\" -credential?secret "
				   + "\n -serviceName?WinLocalService -description?\"This is modified description\"";
				
			}
		}
	
		;
		abstract boolean execute(Map<String, Object> mpParams) throws Exception;

		/**
		 * The method will be executed in case there is an exception occurred
		 * while executing the method. Since the usage of each of the methods
		 * will be different we to make sure that its taken care of.
		 * 
		 * @param errMessage
		 * @return
		 */
		abstract String getUsage(String errMessage);


		WSSession loginIntoITIM(String principal, String credential)
				throws WSInvalidLoginException, WSLoginServiceException {

			WSSessionService proxy = getSessionService();
			WSSession session = proxy.login(principal, credential);
			if (session != null)
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						"loginIntoITIM", this.name(), "session id is "
								+ session.getSessionID());
			else
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						"loginintoITIM", this.name(), "Invalid session");

			return session;
		}
		
		List<WSAttribute> getWSAttributesList(Map<String, Object> mpParams, String methodName, String enumName){
			java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
			WSAttribute wsAttr = null;
			List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();

			while (itrParams.hasNext()) {
				String paramName = itrParams.next();
				Object paramValue = mpParams.get(paramName);
				wsAttr = new WSAttribute();
				wsAttr.setName(paramName);
				ArrayOfXsdString arrStringValues = new ArrayOfXsdString();

				if (paramValue instanceof String) {
					arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
				} else if (paramValue instanceof Vector) {
					Vector paramValues = (Vector) paramValue;
					arrStringValues.getItem().addAll(paramValues);
				} else {
					Utils.printMsg(WSServiceServiceClient.class.getName(), methodName, enumName, "The datatype of parameter '" + paramName + "' is not supported.");
				}
				wsAttr.setValues(arrStringValues);
				lstWSAttributes.add(wsAttr);
			}
			
			return lstWSAttributes;
		}

	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
				operationName = operationName.toUpperCase();
				for(WSSERVICESERVICE_OPERATIONS operation : WSSERVICESERVICE_OPERATIONS.values()){
					if(operation.name().equalsIgnoreCase(operationName)){
						try{
							retCode = operation.execute(mpParams);
							break;
						}catch(Exception e){
							e.printStackTrace();
							operation.getUsage(e.getLocalizedMessage());
						}
					}
				}
		}
		return retCode;
   }
	@Override
	public String getUsage(String errMessage) {
		// TODO Auto-generated method stub
		return null;
	}
	public static void main(String[] args) {
		WSServiceServiceClient client = new WSServiceServiceClient();
		client.run(args, true);
	}

}
