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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSRole;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSSessionService;


/**
 * @author administrator
 * 
 */

public class WSRoleServiceClient extends GenericWSClient {

	private static final String PARAM_ITIM_CREDENTIAL = "credential";
	private static final String PARAM_ITIM_PRINCIPAL = "principal";
	private static final String PARAM_ROLE_FILTER = "filter";
	private static final String PARAM_ROLE_NAME = "roleName";
	private static final String PARAM_ROLE_DESCRIPTION = "roleDescription";
	private static final String PARAM_ORG_CONTAINER = "orgContainer";
	private static final String PARAM_ADD_ROLE_MEMBER = "addMembRoles";
	private static final String PARAM_REMOVE_ROLE_MEMBER = "removeMembRoles";

	@Resource
	// TODO: For usage we will create an enum which will have the getUsage
	// method implementation in it.
	enum WSROLESERVICE_OPERATIONS {

		LOOKUPROLE {
			public boolean execute(Map<String, Object> mpParams)
					throws Exception {

				String sFilterParam = (String) mpParams.get(PARAM_ROLE_FILTER);
				if (sFilterParam == null) {
					Utils
							.printMsg(WSRoleServiceClient.class.getName(),
									"execute", this.name(),
									"No Filter parameter passed to search for the role.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSRoleService roleService = getRoleService();

				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession,
						sFilterParam);
				if (lstWSRoles != null && lstWSRoles.size() > 0) {
					Iterator<WSRole> it = lstWSRoles.iterator();
					while (it.hasNext()) {
						WSRole role = it.next();
					String roleDN = role.getItimDN();
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(), "Getting roles for user  "
									+ role.getName());

					WSRole wsRole = roleService.lookupRole(wsSession, roleDN);
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(), "\n Role Name : "
									+ wsRole.getName() + "\n Person DN : "
									+ wsRole.getItimDN());
					}
					return true;
				} else {

					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No role found matching the filter : "
									+ sFilterParam);
					return false;
				}
			}

