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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.ws.model.WSAccount;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSService;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.services.WSServiceService;

public class WSAccountServiceClient extends GenericWSClient {
	
	private static final String CLASS_NAME = WSAccountServiceClient.class.getName();

	
	private static final String PARAM_DATE = "date";
	private static final String PARAM_NEW_PASSWORD = "newPassword";
	private static final String PARAM_ITIM_CREDENTIAL = "credential";
	private static final String PARAM_ITIM_PRINCIPAL = "principal";
	private static final String PARAM_SERVICE_FILTER = "filter";
	private static final String PARAM_ORG_CONTAINER = "orgContainer";
	private static final String PARAM_ORG_PROFILE_NAME = "profileName";
	private static final String PARAM_PERSON_FILTER = "personFilter";
	private static final String PARAM_JUSTIFICATION = "justification";

	enum WSSACCOUNTSERVICE_OPERATIONS {

		CREATEACCOUNT {

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String orgName = (String) mpParams.get(PARAM_ORG_CONTAINER);
				String profileName = (String) mpParams.get(PARAM_ORG_PROFILE_NAME);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);
				String sDate = (String) mpParams.get(PARAM_DATE);
				String justification = (String) mpParams.get(PARAM_JUSTIFICATION);
				mpParams.remove(OPERATION_NAME);
				mpParams.remove(PARAM_ITIM_PRINCIPAL);
				mpParams.remove(PARAM_ITIM_CREDENTIAL);
				mpParams.remove(PARAM_ORG_CONTAINER);
				mpParams.remove(PARAM_ORG_PROFILE_NAME);
				mpParams.remove(PARAM_SERVICE_FILTER);
				mpParams.remove(PARAM_DATE);
				mpParams.remove(PARAM_JUSTIFICATION);
				WSSession session = loginIntoITIM(principal, credential);

				WSAccountService wsAccountService = getAccountService();
				WSService wsService = this.searchService(filter, orgName, profileName, session);
				String serviceDN = wsService.getItimDN();
				Date utilDate = null;
				if (sDate == null)
					utilDate = new Date();
				else
					utilDate = Utils.convertStringToDate(sDate);
				XMLGregorianCalendar date = Utils.long2Gregorian(utilDate.getTime());
				List<WSAttribute> wsAttrs = this.getWSAttributes(mpParams);

				WSRequest wsRequest = wsAccountService.createAccount(session, serviceDN, wsAttrs,
						date,justification);
				Utils.printWSRequestDetails("Create Account", wsRequest);

				return true;

			}

