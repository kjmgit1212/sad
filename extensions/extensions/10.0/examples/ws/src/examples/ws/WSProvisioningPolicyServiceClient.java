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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.apps.policy.Entitlement;
import com.ibm.itim.apps.policy.ServiceTarget;
import com.ibm.itim.apps.policy.Membership;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSProvisioningPolicy;
import com.ibm.itim.ws.model.WSProvisioningPolicyEntitlement;
import com.ibm.itim.ws.model.WSProvisioningPolicyMembership;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSServiceTarget;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSProvisioningPolicyEntitlement;
import com.ibm.itim.ws.services.ArrayOfTns1WSProvisioningPolicyMembership;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSProvisioningPolicyService;
import com.ibm.itim.ws.services.WSSessionService;

/**
 * This is an example client to exercise WSProvisioningPolicyService API
 * methods. The example supports two operations viz. createPolicy and
 * getPolicies. The other operations are not included for the sake of brevity
 * but they can be easily implemented by following the implementation details of
 * the two operations provided in this class.
 * 
 * @author sandip_dey
 * 
 */
public class WSProvisioningPolicyServiceClient extends GenericWSClient {

	private static final String PARAM_DATE = "date";
	private static final String ORG_CONTAINER_PROFILE_NAME = "OrganizationalUnit";
	private static final String ORG_UNIT_NAME = "orgUnitName";
	private static final String PARAM_POLICY_NAME = "policyName";

	private enum WSPROVISIONINGPOLICY_OPERATIONS {
		CREATEPOLICY {

			@Override
			public boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";
				boolean success = true;
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String orgUnitName = (String) map.get(ORG_UNIT_NAME);
				WSSession session = loginIntoITIM(principal, credential);
				WSProvisioningPolicyService wsProvisioningPolicyService = getProvisioningPolicyService();

				String sDate = (String) map.get(PARAM_DATE);
				Date date = null;
				if (sDate != null) {
					try {
						date = Utils.convertStringToDate(sDate);
					} catch (ParseException e) {
						Utils
								.printMsg(
										WSProvisioningPolicyServiceClient.class
												.getName(),
										methodName,
										this.name(),
										"The date is not specified in the expected format. Expected format is MM/DD/YYYY");
						Utils.printMsg(WSProvisioningPolicyServiceClient.class
								.getName(), methodName, this.name(), this
								.getUsage(e.getLocalizedMessage()));
					}
				} else {
					date = new Date();
				}
				XMLGregorianCalendar xmlDate = Utils.long2Gregorian(date
						.getTime());
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				List<WSOrganizationalContainer> lstWSOrganizationContainers = wsOrgContainerService
						.searchContainerByName(session, null,
								ORG_CONTAINER_PROFILE_NAME, orgUnitName);
				WSOrganizationalContainer parentWSOrgContainer = null;
				if (lstWSOrganizationContainers != null
						&& lstWSOrganizationContainers.size() > 0) {
					// We have found some organizational containers
					// Get the first container and proceed
					parentWSOrgContainer = lstWSOrganizationContainers.get(0);
					WSProvisioningPolicy policy = this.getProvisioningPolicy();
					WSRequest requestObj = wsProvisioningPolicyService
							.createPolicy(session, parentWSOrgContainer,
									policy, xmlDate);
					Utils.printMsg(WSProvisioningPolicyServiceClient.class
							.getName(), methodName, this.name(),
							"Request status: " + requestObj.getStatusString());
				} else {
					Utils
							.printMsg(WSProvisioningPolicyServiceClient.class
									.getName(), methodName, this.name(),
									"The parent organization unit passed in not valid.");
					success = false;
				}

				return success;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsProvisioningPolicyService -operationName?createPolicy -principal?<username> -credential?<password> -orgUnitName?<orgOU> - date?<date>"
						+ "\n for example: wsProvisioningPolicyService -operationName?createPolicy -principal?\"itim manager\" -credential?secret -orgUnitName?orgOU1 -date?10/12/2009";
			}
		},

