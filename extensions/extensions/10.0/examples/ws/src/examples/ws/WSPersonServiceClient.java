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

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;

import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.ws.model.WSAccount;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSRole;
import com.ibm.itim.ws.model.WSService;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSSessionService;

/**
 * @author administrator
 * 
 */

public class WSPersonServiceClient extends GenericWSClient {
	
	private static final String PARAM_NOTIFY_BY_MAIL = "notifyByMail";
	private static final String PARAM_SYNC_PASSWORD = "syncPassword";
	private static final String PARAM_DATE = "date";
	private static final String PARAM_TENANT_ID = "tenantId";
	private static final String PARAM_NEW_PASSWORD = "newPassword";
	private static final String PARAM_RESTORE_ACCOUNTS = "restoreAccounts";
	
	private static final String PARAM_PERSON_FILTER = "filter";
	private static final String PARAM_ROLE_FILTER = "filterRole";
	private static final String PARAM_JUSTIFICATION = "justification";
	
	
	@Resource
	private WebServiceContext wsCtxt = null;

	// TODO: For usage we will create an enum which will have the getUsage
	// method implementation in it.
	enum WSPERSONSERVICE_OPERATIONS {
		GETPRINCIPALPERSON {

			public boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				
				WSPersonService personService = getPersonService();
				WSPerson wsPerson = personService.getPrincipalPerson(session);
				Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Principle Person Details : \n" + 
								"Name : " + wsPerson.getName() + " \n" + 
								"Profile Name : " + wsPerson.getProfileName() + " \n" +
								"Person DN : " + wsPerson.getItimDN() );
				wsPerson.getAttributes();

				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?getPrincipalPerson -principal?<username> -credential?<password> \n for example: wsPersonService -operationName?getPrincipalPerson -principal?\"itim manager\" -credential?secret";
			}
		},


		CREATEPERSON {

			public boolean execute(Map<String, Object> mpParams) throws Exception {
				
				boolean executedSuccessfully = false;
				mpParams.remove(OPERATION_NAME);
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				mpParams.remove(PARAM_ITIM_PRINCIPAL);
				mpParams.remove(PARAM_ITIM_CREDENTIAL);
				mpParams.remove(PARAM_JUSTIFICATION);
				WSSession session = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				//System.out.println(" session ==> " + session);
				//System.out.println(" proxy ==> " + proxy);

				String containerName = (String) mpParams.get(PARAM_ORG_CONTAINER);
				mpParams.remove(PARAM_ORG_CONTAINER);

				// Dependency on OrganizationalContainerservice.
				WSOrganizationalContainerService port = getOrganizationalContainerService();
				List<WSOrganizationalContainer> lstWSOrgContainers = port.searchContainerByName(
						session, null, "OrganizationalUnit", containerName);
				
				//System.out.println(" response " + response);
				if (lstWSOrgContainers != null && !lstWSOrgContainers.isEmpty()) {
					WSOrganizationalContainer wsContainer = lstWSOrgContainers.get(0);
					WSPerson wsPerson = createWSPersonFromAttributes(mpParams);
					XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
					boolean isCreatePersonAllowed = personService.isCreatePersonAllowed(session);
					if(isCreatePersonAllowed){
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "The user " + PARAM_ITIM_PRINCIPAL + " is authorized to create a person");
						WSRequest wsRequest = personService.createPerson(session, wsContainer, wsPerson, date,justification);
						Utils.printWSRequestDetails("create person", wsRequest);
					}else{
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "The user " + PARAM_ITIM_PRINCIPAL + " is not authorized to create a person");
					}
					
					executedSuccessfully = true;
				} else {
					System.out.println("No container found matching " + containerName);
				}

				return executedSuccessfully;
			}

			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?createPerson -principal?<username> -credential?<password> -cn?<Name of the person> -sn?<Last Name> [additional person attributes] -orgContainer?<Container Name in which the user is to be added>"+
				"\n for example: wsPersonService -operationName?createPerson -principal?\"itim manager\" -credential?secret -cn?\"Judith Hill\" -sn?\"Hill\" -orgContainer?Finance ";
			}
		},

		DELETEPERSON {

			// TODO: Dependency on RoleService
			public boolean execute(Map<String, Object> mpParams) throws Exception {
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "DELETEPERSON", "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson personToBeDeleted = lstWSPersons.get(0);
					String personDN = personToBeDeleted.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "DELETEPERSON", "Deleting the person " + personToBeDeleted.getName() + " having DN " + personToBeDeleted.getItimDN());
					WSRequest wsRequest = personService.deletePerson(wsSession, personDN, date,justification);
					Utils.printWSRequestDetails("delete person", wsRequest);
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "DELETEPERSON", "No person found matching the filter : " + sFilterParam);
					return false;
				}
				
				
			}

			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?deletePerson -principal?<username> -credential?<password> -filter?\"cn=<Name of the person>\" "+
						"\n for example: wsPersonService -operationName?deletePerson -principal?\"itim manager\" -credential?secret -filter?\"(sn=\"Arora\")\" " +
						" The filter parameter is used to search for the person in ITIM.";
			}

		},
		
		GETACCOUNTSBYOWNER{
			
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "DELETEPERSON", "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "GETACCOUNTSBYOWNER", "Getting accounts owned by user  " + person.getName());
					List<WSAccount> lstWSAccounts = personService.getAccountsByOwner(wsSession, personDN);
					if(lstWSAccounts != null && lstWSAccounts.size() > 0){
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "GETACCOUNTSBYOWNER", " Number of accounts owned by user " + person.getName() + " is " + lstWSAccounts.size());
						for(WSAccount wsAccount : lstWSAccounts){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "GETACCOUNTSBYOWNER", " \n User ID : " + wsAccount.getName() + " \n" +
									" Service Name : " + wsAccount.getServiceName() + " \n " +
									" Account Profile Name : " + wsAccount.getProfileName() + " \n " +
									" Account DN : " + wsAccount.getItimDN());
						}
					}
					
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}
			
			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?deletePerson -principal?<username> -credential?<password> -filter?\"cn=<Name of the person>\" -filter?\"sn=<Last Name>\" [additional filter criteria attributes]"+
						"\n for example: wsPersonService -operationName?createPerson -principal?\"itim manager\" -credential?secret -cn?\"Judith Hill\" -sn?\"Hill\" ";
			}
			
		},
		
		GETFILTEREDAUTHORIZEDSERVICES{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "GETACCOUNTSBYOWNER", "Getting accounts owned by user  " + person.getName());
					
					mpParams.remove(PARAM_ITIM_CREDENTIAL);
					mpParams.remove(PARAM_ITIM_PRINCIPAL);
					mpParams.remove(PARAM_PERSON_FILTER);
					mpParams.remove(OPERATION_NAME);
					
					WSAttribute wsServiceAttr = this.serviceSearchAttributes(mpParams);
					List<WSService> lstWSServices = personService.getFilteredAuthorizedServices(wsSession, personDN, wsServiceAttr);
					if(lstWSServices != null && lstWSServices.size() > 0){
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "GETACCOUNTSBYOWNER", " Number of authorized services " + lstWSServices.size());
						for(WSService wsService : lstWSServices){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), 
									"\n Service Name : " + wsService.getName() + " \n " +
									"\n Service Profile Name : " + wsService.getProfileName() + " \n " +
									"\n Service DN : " + wsService.getItimDN());
						}
					}
					
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "DELETEPERSON", "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}
			
			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?deletePerson -principal?<username> -credential?<password> -filter?\"cn=<Name of the person>\" -filter?\"sn=<Last Name>\" [additional filter criteria attributes]"+
						"\n for example: wsPersonService -operationName?createPerson -principal?\"itim manager\" -credential?secret -cn?\"Judith Hill\" -sn?\"Hill\" ";
			}
			
			private WSAttribute serviceSearchAttributes(Map<String, Object> mpParams){
				WSAttribute wsServiceAttr = new WSAttribute();
				if(!mpParams.isEmpty()){
					ArrayOfXsdString values = new ArrayOfXsdString();
					Iterator<String> itrParamsKey = mpParams.keySet().iterator();
					String paramName = itrParamsKey.next();
					wsServiceAttr.setName(paramName);
					Object objParamValue = mpParams.get(paramName);
					if(objParamValue instanceof String){
						values.getItem().add((String)objParamValue);
					}else if(objParamValue instanceof Vector){
						values.getItem().addAll((Vector)objParamValue);
					}else{
						Utils.printMsg(WSPersonServiceClient.class.getName(), "serviceSearchAttributes", this.name(), "The parameter value datatype is not supported.");
						System.out.println("" + this.getUsage(""));
					}
					wsServiceAttr.setValues(values);
				}else{
					Utils.printMsg(WSPersonServiceClient.class.getName(), "serviceSearchAttributes", this.name(), "No service filter attributes specified.");
					System.out.println("" + this.getUsage(""));
				}
				
				
				return wsServiceAttr;
			}
		},
		
		GETPERSONROLES{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Getting roles for user  " + person.getName());
					
					List<WSRole> lstWSRoles = personService.getPersonRoles(wsSession, personDN);
					if(lstWSRoles != null && lstWSRoles.size() > 0){
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), " Number of roles owned by user " + person.getName() + " is " + lstWSRoles.size());
						for(WSRole wsRole : lstWSRoles){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "\n Role Name : " + wsRole.getName() +
									"\n Role Description : " + wsRole.getDescription() +
									"\n Role DN : " + wsRole.getItimDN());
						}
					}
					
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}
			
			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?getPersonRoles -principal?<username> -credential?<password> -filter?\"cn=<Name of the person>\" -filter?\"sn=<Last Name>\" [additional filter criteria attributes]"+
						"\n for example: wsPersonService -operationName?createPerson -principal?\"itim manager\" -credential?secret -cn?\"Judith Hill\" -sn?\"Hill\" ";
			}
		},
		
		LOOKUPPERSON{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Getting roles for user  " + person.getName());
					
					WSPerson wsPerson = personService.lookupPerson(wsSession, personDN);
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "\n Person Name : " + wsPerson.getName() +
									"\n Profile Name : " + wsPerson.getProfileName() +
									"\n Person DN : " + wsPerson.getItimDN());
					
					return person.getName().equals(wsPerson.getName());
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}
			
			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?lookupPerson -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" [additional filter criteria attributes]"+
						"\n for example: wsPersonService -operationName?lookupPerson -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\"";
			}
		},
		
		MODIFYPERSON{
			public boolean execute(Map<String, Object> mpParams) throws Exception{
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be deleted.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be modified. 
					//If there are more than one person then we select the first one and modify it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Modifying person  " + person.getName());
					//Remove the unnecessary attributes from the Map mpParams so that the person attributes to be modified can be passed
					mpParams.remove(OPERATION_NAME);
					mpParams.remove(PARAM_ITIM_PRINCIPAL);
					mpParams.remove(PARAM_ITIM_CREDENTIAL);
					mpParams.remove(PARAM_PERSON_FILTER);
					
					List<WSAttribute> lstWSAttributes = Utils.getWSAttributesList(mpParams,"MODIFYPERSON");
					if(lstWSAttributes != null && lstWSAttributes.size() > 0){
						WSRequest wsRequest = personService.modifyPerson(wsSession, personDN, lstWSAttributes, date,justification);
						Utils.printWSRequestDetails("modify person", wsRequest);
					}else{
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No modify parameters passed to the modifyPerson operation.");
						Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "\n" + this.getUsage(""));
					}
					
					
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}
			
			public String getUsage(String errMsg) {
				return "usage: wsPersonService -operationName?modifyPerson -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" \n -mail?<Email Address> -ersynchpassword?<password>"+
						"\n The attributes specified after the -filter parameter are the attributes which are to be modified."+
						"\n for example: wsPersonService -operationName?modifyPerson -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -mail?modifiedEmailAddr@ibm.com -ersyncpassword?m0difiedPwd";
			}
		},
		
		RESTOREPERSON{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be restored.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Restoring user  " + person.getName());
					String sRestoreAccts = (String)mpParams.get(PARAM_RESTORE_ACCOUNTS);
					boolean restoreAccounts = new Boolean(sRestoreAccts);
					String password = (String)mpParams.get(PARAM_NEW_PASSWORD);
					
					WSRequest wsRequest = personService.restorePerson(wsSession, personDN, restoreAccounts, password, date,justification);
					Utils.printWSRequestDetails("restore person", wsRequest);
					
					return true;
					
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsPersonService -operationName?restorePerson -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" -restoreAccounts?<true/false> -newPassword?<new password>"+
				"\n for example: wsPersonService -operationName?restorePerson -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -restoreAccounts?true -newPassword?sec001ret";
			}
			
		},
		
		SELFREGISTER{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				
				mpParams.remove(OPERATION_NAME);
				
				String tenantId = (String)mpParams.get(PARAM_TENANT_ID);
				mpParams.remove(PARAM_TENANT_ID);
				
				WSPerson wsPerson = createWSPersonFromAttributes(mpParams);
				WSPersonService personService = getPersonService();
				personService.selfRegister(wsPerson, null);
				Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Self Registration of person completed successfully.");
				return true;
				
			}

			@Override
			String getUsage(String errMessage) {
				
				return "usage: wsPersonService -operationName?selfRegister [-tenantId?<tenant id>] -cn?<Name of the person> -sn?<Last Name> [additional person attributes]"+
				"\n tenantId is an optional parameter if not specified then the value specified in the enRole.properties for property \"enrole.defaulttenant.id\" is used." +
				"\n for example: wsPersonService -operationName?selfRegister -tenantId?org " +
				"-cn?\"Judith Hill\" -sn?Hill -l?\"IBM Location\" -uid?\"Judith Hill\" -givenname?Judith ";
				
			}
			
		},
		
		SUSPENDPERSONADVANCED{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be restored.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Suspending person  " + person.getName());
					
					boolean includeAccounts = Boolean.getBoolean((String)mpParams.get(PARAM_INCLUDE_ACCOUNTS));
					String sDate = (String)mpParams.get(PARAM_DATE);
					Date date = null;
					if(sDate != null){
						try{
							date = Utils.convertStringToDate(sDate);
						}catch(ParseException e){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "The date is not specified in the expected format. Expected format is MM/DD/YYYY");
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), this.getUsage(e.getLocalizedMessage()));
						}
					}else{
						date = new Date();
					}
					
					
					
					XMLGregorianCalendar xmlDate = Utils.long2Gregorian(date.getTime());
					WSRequest wsRequest = personService.suspendPersonAdvanced(wsSession, personDN, includeAccounts, xmlDate,justification);
					Utils.printWSRequestDetails("Suspended person ", wsRequest);
					
					return true;
					
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsPersonService -operationName?suspendPersonAdvanced -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" -includeAccounts?<true/false> -date?MM/DD/YYYY" +
						"\n for example: -operationName?suspendPersonAdvanced -credential?secret -principal?\"itim manager\" -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -includeAccounts?true -date?04/01/2010" +
						"\n If the \"date\" parameter is not specified then todays date is used for scheduling the person suspend action.";
			}
			
		},
		
		SYNCHPASSWORDS{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be restored.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Synchronizing password for person  " + person.getName());
					
					String syncPassword = (String)mpParams.get(PARAM_SYNC_PASSWORD);
					Boolean notifyByMail = Boolean.parseBoolean((String)mpParams.get(PARAM_NOTIFY_BY_MAIL));
					String sDate = (String)mpParams.get(PARAM_DATE);
					Date date = null;
					if(sDate != null){
						try{
							date = Utils.convertStringToDate(sDate);
						}catch(ParseException e){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "The date is not specified in the expected format. Expected format is MM/DD/YYYY");
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), this.getUsage(e.getLocalizedMessage()));
						}
					}else{
						date = new Date();
					}
					
					
					
					XMLGregorianCalendar xmlDate = Utils.long2Gregorian(date.getTime());
					WSRequest wsRequest = personService.synchPasswords(wsSession, personDN, syncPassword, xmlDate, notifyByMail);
					Utils.printWSRequestDetails("password synchronization", wsRequest);
					
					return true;
					
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
				
			}

			@Override
			String getUsage(String errMessage) {
				// TODO Auto-generated method stub
				//-operationName?synchPasswords -credential?secret -principal?"itim manager" -filter?"(&(cn="Judith Hill")(sn=Hill))" -syncPassword?sec001ret -notifyByMail?true -date?04/01/2010
				return "usage: wsPersonService -operationName?synchPasswords -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" -syncPassword?<password> -notifyByMail?<true/false> -date?MM/DD/YYYY" +
				"\n for example: -operationName?synchPasswords -credential?secret -principal?\"itim manager\" -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -syncPassword?sec001ret -notifyByMail?true -date?04/01/2010" +
				"\n If the \"date\" parameter is not specified then todays date is used for scheduling the person suspend action." + 
				"\n The \"syncPassword\" parameter value will be used for synchronizing the passwords for all the accounts.";
			}
			
		},
		
		SYNCHGENERATEDPASSWORD{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No Filter parameter passed to search for the person to be restored.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "Synchronizing password for person  " + person.getName());
					
					String sDate = (String)mpParams.get(PARAM_DATE);
					Date date = null;
					if(sDate != null){
						try{
							date = Utils.convertStringToDate(sDate);
						}catch(ParseException e){
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "The date is not specified in the expected format. Expected format is MM/DD/YYYY");
							Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), this.getUsage(e.getLocalizedMessage()));
						}
					}else{
						date = new Date();
					}
					
					
					
					XMLGregorianCalendar xmlDate = Utils.long2Gregorian(date.getTime());
					WSRequest wsRequest =personService.synchGeneratedPassword(wsSession, personDN, xmlDate);
					Utils.printWSRequestDetails("synchronization of generated password", wsRequest);
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", this.name(), "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				//-operationName?synchGeneratedPassword -credential?secret -principal?"itim manager" -filter?"(&(cn="Judith Hill")(sn=Hill))" -date?04/01/2010
				return "usage: wsPersonService -operationName?synchGeneratedPassword -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<Last Name>))\" -date?MM/DD/YYYY" +
				"\n for example: -operationName?synchGeneratedPassword -credential?secret -principal?\"itim manager\" -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -date?04/01/2010" +
				"\n If the \"date\" parameter is not specified then todays date is used for scheduling the person suspend action.";
			}
			
		},
		
		ADDROLESTOPERSON{

			// This method will cover testing of three methods: addRolesToPerson, addRole and addRoleToPerson.
			boolean execute(Map<String, Object> mpParams) throws Exception {
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sPersonFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sPersonFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "No Filter parameter passed to search for the person.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
				String sRoleFilterParam = (String)mpParams.get(PARAM_ROLE_FILTER);
				if(sRoleFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "No Filter parameter passed to search for the Roles.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				List<String> roleDNlist = new ArrayList<String>();
				WSRoleService roleService = getRoleService();
				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession, sRoleFilterParam);
				if(lstWSRoles != null && lstWSRoles.size() > 0){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "Role(s) matching the filter, "+ sRoleFilterParam+ ", are: ");
					for (Iterator<WSRole> iter = lstWSRoles.iterator(); iter.hasNext();) {
						WSRole wsRole = (WSRole) iter.next();
						String roleDN = wsRole.getItimDN();
						roleDNlist.add(roleDN);
						Utils.printMsg(WSRoleServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", roleDN);
					}
				}else{
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "No role found matching the filter : " + sRoleFilterParam);
					return false;
				}

				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sPersonFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means the requested person exists. If there are more than one person then we select the first one.
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "Adding role(s) to a person, " + person.getName() + ", having DN " + person.getItimDN());

					WSRequest wsRequest = personService.addRolesToPerson(wsSession, personDN, roleDNlist, date,justification);
					Utils.printWSRequestDetails("add roles to person", wsRequest);
					System.out.println("Add role to person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", "No person found matching the filter : " + sPersonFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				
				return "usage: wsPersonService -operationName?addRolesToPerson  -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<lastname>))\" -filterRole?<LDAP filter> -date?MM/DD/YYYY"+
				"\n for example: wsPersonService -operationName?addRolesToPerson  -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -filterRole?\"(errolename=a*)\" -date?04/01/2010";
				
			}
			
		},
		
		REMOVEROLESFROMPERSON{

			// This method will cover testing of three methods: removeRolesToPerson, removeRole and removeRoleToPerson.
			boolean execute(Map<String, Object> mpParams) throws Exception {
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sPersonFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sPersonFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No Filter parameter passed to search for the person.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
				String sRoleFilterParam = (String)mpParams.get(PARAM_ROLE_FILTER);
				if(sRoleFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No Filter parameter passed to search for the Roles.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				List<String> roleDNlist = new ArrayList<String>();
				WSRoleService roleService = getRoleService();
				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession, sRoleFilterParam);
				if(lstWSRoles != null && lstWSRoles.size() > 0){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "Role(s) matching the filter, "+ sRoleFilterParam+ ", are: ");
					for (Iterator<WSRole> iter = lstWSRoles.iterator(); iter.hasNext();) {
						WSRole wsRole = (WSRole) iter.next();
						String roleDN = wsRole.getItimDN();
						roleDNlist.add(roleDN);
						Utils.printMsg(WSRoleServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", roleDN);
					}
				}else{
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No role found matching the filter : " + sRoleFilterParam);
					return false;
				}
					
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sPersonFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means the requested person exists. If there are more than one person then we select the first one.
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "Removing role(s) from a person, " + person.getName() + ", having DN " + person.getItimDN());

					WSRequest wsRequest = personService.removeRolesFromPerson(wsSession, personDN, roleDNlist, date,justification);
					Utils.printWSRequestDetails("remove roles from person", wsRequest);
					System.out.println("Remove roles from person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No person found matching the filter : " + sPersonFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				
				return "usage: wsPersonService -operationName?removeRolesFromPerson  -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<lastname>))\" -filterRole?<LDAP filter> -date?MM/DD/YYYY"+
				"\n for example: wsPersonService -operationName?removeRolesFromPerson  -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -filterRole?\"(errolename=a*)\" -date?04/01/2010";
				
			}
			
		},
		
		TRANSFERPERSON{

			boolean execute(Map<String, Object> mpParams) throws Exception {
				//Search for a person from root. Take the cn or sn filter criteria from the user 
				String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
				if(sFilterParam == null){
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "TRANSFERPERSON", "No Filter parameter passed to search for the person.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
					
				//Call a searchPerson API to search for the person;
				String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSPersonService personService = getPersonService();
				XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
				String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
				String containerName = (String) mpParams.get(PARAM_ORG_CONTAINER);
				// Dependency on OrganizationalContainerservice.
				WSOrganizationalContainerService port = getOrganizationalContainerService();
				List<WSOrganizationalContainer> lstWSOrgContainers = port.searchContainerByName(
						wsSession, null, "OrganizationalUnit", containerName);

				if (lstWSOrgContainers == null || lstWSOrgContainers.isEmpty()) {
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "TRANSFERPERSON", "No container found matching "+ containerName);
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}
				WSOrganizationalContainer wsContainer = lstWSOrgContainers.get(0);
					
				List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
				if(lstWSPersons != null && lstWSPersons.size() > 0){
					//This means the requested person exists. If there are more than one person then we select the first one.
					WSPerson person = lstWSPersons.get(0);
					String personDN = person.getItimDN();
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "TRANSFERPERSON", "Transfer person " + person.getName() + " to a container having DN " + wsContainer.getItimDN());

					WSRequest wsRequest = personService.transferPerson(wsSession, personDN, wsContainer, date,justification);
					Utils.printWSRequestDetails("transfer person", wsRequest);
					System.out.println("Transfer person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
					return true;
				}else{
					//Output a message which says that the no person found matching the filter criteria
					Utils.printMsg(WSPersonServiceClient.class.getName(), "execute", "TRANSFERPERSON", "No person found matching the filter : " + sFilterParam);
					return false;
				}
			}

			@Override
			String getUsage(String errMessage) {
				
				return "usage: wsPersonService -operationName?transferPerson  -principal?<username> -credential?<password> -filter?\"(&(cn=<Name of the person>)(sn=<last name>)) -orgContainer?<orgContainerName> -date?MM/DD/YYYY"+
				"\n for example: wsPersonService -operationName?transferPerson  -principal?\"itim manager\" -credential?secret -filter?\"(&(cn=\"Judith Hill\")(sn=Hill))\" -orgContainer?myContainer -date?04/01/2010";
				
			}
			
		}
		
		;

		

		private static final String PARAM_SERVICE_FILTER = "serviceFilter";
		private static final String PARAM_INCLUDE_ACCOUNTS = "includeAccounts";
		private static final String PARAM_ORG_CONTAINER = "orgContainer";

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

		static WSPERSONSERVICE_OPERATIONS getPersonServiceOperation(String param) {
			return WSPERSONSERVICE_OPERATIONS.valueOf(param);
		}
		
		WSSession loginIntoITIM(String principal, String credential) throws WSInvalidLoginException, WSLoginServiceException{
			
			WSSessionService proxy = getSessionService();
			WSSession session = proxy.login(principal, credential);
			if(session != null)
				Utils.printMsg(WSPersonServiceClient.class.getName(), "loginIntoITIM", this.name(), "session id is " + session.getSessionID());
			else
				Utils.printMsg(WSPersonServiceClient.class.getName(), "loginintoITIM", this.name(), "Invalid session");
			
			return session;
		}
				
		WSPerson createWSPersonFromAttributes(Map<String, Object> mpParams) {
			WSPerson wsPerson = new WSPerson();
			wsPerson.setProfileName(ObjectProfileCategory.PERSON);
			java.util.Collection attrList = new ArrayList();
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
					Utils.printMsg(WSPersonServiceClient.class.getName(), "createWSPersonFromAttributes", "CREATEPERSON", "The parameter value datatype is not supported.");
					//System.out.println(" The parameter value datatype is not supported.");
				}
				wsAttr.setValues(arrStringValues);
				lstWSAttributes.add(wsAttr);
			}
			ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
			attrs.getItem().addAll(lstWSAttributes);
			wsPerson.setAttributes(attrs);

			return wsPerson;
		}
		