			// Need to complete this
			public String getUsage(String errMsg) {
				return "usage: wsRoleService -operationName?lookupRole -principal?<username> -credential?<password> -filter?\"(erRoleName=rolename)\""
						+ "\n for example: wsRoleService -operationName?lookupRole -principal?\"itim manager\" -credential?secret -filter?\"(errolename=*)\"";
			}
		},

		LOOKUPSYSTEMROLE {
			public boolean execute(Map<String, Object> mpParams)
					throws Exception {

				String sFilterParam = (String) mpParams.get(PARAM_ROLE_FILTER);
				if (sFilterParam == null) {
					Utils
							.printMsg(WSRoleServiceClient.class.getName(),
									"execute", this.name(),
									"No Filter parameter passed to search for the role.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSRoleService roleService = getRoleService();

				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession,
						sFilterParam);
				if (lstWSRoles != null && lstWSRoles.size() > 0) {

					WSRole role = lstWSRoles.get(0);
					String roleDN = role.getItimDN();
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(), "Getting roles for user  "
									+ role.getName());

					WSRole wsRole = roleService.lookupSystemRole(wsSession,
							roleDN);
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(), "\n Role Name : "
									+ wsRole.getName() + "\n Person DN : "
									+ wsRole.getItimDN());

					return role.getName().equals(wsRole.getName());
				} else {

					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No role found matching the filter : "
									+ sFilterParam);
					return false;
				}
			}

			// Need to complete this
			public String getUsage(String errMsg) {
				return "usage: wsRoleService -operationName?lookupSystemRole -principal?<username> -credential?<password> -filter?\"(erRoleName=rolename)\" [additional filter criteria attributes]"
						+ "\n for example: wsRoleService -operationName?lookupSystemRole -principal?\"itim manager\" -credential?secret -filter?\"(errolename=*)\"";
			}
		},

		CREATESTATICROLE {
			public boolean execute(Map<String, Object> mpParams)
					throws Exception {
				String roleName = (String) mpParams.get(PARAM_ROLE_NAME);
				if (roleName == null) {
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No name parameter passed to create the role.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				WSRole wsRole = new WSRole();
				wsRole.setName(roleName);
				String description = (String) mpParams
						.get(PARAM_ROLE_DESCRIPTION);
				if (description != null) {
					wsRole.setDescription(description);
				}
				
				ArrayOfTns1WSAttribute attr = new ArrayOfTns1WSAttribute();
				wsRole.setAttributes(attr);

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSRoleService roleService = getRoleService();
				WSOrganizationalContainerService port = getOrganizationalContainerService();
				WSOrganizationalContainer wsContainer = null;
				String containerName = (String) mpParams
						.get(PARAM_ORG_CONTAINER);
				if (containerName != null) {
					List<WSOrganizationalContainer> lstWSOrgContainers = port
							.searchContainerByName(wsSession, null,
									"OrganizationalUnit", containerName);

					if (lstWSOrgContainers != null
							&& !lstWSOrgContainers.isEmpty()) {
						wsContainer = lstWSOrgContainers.get(0);
					} else {
						System.out.println("No container found matching "
								+ containerName);
						return false;
					}
				}

				roleService.createStaticRole(wsSession, wsContainer, wsRole);
				Utils.printMsg(WSRoleServiceClient.class.getName(), "execute",
						this.name(),
						"Create static role request submitted successfully.");

				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsRoleService -operationName?createStaticRole "
						+ "-principal?<username> -credential?<password> -roleName?<role name>"
						+ "-roleDescription?<role description> -orgContainer?<Name of the Organizational Container in which to create the role>"
						+ "\n for example: wsRoleService -operationName?createStaticRole -principal?\"itim manager\" -credential?secret -roleName?\"Sample static role\" -roleDescription?\"Sample static role\" -orgContainer?Organization";
			}

		},

		GETMEMBERROLES {
			public boolean execute(Map<String, Object> mpParams)
					throws Exception {
				String roleName = (String) mpParams.get(PARAM_ROLE_NAME);
				if (roleName == null) {
					Utils
							.printMsg(WSRoleServiceClient.class.getName(),
									"execute", this.name(),
									"No name parameter passed to get the member roles.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSRoleService roleService = getRoleService();

				String roleSearchfilter = "(errolename=" + roleName + ")";
				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession,
						roleSearchfilter);
				if (lstWSRoles != null && lstWSRoles.size() > 0) {

					WSRole role = lstWSRoles.get(0);
					String roleDN = role.getItimDN();
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"Getting role with the specified name  " + roleDN);

					List<WSRole> membRoles = roleService.getMemberRoles(
							wsSession, roleDN);
					for (WSRole tempRole : membRoles) {
						Utils.printMsg(WSRoleServiceClient.class.getName(),
								"execute", this.name(), "Member roles: "
										+ tempRole.getName().toString());
					}
				} else {

					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No role found matching the name : " + roleName);
					return false;
				}
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsRoleService -operationName?getMemberRoles "
						+ "-principal?<username> -credential?<password> -roleName?<name of the superior role whose child roles are to be retrieved>"
						+ "\n for example: wsRoleService -operationName?getMemberRoles -principal?\"itim manager\" -credential?secret -roleName?\"Sample role\"";
			}
		},

		UPDATEROLEHIERARCHY {
			public boolean execute(Map<String, Object> mpParams)
					throws Exception {
				String roleName = (String) mpParams.get(PARAM_ROLE_NAME);
				if (roleName == null) {
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No role name parameter passed to update member roles.");
					String usage = this.getUsage("");
					System.out.println(usage);
					return false;
				}

				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSRoleService roleService = getRoleService();
				String dnOfRoleToUpdate = "";
				String roleSearchfilter = "(errolename=" + roleName + ")";
				List<WSRole> lstWSRoles = roleService.searchRoles(wsSession,
						roleSearchfilter);
				if (lstWSRoles != null && lstWSRoles.size() > 0) {
					WSRole role = lstWSRoles.get(0);
					String roleDN = role.getItimDN();
					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"Getting role with the specified name  " + roleDN);

					dnOfRoleToUpdate = roleDN;
				} else {

					Utils.printMsg(WSRoleServiceClient.class.getName(),
							"execute", this.name(),
							"No role found matching the name : " + roleName);
					return false;
				}

				String strAddRoleMembers = (String) mpParams
						.get(PARAM_ADD_ROLE_MEMBER);
				List<String> addMembers = null;
				if (strAddRoleMembers != null) {
					addMembers = getRoleDNList(wsSession, roleService,
							strAddRoleMembers);
					if (addMembers == null)
						return false;
				}

				String strRemoveRoleMembers = (String) mpParams
						.get(PARAM_REMOVE_ROLE_MEMBER);
				List<String> removeMembers = null;
				if (strRemoveRoleMembers != null) {
					removeMembers = getRoleDNList(wsSession, roleService,
							strRemoveRoleMembers);
					if (removeMembers == null)
						return false;
				}

				Date currDate = new Date();
				XMLGregorianCalendar date = Utils.long2Gregorian(currDate
						.getTime());
				roleService.updateRoleHierarchy(wsSession, dnOfRoleToUpdate,
						addMembers, removeMembers, date);
				Utils.printMsg(WSRoleServiceClient.class.getName(), "execute",
						this.name(),
						"Update role hierarchy request submitted successfully.");
				return true;
			}

			private List<String> getRoleDNList(WSSession wsSession,
					WSRoleService roleService, String stringOfRoleNames)
					throws Exception {
				List<String> roleDNList = new ArrayList<String>();
				String[] strArray = stringOfRoleNames.split(":");
				String roleSearchfilter = "";
				for (int i = 0; i < strArray.length; i++) {
					roleSearchfilter = "(errolename=" + strArray[i] + ")";
					List<WSRole> lstWSRoles = roleService.searchRoles(
							wsSession, roleSearchfilter);
					if (lstWSRoles != null && lstWSRoles.size() > 0) {
						WSRole role = lstWSRoles.get(0);
						String roleDN = role.getItimDN();
						Utils.printMsg(WSRoleServiceClient.class.getName(),
								"execute", this.name(),
								"Getting role with the specified name  "
										+ roleDN);

						roleDNList.add(roleDN);
					} else {

						Utils.printMsg(WSRoleServiceClient.class.getName(),
								"execute", this.name(),
								"No role found matching the name : "
										+ strArray[i]);
						return null;
					}
				}
				return roleDNList;
			}

			public String getUsage(String errMsg) {
				return "usage: wsRoleService -operationName?updateRoleHierarchy "
						+ "-principal?<username> -credential?<password> -roleName?<name of the superior role whose child roles are to be added, removed, or both>"
						+ "-addMembRoles?<string of role names, seperated with colon ':', that are to be added as members to the role> "
						+ "-removeMembRoles?<string of role names, seperated with colon ':', that are to be remove from members list of the role>"
						+ "\n for example: wsRoleService -operationName?updateRoleHierarchy -principal?\"itim manager\" "
						+ "\n -credential?secret -roleName?\"Sample Role\"; -addMembRoles?\"Role3:Role4\" -removeMembRoles?\"Role1:Role2\"";
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

		static WSROLESERVICE_OPERATIONS getRoleServiceOperation(String param) {
			return WSROLESERVICE_OPERATIONS.valueOf(param);
		}

		WSSession loginIntoITIM(String principal, String credential)
				throws WSInvalidLoginException, WSLoginServiceException {

			WSSessionService proxy = getSessionService();
			WSSession session = proxy.login(principal, credential);
			if (session != null)
				Utils.printMsg(WSRoleServiceClient.class.getName(),
						"loginIntoITIM", this.name(), "session id is "
								+ session.getSessionID());
			else
				Utils.printMsg(WSRoleServiceClient.class.getName(),
						"loginintoITIM", this.name(), "Invalid session");

			return session;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WSRoleServiceClient wsPSClient = new WSRoleServiceClient();
		wsPSClient.run(args, true);
	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSROLESERVICE_OPERATIONS operation : WSROLESERVICE_OPERATIONS
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