		GETPOLICIES {

			@Override
			public boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";
				boolean success = true;
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String orgUnitName = (String) map.get(ORG_UNIT_NAME);
				String policyName = (String) map.get(PARAM_POLICY_NAME);

				WSSession session = loginIntoITIM(principal, credential);
				WSProvisioningPolicyService wsProvisioningPolicyService = getProvisioningPolicyService();
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				List<WSOrganizationalContainer> lstWSOrganizationContainers = wsOrgContainerService
						.searchContainerByName(session, null,
								ORG_CONTAINER_PROFILE_NAME, orgUnitName);
				WSOrganizationalContainer parentWSOrgContainer = null;
				if (lstWSOrganizationContainers != null
						&& lstWSOrganizationContainers.size() > 0) {
					// We have found some organizational containers
					// Get the first container and proceed
					parentWSOrgContainer = lstWSOrganizationContainers.get(0);
					List<WSProvisioningPolicy> policies = wsProvisioningPolicyService
							.getPolicies(session, parentWSOrgContainer,
									policyName);
					Utils.printMsg(WSProvisioningPolicyServiceClient.class
							.getName(), methodName, this.name(),
							"No of policies found : " + policies.size());
					for (WSProvisioningPolicy wsp : policies) {
						Utils.printMsg(WSProvisioningPolicyServiceClient.class
								.getName(), methodName, this.name(),
								"Name of policy: " + wsp.getName());
					}
				} else {
					Utils
							.printMsg(WSProvisioningPolicyServiceClient.class
									.getName(), methodName, this.name(),
									"The parent organization unit passed in not valid.");

					success = false;
				}

				return success;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsProvisioningPolicyService -operationName?getPolicies -principal?<username> -credential?<password> -orgUnitName?<orgOU> - policyName?<policyName>"
						+ "\n for example: wsProvisioningPolicyService -operationName?getPolicies -principal?\"itim manager\" -credential?secret -orgUnitName?orgOU1 -policyName?Policy";
			}
		};

		abstract boolean execute(Map<String, Object> mpParams) throws Exception;

		abstract String getUsage(String errMessage);

		/**
		 * Get a sample provisioning policy
		 * 
		 * @return WSProvisioningPolicy
		 */
		WSProvisioningPolicy getProvisioningPolicy() {

			WSProvisioningPolicy pp = new WSProvisioningPolicy();
			pp.setName("Test Policy");
			pp.setEnabled(true);
			pp.setPriority(1);

			ArrayOfTns1WSProvisioningPolicyEntitlement arrEntitlements = new ArrayOfTns1WSProvisioningPolicyEntitlement();

			WSProvisioningPolicyEntitlement entitlement = new WSProvisioningPolicyEntitlement();

			WSServiceTarget serviceTarget = new WSServiceTarget();
			serviceTarget.setType(ServiceTarget.TYPE_ALL);
			entitlement.setServiceTarget(serviceTarget);

			entitlement.setType(Entitlement.ENTITLEMENT_TYPE_AUTHORIZED);

			arrEntitlements.getItem().add(entitlement);
			pp.setEntitlements(arrEntitlements);

			ArrayOfTns1WSProvisioningPolicyMembership arrMemberships = new ArrayOfTns1WSProvisioningPolicyMembership();

			WSProvisioningPolicyMembership membership = new WSProvisioningPolicyMembership();
			membership.setName("*");
			membership.setType(Membership.TYPE_ALL_PERSONS);

			arrMemberships.getItem().add(membership);
			pp.setMembership(arrMemberships);

			return pp;
		}

		WSSession loginIntoITIM(String principal, String credential)
				throws WSInvalidLoginException, WSLoginServiceException {
			String methodName = "loginIntoITIM";
			WSSessionService proxy = getSessionService();
			WSSession session = proxy.login(principal, credential);
			if (session != null) {
				Utils.printMsg(WSProvisioningPolicyServiceClient.class
						.getName(), methodName, this.name(), "Session Id: "
						+ session.getSessionID());
			} else {
				Utils.printMsg(WSProvisioningPolicyServiceClient.class
						.getName(), methodName, this.name(), "Invalid session");
			}
			return session;
		}

	}

	public static void main(String[] args) {
		WSProvisioningPolicyServiceClient wsPSClient = new WSProvisioningPolicyServiceClient();
		wsPSClient.run(args, true);
	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSPROVISIONINGPOLICY_OPERATIONS operation : WSPROVISIONINGPOLICY_OPERATIONS
					.values()) {
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


}