//		/**
//		 * The method returns a list of WSAttribute data objects from the input map.
//		 * 
//		 * @param mpParams
//		 * @return
//		 */
//		List<WSAttribute> getWSAttributesList(Map<String, Object> mpParams){
//			java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
//			WSAttribute wsAttr = null;
//			List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();
//
//			while (itrParams.hasNext()) {
//				String paramName = itrParams.next();
//				Object paramValue = mpParams.get(paramName);
//				wsAttr = new WSAttribute();
//				wsAttr.setName(paramName);
//				ArrayOfXsdString arrStringValues = new ArrayOfXsdString();
//				
//				List lstAttrValues = this.parseWSModifyArgs(paramValue, wsAttr);
//				arrStringValues.getItem().addAll(lstAttrValues);
////				if (paramValue instanceof String) {
////					arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
////				} else if (paramValue instanceof Vector) {
////					Vector paramValues = (Vector) paramValue;
////					arrStringValues.getItem().addAll(paramValues);
////				} else {
////					Utils.printMsg(WSPersonServiceClient.class.getName(), "createWSPersonFromAttributes", "CREATEPERSON", "The parameter value datatype is not supported.");
////					//System.out.println(" The parameter value datatype is not supported.");
////				}
//				wsAttr.setValues(arrStringValues);
//				lstWSAttributes.add(wsAttr);
//			}
//			
//			return lstWSAttributes;
//		}
//		
//		
//		List parseWSModifyArgs(Object paramValue, WSAttribute wsAttr){
//			List lstAttrValues = null;
//			if(paramValue instanceof String){
//				String sParamValue = (String)paramValue;
//				if(sParamValue.indexOf(":") >= 0){
//					sParamValue = setModOperationTypeAndReturnAttrValue(wsAttr, sParamValue);
//				}
//				lstAttrValues = Arrays.asList(sParamValue);
//			}else if(paramValue instanceof Vector){
//				Vector<String> paramValues = (Vector<String>)paramValue;
//				lstAttrValues = new ArrayList();
//				for(String sParamVal : paramValues){
//					if(sParamVal.indexOf(":") >= 0)
//						lstAttrValues.add(this.setModOperationTypeAndReturnAttrValue(wsAttr, sParamVal));
//				}
//			}else {
//				Utils.printMsg(WSPersonServiceClient.class.getName(), "createWSPersonFromAttributes", "CREATEPERSON", "The parameter value datatype is not supported.");
//				//System.out.println(" The parameter value datatype is not supported.");
//			}
//			
//			return lstAttrValues;
//		}
//
//		String setModOperationTypeAndReturnAttrValue(WSAttribute wsAttr, String sParamValue) {
//			int operationType = 0;
//			int delimiterIndex = sParamValue.indexOf(":");
//			String attrValue = null;
//			if(delimiterIndex >=0){
//				String[] args = sParamValue.split(":");
//				String opType = args[1];
//				Utils.MODIFY_OPERATION_TYPE enumValue = Utils.MODIFY_OPERATION_TYPE.valueOf(opType.trim());
//				operationType = enumValue.ordinal();
//				attrValue = args[0];
//			}
//			wsAttr.setOperation(operationType);
//			return attrValue;
//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WSPersonServiceClient wsPSClient = new WSPersonServiceClient();
		wsPSClient.run(args, true);
	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
				operationName = operationName.toUpperCase();
				for(WSPERSONSERVICE_OPERATIONS operation : WSPERSONSERVICE_OPERATIONS.values()){
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
