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

package examples.ws;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibm.itim.ws.model.WSAccessEntitlement;
import com.ibm.itim.ws.model.WSAccount;
import com.ibm.itim.ws.model.WSNewUserAccess;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.model.WSUserAccess;
import com.ibm.itim.ws.services.WSAccessService;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.services.WSSessionService;


/**
 * @author administrator
 * 
 */

public class WSAccessServiceClient extends GenericWSClient {
	
	
	private static final String PARAM_ITIM_CREDENTIAL = "credential";
	private static final String PARAM_ITIM_PRINCIPAL = "principal";
	private static final String PARAM_PERSON_SEARCH_FILTER = "person";
	private static final String PARAM_CONTAINER = "container";
	private static final String PARAM_ACCESS_NAME = "accessName";
	private static final String PARAM_ACCESS_TYPE = "accessType";
	private static final String PARAM_ACCESS_INFO = "accessInfo";
	private static final String PARAM_USER_ID = "userID";
	private static final String PARAM_PASSWORD = "password";
	
	
	// TODO: For usage we will create an enum which will have the getUsage
	// method implementation in it.
	enum WSACCESSSERVICE_OPERATIONS {
		
		
		CREATEACCESS {
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				
					
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				String personSearchFilter = (String)mpParams.get(PARAM_PERSON_SEARCH_FILTER);
				String accessName = (String)mpParams.get(PARAM_ACCESS_NAME);
				String userID = (String)mpParams.get(PARAM_USER_ID);
				String password = (String)mpParams.get(PARAM_PASSWORD);
				if(personSearchFilter == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "CREATEACCESS", "No Filter parameter passed to search for the person.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				WSAccessService accessService = getAccessService();
				WSAccountService accountService = getAccountService();
				
				//Call searchPerson API to search for the person using specified filter
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, 
												personSearchFilter, null);
				String personDN;
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//If there are more than one person matching given criteria then select the first one.
					personDN = lstWSPersons.get(0).getItimDN();
				}
				else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "CREATEACCESS", "No person found matching the filter : " + personSearchFilter);
					return false;
				}

				// Call searchAvailableAccessEntitlements to search for the available accesses
				List<WSAccessEntitlement> listWSAccessEntitlement = accessService.
						searchAvailableAccessEntitlements(wsSession, null, personDN, null, accessName);
				WSAccessEntitlement wsAccessEnt;
				if(listWSAccessEntitlement != null && listWSAccessEntitlement.size() > 0){
					//select the first access entitlement from the returned list.
					wsAccessEnt = listWSAccessEntitlement.get(0);
				}
				else{
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "CREATEACCESS", "No Access entitlements found matching the access name : " + accessName);
					return false;
				}
				String accountDN = null;
				if (wsAccessEnt.getServiceDN() != null && !wsAccessEnt.getServiceDN().isEmpty()) {
					if (userID != null & !userID.isEmpty()) {
						// search for the accountDN if its an existing account
						WSSearchArguments searchArgs = new WSSearchArguments();
						searchArgs.setFilter("(&(erservice="
								+ wsAccessEnt.getServiceDN() + ")(eruid=" + userID + "))");
						System.out.println(searchArgs.getFilter());
						List<WSAccount> listAccounts = accountService.searchAccounts(wsSession, 
														searchArgs);
						if (listAccounts.size() != 0) {
							accountDN = listAccounts.get(0).getItimDN();
						}
						else
							Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "CREATEACCESS", "No accounts found for a given user. UserID : "+ userID);
					}
					else
						Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "CREATEACCESS", "User ID parameter is not specified.");
				}
				else
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "CREATEACCESS", "ServiceDN information is not available on accessEntitlement object");
				List<WSNewUserAccess> 	wsNewUserAccessLst = new ArrayList<WSNewUserAccess> ();
				WSNewUserAccess wnua = new WSNewUserAccess();
				wnua.setUserId(userID);
				wnua.setPassword(password);
				wnua.setAccountDN(accountDN);
				wnua.setOwnerDN(personDN);
				
				wsNewUserAccessLst.add(wnua);
				
				List<WSRequest> listReq = accessService.createAccess(wsSession, wsAccessEnt, 
											wsNewUserAccessLst, null);
				System.out.println("Create access request submitted successfully.");
				Utils.printWSRequestDetails("create Access", listReq.get(0));
				return true;
				
			}

			public String getUsage(String errMsg) {
				return "usage: wsAccessServiceClient -operationName?createAccess -principal?<username> -credential?<password> -person?\"(cn=a*)\" -accessName?<access name> -userID?<user id> -password?<password>"+
						"\n for example: wsAccessServiceClient -operationName?createAccess" +
						" -principal?\"itim manager\" -credential?secret -person?\"(cn=a*)\"" +
						" -accessName?GroupAccess -userID?user1 -password?Passw0rd";
			}
		},
			
		GETACCESSES{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				String personSearchFilter = (String)mpParams.get(PARAM_PERSON_SEARCH_FILTER);
				String accessName = (String)mpParams.get(PARAM_ACCESS_NAME);
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				WSAccessService accessService = getAccessService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, 
														personSearchFilter, null);
				String personDN = null;
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//If there are more than one person matching given criteria then select the first one.
					personDN = lstWSPersons.get(0).getItimDN();
				}
				else{
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "GETACCESSES", "No person found matching the filter : " + personSearchFilter);
					return false;
				}
				List<WSAccessEntitlement> listWSAccessEntitlement = null;
				List<WSUserAccess> wsUserAccessList = new ArrayList<WSUserAccess>();
				if(accessName!=null){
					//Search all available accesses have same accessName
					listWSAccessEntitlement = accessService.
						searchAvailableAccessEntitlements(wsSession, null, personDN, null, accessName);
					if(listWSAccessEntitlement != null && listWSAccessEntitlement.size() > 0){
						//get all accesses of user that have same accessname
						for(WSAccessEntitlement wsAccessEnt : listWSAccessEntitlement) {
							System.out.println("wsAccessEnt = " + wsAccessEnt.getName() + " " + wsAccessEnt.getAccessId());
							wsUserAccessList.addAll(accessService.getAccesses(wsSession,
								personDN, wsAccessEnt.getAccessId()));
						}
					} else {
						Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "GETACCESSES", "No Access entitlements found matching the access name : " + accessName);
						return false;
					}
				} else {
					//get all accesses of user
					wsUserAccessList.addAll(accessService.getAccesses(wsSession,
						personDN, null));
				}

				System.out.println("Get accesses request submitted successfully.");
				for (WSUserAccess userAcc : wsUserAccessList) {
					System.out.println(userAcc.getAccessId());
					System.out.println(userAcc.getAccessName());
					System.out.println(userAcc.getAccessType());				
					System.out.println("******");
				}
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsAccessServiceClient -operationName?getAccesses -principal?<username> -credential?<password> -person?\"(cn=a*)\" -accessName?<access name>"+
						"\n for example: wsAccessServiceClient -operationName?getAccesses -principal?\"itim manager\" " +
						"-credential?secret -person?\"(cn=a*)\" -accessName?AuditRole";
			}
		},
			
		SEARCHAVAILABLEACCESSENTITLEMENTS{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				String personSearchFilter = (String)mpParams.get(PARAM_PERSON_SEARCH_FILTER);
				String container = (String)mpParams.get(PARAM_CONTAINER);
				String accessType = (String)mpParams.get(PARAM_ACCESS_TYPE);
				String accessInfo = (String)mpParams.get(PARAM_ACCESS_INFO);
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				WSAccessService accessService = getAccessService();
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				
				WSOrganizationalContainer wsContainer = null;
				if (container != null) {
					List<WSOrganizationalContainer> lstWSOrgContainers = wsOrgContainerService
							.searchContainerByName(wsSession, null, "Organization", container);
					if (lstWSOrgContainers != null && !lstWSOrgContainers.isEmpty()) {
						wsContainer = lstWSOrgContainers.get(0);
						container = wsContainer.getItimDN();
					}
					else
						Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "SEARCHAVAILABLEACCESSENTITLEMENTS", "No container found matching the container name : " + container);
				}
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession,
													personSearchFilter, null);
				String personDN = null;
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//If there are more than one person matching given criteria then select the first one.
					personDN = lstWSPersons.get(0).getItimDN();
				}
				else{
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "SEARCHAVAILABLEACCESSENTITLEMENTS", "No person found matching the filter : " + personSearchFilter);
					return false;
				}
				
				List<WSAccessEntitlement> wsAccessEntitlementList = accessService.
							searchAvailableAccessEntitlements(wsSession, container, 
									personDN, accessType, accessInfo);
				System.out.println("searchAvailableAccessEntitlements request submitted successfully.");
				for (WSAccessEntitlement wsAccEnt: wsAccessEntitlementList){
					System.out.println("***");
					System.out.println(wsAccEnt.getAccessId());
					System.out.println(wsAccEnt.getAccessType());
					System.out.println(wsAccEnt.getAccessDescription());
					System.out.println(wsAccEnt.getName());
					System.out.println(wsAccEnt.getProfileName());
					System.out.println(wsAccEnt.getServiceName());
				}
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsAccessServiceClient -operationName?searchAvailableAccessEntitlements -principal?<username> -credential?<password> -person?\"(cn=a*)\" -container?<container name> -accessType?<access type> -accessInfo?<access info>"
						+ "\n for example: wsAccessServiceClient -operationName?searchAvailableAccessEntitlements" +
						" -principal?\"itim manager\" -credential?secret -person?\"(cn=a*)\"" +
						" -container?Organization -accessType?Application -accessInfo?AuditRole";
			}
		},
		
		REMOVEACCESS {
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				String personSearchFilter = (String)mpParams.get(PARAM_PERSON_SEARCH_FILTER);
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				WSAccessService accessService = getAccessService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, 
													personSearchFilter, null);
				String personDN = null;
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//If there are more than one person matching given criteria then select the first one.
					personDN = lstWSPersons.get(0).getItimDN();
				}
				else{
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "REMOVEACCESS", "No person found matching the filter : " + personSearchFilter);
					return false;
				}
				// Get the list of user accesses of a person.
				List<WSUserAccess> wsualist = accessService.getAccesses(wsSession, personDN, null);
				WSUserAccess wsuAccess = null;
				if(wsualist != null && wsualist.size() > 0){
					//select the first user access from the returned list.
					wsuAccess = wsualist.get(0);
					System.out.println("user access list : size- "+ wsualist.size() );
				}
				else{
					Utils.printMsg(WSAccessServiceClient.class.getName(), "execute", "REMOVEACCESS", "No user Accesses found for a given person: " + personDN);
					return false;
				}
				WSRequest wsReq = accessService.removeAccess (wsSession, wsuAccess, null);
				
				System.out.println("Remove access request submitted successfully.");
				Utils.printWSRequestDetails("Remove Access", wsReq);
				return true;
				
			}

			public String getUsage(String errMsg) {
				return "usage: wsAccessServiceClient -operationName?removeAccess -principal?<username> -credential?<password> -person?\"(cn=a*)\""+
						"\n for example: wsAccessServiceClient -operationName?removeAccess" +
						" -principal?\"itim manager\" -credential?secret -person?\"(cn=a*)\"";
			}
		},

		;

		/**
		 * The method is to be overridden in each operation defined enum. The
		 * method will have the core logic of connecting to the target service
		 * and executing the web service method.
		 * 
		 * @param mpParams
		 * @return
		 * @throws Exception
		 */
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

		static WSACCESSSERVICE_OPERATIONS getRoleServiceOperation(String param) {
			return WSACCESSSERVICE_OPERATIONS.valueOf(param);
		}
		
		WSSession loginIntoITIM(String principal, String credential) throws WSInvalidLoginException, WSLoginServiceException{
			
			WSSessionService proxy = getSessionService();			
			WSSession session = proxy.login(principal, credential);
			if(session != null)
				Utils.printMsg(WSAccessServiceClient.class.getName(), "loginIntoITIM", this.name(), "session id is " + session.getSessionID());
			else
				Utils.printMsg(WSAccessServiceClient.class.getName(), "loginintoITIM", this.name(), "Invalid session");
			
			return session;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WSAccessServiceClient wsPSClient = new WSAccessServiceClient();
		wsPSClient.run(args, true);
	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
				operationName = operationName.toUpperCase();
				for(WSACCESSSERVICE_OPERATIONS operation : WSACCESSSERVICE_OPERATIONS.values()){
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

}