			@Override
			String getUsage(String errMessage) {

				return "usage: wsAccountService -operationName?createAccount "
						+ "-principal?<username> -credential?<password> -date?<Date when the account should be created>"
						+ "-orgContainer?<Name of the container in which the service resides> "
						+ "-profileName?<Profile Name of the Org Container> "
						+ "-filter?<Filter to search the service> -eruid?<Account ID attribute> "
						+ "-owner?<DN of the person>"
						+ "\n for example: wsAccountService -operationName?createAccount -principal?\"itim manager\" -credential?secret -date?04/23/2010"
						+ "-orgContainer?Finance -profileName?OrganizationalUnit -filter?(erServiceName=\"Payroll App\") "
						+ "-eruid?jhill -owner?\"erglobalid=00000000000000000007,ou=0,ou=people,erglobalid=00000000000000000000,ou=org,dc=com\"";

			}
		},

		DEPROVISIONACCOUNT {

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);
				String sDate = (String) mpParams.get(PARAM_DATE);
				String justification=(String) mpParams.get(PARAM_JUSTIFICATION);
				
				WSSession session = loginIntoITIM(principal, credential);
				WSAccountService wsAccountService = getAccountService();
				WSAccount wsAccount = this.searchAccount(session, filter);
				
				Date utilDate = null;
				if (sDate == null)
					utilDate = new Date();
				else
					utilDate = Utils.convertStringToDate(sDate);
				XMLGregorianCalendar date = Utils.long2Gregorian(utilDate.getTime());
				WSRequest wsRequest = wsAccountService.deprovisionAccount(session, wsAccount.getItimDN(), date,justification);
				Utils.printWSRequestDetails(" deprovision accounts ", wsRequest);
				
				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsAccountService -operationName?deprovisionAccount "
				+ "-principal?<username> -credential?<password> -date?<Date when the account should be deprovisioned>"
				+ "-filter?<Filter to search the account>"
				+ "\n for example: wsAccountService -operationName?deprovisionAccount -principal?\"itim manager\" -credential?secret -date?04/23/2010"
				+ "-filter?(&(erUid=jhill)(owner=erglobalid=2722133467913424484,ou=0,ou=people,erglobalid=00000000000000000000,ou=org,dc=com)) ";
			}

		},
		
		RESTOREACCOUNT{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);
				String sDate = (String) mpParams.get(PARAM_DATE);
				String newPassword = (String)mpParams.get(PARAM_NEW_PASSWORD);
				String justification= (String)mpParams.get(PARAM_JUSTIFICATION);
				WSSession session = loginIntoITIM(principal, credential);
				WSAccountService wsAccountService = getAccountService();
				
				WSAccount wsAccount = this.searchAccount(session, filter);
				
				Date utilDate = null;
				if (sDate == null)
					utilDate = new Date();
				else
					utilDate = Utils.convertStringToDate(sDate);
				XMLGregorianCalendar date = Utils.long2Gregorian(utilDate.getTime());
				
				WSRequest wsRequest = wsAccountService.restoreAccount(session, wsAccount.getItimDN(), newPassword, date,justification);
				Utils.printWSRequestDetails("restore account ", wsRequest);
				
				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsAccountService -operationName?restoreAccount "
				+ "-principal?<username> -credential?<password> -date?<Date when the account should be restored>"
				+ "-filter?<Filter to search the account> -newPassword?<New Password while restoring the account.>"
				+ "\n for example: wsAccountService -operationName?restoreAccount -principal?\"itim manager\" -credential?secret -date?04/23/2010"
				+ "-filter?(&(erUid=jhill)(owner=erglobalid=2722133467913424484,ou=0,ou=people,erglobalid=00000000000000000000,ou=org,dc=com)) "
				+ "-newPassword?secret";
			}
			
		},
		
		SUSPENDACCOUNT{
				
			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);
				String sDate = (String) mpParams.get(PARAM_DATE);
				String justification = (String) mpParams.get(PARAM_JUSTIFICATION);
				WSSession session = loginIntoITIM(principal, credential);
				WSAccountService wsAccountService = getAccountService();
				WSAccount wsAccount = this.searchAccount(session, filter);
				
				Date utilDate = null;
				if (sDate == null)
					utilDate = new Date();
				else
					utilDate = Utils.convertStringToDate(sDate);
				XMLGregorianCalendar date = Utils.long2Gregorian(utilDate.getTime());
				
				WSRequest wsRequest = wsAccountService.suspendAccount(session, wsAccount.getItimDN(), date,justification);
				Utils.printWSRequestDetails(" suspend account ", wsRequest);
				
				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsAccountService -operationName?suspendAccount "
				+ "-principal?<username> -credential?<password> -date?<Date when the account should be suspended>"
				+ "-filter?<Filter to search the account> "
				+ "\n for example: wsAccountService -operationName?suspendAccount -principal?\"itim manager\" -credential?secret -date?04/23/2010"
				+ "-filter?(&(erUid=jhill)(owner=erglobalid=2722133467913424484,ou=0,ou=people,erglobalid=00000000000000000000,ou=org,dc=com)) ";
			}
			
		},
		
		ORPHANACCOUNTS{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);

				WSSession session = loginIntoITIM(principal, credential);
				WSAccountService wsAccountService = getAccountService();

				List<WSAccount> lstWSAccounts = this.searchAccounts(session,filter);
				WSPersonService port = getPersonService();
				WSPerson wsPerson = port.getPrincipalPerson(session);
				wsAccountService.orphanAccounts(session, wsPerson.getItimDN(),lstWSAccounts);
				Utils.printMsg(CLASS_NAME, "execute", this.name(), "Successfully submitted request for orphaning accounts.");

				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsAccountService -operationName?orphanAccounts "
				+ "-principal?<username> -credential?<password> "
				+ "-filter?<Filter to search the account> "
				+ "\n for example: wsAccountService -operationName?orphanAccounts -principal?\"itim manager\" -credential?secret"
				+ "-filter?(erUid=jhill) ";
			}
			
		},
		
		ADOPTACCOUNTS{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) mpParams.get(PARAM_SERVICE_FILTER);
				String personFilter = (String) mpParams.get(PARAM_PERSON_FILTER);

				WSSession session = loginIntoITIM(principal, credential);
				WSAccountService wsAccountService = getAccountService();

				List<WSAccount> lstWSAccounts = this.searchAccounts(session,filter);
				WSPersonService port = getPersonService();
				List<WSPerson> wsPersons = port.searchPersonsFromRoot(session,personFilter, null);
				WSPerson wsPerson = null;
				if (wsPersons != null && wsPersons.size() > 0)
					wsPerson = wsPersons.get(0);
				else {
					System.out.println(" The person filter " + personFilter
							+ " did not match any persons in ITIM");
					throw new Exception(" The person filter " + personFilter
							+ " did not match any persons in ITIM");
				}

				wsAccountService.adoptAccounts(session, wsPerson.getItimDN(),lstWSAccounts);

				Utils.printMsg(CLASS_NAME, "execute", this.name(),
						"Adopt accounts request submitted successfully.");

				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsAccountService -operationName?adoptAccounts "
				+ "-principal?<username> -credential?<password> "
				+ "-filter?<Filter to search the account> "
				+ " -personFilter?<\"(&(cn=<Name of the person>)(sn=<Last Name>))\">"
				+ "\n for example: wsAccountService -operationName?orphanAccounts -principal?\"itim manager\" -credential?secret -date?04/23/2010"
				+ "-filter?(erUid=jhill) -personFilter?\"(&(cn=\"Judith Hill\")(sn=Hill))\"";
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
			System.out.println("Principle and credential are : " + principal
					+ credential);
			WSSession session = proxy.login(principal, credential);
			if (session != null)
				Utils.printMsg(WSAccountServiceClient.class.getName(),
						"loginIntoITIM", this.name(), "session id is "
								+ session.getSessionID());
			else
				Utils.printMsg(WSAccountServiceClient.class.getName(),
						"loginintoITIM", this.name(), "Invalid session");

			return session;
		}

	
		WSService searchService(String filter, String orgName,
				String profileName, WSSession wsSession) throws Exception {

			WSService wsService = null;
			WSServiceService port = getServiceService();
			WSOrganizationalContainer wscontainer = this.searchOrgContainerByName(wsSession, orgName, profileName);
			List<WSService> lstWSServices = port.searchServices(wsSession,
					wscontainer, filter);

			if (lstWSServices != null && lstWSServices.size() > 0)
				wsService = lstWSServices.get(0);
			else {
				System.out
						.println(" No No Service found in ITIM matching the filter "
								+ filter);
				throw new Exception(
						"No Service found in ITIM matching the filter "
								+ filter);
			}

			return wsService;
		}

		WSOrganizationalContainer searchOrgContainerByName(WSSession wsSession,
				String containerName, String profileName) throws Exception {

			WSOrganizationalContainer wsOrgContainer = null;
			WSOrganizationalContainerService port = getOrganizationalContainerService();
			List<WSOrganizationalContainer> lstWSOrgContainer = port
					.searchContainerByName(wsSession, null, profileName,
							containerName);

			if (lstWSOrgContainer != null && lstWSOrgContainer.size() > 0)
				wsOrgContainer = lstWSOrgContainer.get(0);
			else {
				System.out
						.println(" No matching organization found for container name "
								+ containerName);
				throw new Exception(
						" No matching organization found for container name "
								+ containerName);
			}

			return wsOrgContainer;
		}

		List<WSAttribute> getWSAttributes(Map<String, Object> mpParams) throws Exception {
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
					Utils.printMsg(WSPersonServiceClient.class.getName(), "getWSAttributes", this
							.name(), "The parameter value datatype is not supported.");
					// System.out.println(" The parameter value datatype is not supported.");
				}
				wsAttr.setValues(arrStringValues);
				lstWSAttributes.add(wsAttr);
			}

			return lstWSAttributes;
		}
		
		
		List<WSAccount> searchAccounts(WSSession session, String filter) throws Exception{
			
			WSSearchArguments searchArgs = new WSSearchArguments();
			searchArgs.setFilter(filter);
			//List<WSAccount> lstWSAccounts = this.getAccountService().searchAccounts(session, searchArgs);
			List<WSAccount> lstWSAccounts = getAccountService().searchAccounts(session, searchArgs);
			
			if(lstWSAccounts == null || lstWSAccounts.isEmpty()){
				System.out.println(" No accounts found matching filter " + filter);
				throw new Exception(" No accounts found matching filter " + filter);
			}
				
			
			return lstWSAccounts;
		}
		
		WSAccount searchAccount(WSSession session, String filter) throws Exception {
			WSAccount wsAccount = null;
			List<WSAccount> lstWSAccounts = searchAccounts(session, filter);
			
			wsAccount = lstWSAccounts.get(0);
			

			return wsAccount;
		}

	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);
		boolean retCode = false;
		
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSSACCOUNTSERVICE_OPERATIONS operation : WSSACCOUNTSERVICE_OPERATIONS.values()) {
				if (operation.name().equalsIgnoreCase(operationName)) {
					try {
						retCode = operation.execute(mpParams);
						break;
					} catch (Exception e) {
						e.printStackTrace();
						operation.getUsage(e.getLocalizedMessage());
					}
				}
			}
		}
		return retCode;
	}


	public static void main(String[] args) {
		WSAccountServiceClient client = new WSAccountServiceClient();
		client.run(args, true);
	}

}
