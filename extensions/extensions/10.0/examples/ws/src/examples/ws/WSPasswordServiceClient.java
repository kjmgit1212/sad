/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2005, 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/
package examples.ws;

/**
 * This is an example client to exercise WSPasswordService API methods. The example supports
 * for operations viz. changePassword, generatePassword, getPasswordRules and selfChangePassword. 
 * The other operations are not included for the sake of brevity but they can be easily implemented
 * by following the implementation details of the four operations provided in this class. 
 * 
 * @author sandip_dey
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.ibm.itim.ws.model.WSPasswordRulesInfo;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSPasswordService;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.services.WSSessionService;


public class WSPasswordServiceClient extends GenericWSClient {

	private static final String PARAM_ACCOUNT_IDS = "accountIDs";
	private static final String PARAM_NEW_PASSWORD = "newPassword";
	private static final String PARAM_OLD_PASSWORD = "oldPassword";
	private static final String PARAM_ITIMACCOUNT_ID = "itimAccountID";
	private static final String COMMA_DELIM = ",";

	enum WSPASSWORDSERVICE_OPERATIONS {
		
		CHANGEPASSWORD {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				String accountListParam = (String) map.get(PARAM_ACCOUNT_IDS);
				List<String> accountDNs = getAccountDNs(session,
						accountListParam);

				String newPassword = (String) map.get(PARAM_NEW_PASSWORD);

				WSPasswordService passwdService = getPasswordService();
				WSRequest request = passwdService.changePassword(session,
						accountDNs, newPassword);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Request ID: "
								+ request.getRequestId() + "\nStatus: "
								+ request.getStatus() + "\nResult: "
								+ request.getResult());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsPasswordService -operationName?changePassword -principal?username -credential?password -accountIDs?accountID1,accountID2,... -newPassword?newAccountPassword"
						+ "\nFor example: wsPasswordService -operationName?changePassword -principal?\"itim manager\" -credential?secret -accountIDs?\"account1,account2\" -newPassword?newpasswd";

			}
		},

		GENERATEPASSWORD {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				String accountListParam = (String) map.get(PARAM_ACCOUNT_IDS);
				List<String> accountDNs = getAccountDNs(session,
						accountListParam);

				WSPasswordService passwdService = getPasswordService();
				String password = passwdService.generatePassword(session,
						accountDNs);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Generated password: "
								+ password);

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsPasswordService -operationName?generatePassword -principal?username -credential?password -accountIDs?accountID1,accountID2,..."
						+ "\nFor example: wsPasswordService -operationName?generatePassword -principal?\"itim manager\" -credential?secret -accountIDs?\"account1,account2\"";

			}
		},

		GETPASSWORDRULES {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				String accountListParam = (String) map.get(PARAM_ACCOUNT_IDS);
				List<String> accountDNs = getAccountDNs(session,
						accountListParam);
				
				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Account DNs: "
								+ accountDNs);

				WSPasswordService passwdService = getPasswordService();
				WSPasswordRulesInfo wsRuleInfo = passwdService
						.getPasswordRules(session, accountDNs);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Password Rules Info: "
								+ wsRuleInfo.toString());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsPasswordService -operationName?getPasswordRules -principal?username -credential?password -accountIDs?accountID1,accountID2,..."
						+ "\nFor example: wsPasswordService -operationName?getPasswordRules -principal?\"itim manager\" -credential?secret -accountIDs?\"account1,account2\"";

			}
		},
		
		SELFCHANGEPASSWORD {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String itimAccountID = (String) map.get(PARAM_ITIMACCOUNT_ID);
				String oldPassword = (String) map.get(PARAM_OLD_PASSWORD);
				String newPassword = (String) map.get(PARAM_NEW_PASSWORD);

				WSPasswordService passwdService = getPasswordService();
				WSRequest request = passwdService.selfChangePassword(
						itimAccountID, oldPassword, newPassword);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Request ID: "
								+ request.getRequestId() + "\nStatus: "
								+ request.getStatus() + "\nResult: "
								+ request.getResult());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsPasswordService -operationName?selfChangePassword -itimAccountID?itimID -oldPassword?oldPasswd -newPassword?newPasswd"
						+ "\nFor example: wsPasswordService -operationName?selfChangePassword -itimAccountID?\"itim manager\" -oldPassword?secret -newPassword?secret1";

			}
		}

		;

		abstract boolean execute(Map<String, Object> map) throws Exception;

		abstract String getUsage();

		WSSession loginIntoITIM(String principal, String credential)
				throws WSInvalidLoginException, WSLoginServiceException {

			String methodName = "loginIntoITIM";
			WSSessionService proxy = getSessionService();
			WSSession session = proxy.login(principal, credential);

			if (session != null) {
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						methodName, this.name(), "Session Id: "
								+ session.getSessionID());
			} else {
				Utils.printMsg(WSServiceServiceClient.class.getName(),
						methodName, this.name(), "Invalid session");
			}

			return session;
		}

		List<String> getAccountDNs(WSSession session, String accountList)
				throws Exception {
			List<String> accountDNs = new ArrayList<String>();
			
			WSAccountService proxy = getAccountService();
			StringBuffer filter = new StringBuffer();
			StringTokenizer st = new StringTokenizer(accountList, COMMA_DELIM);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				WSSearchArguments searchArgs = new WSSearchArguments();
				filter.append("(");
				filter.append("eruid=");
				filter.append(token);
				filter.append(")");
				searchArgs.setFilter(filter.toString());
				String accountDN = proxy.searchAccounts(session, searchArgs)
						.get(0).getItimDN();
				accountDNs.add(accountDN);
				filter.setLength(0);
			}

			return accountDNs;
		}

	}

	@Override
	public boolean executeOperation(Map<String, Object> map) {
		boolean retCode = false;
		String operationName = (String) map.get(OPERATION_NAME);

		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSPASSWORDSERVICE_OPERATIONS operation : WSPASSWORDSERVICE_OPERATIONS
					.values()) {
				if (operation.name().equals(operationName)) {
					try {
						retCode = operation.execute(map);
						break;
					} catch (Exception e) {
						e.printStackTrace();
						operation.getUsage();
					}
				}
			}
		}
		return retCode;
	}


	/**
	 * Run the client in the verbose mode
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		WSPasswordServiceClient client = new WSPasswordServiceClient();
		client.run(args, true);
	}

}
