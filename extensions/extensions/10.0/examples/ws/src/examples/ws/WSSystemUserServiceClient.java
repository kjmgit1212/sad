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
 * This is an example client to exercise WSSystemUserService API methods. The example supports
 * five operations viz. getSystemUser, getSystemRoleNames, addDelegate, getDelegates and setChallengeResponseInfo. 
 * The other operations are not included for the sake of brevity but they can be easily implemented
 * by following the implementation details of the five operations provided in this class. 
 * 
 * @author prakash_chavan
 * 
 */
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.ws.model.WSChallengeResponseInfo;
import com.ibm.itim.ws.model.WSDelegate;
import com.ibm.itim.ws.model.WSDelegateInfo;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.model.WSSystemUser;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSSystemUserService;


public class WSSystemUserServiceClient extends GenericWSClient {

	private static final String PARAM_CR_QUESTIONS = "crQuestions";
	private static final String PARAM_CR_ANSWERS = "crAnswers";
	private static final String PARAM_ITIMACCOUNT_ID = "itimAccountID";
	private static final String PARAM_DELEGATE_ITIMACCOUNT_ID = "delegateItimAccountID";
	private static final String COMMA_DELIM = ",";

	enum WSSYSTEMUSERSERVICE_OPERATIONS {
		
		GETSYSTEMUSER {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				WSSystemUserService sysUserService = getSystemUserService();
				WSSystemUser wsSysUser = sysUserService.getSystemUser(session);

				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "System User Name: "
								+ wsSysUser.getName());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSystemUserService -operationName?getSystemUser -principal?username -credential?password"
						+ "\nFor example: wsSystemUserService -operationName?getSystemUser -principal?\"itim manager\" -credential?\"secret\"";

			}
		},
	
		GETSYSTEMROLENAMES {
			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				String accountListParam = (String) map.get(PARAM_ITIMACCOUNT_ID);
				List<String> accountDNs = getAccountDNs(session,
						accountListParam);
				String systemUserDN = accountDNs.get(0);
				WSSystemUserService sysUserService = getSystemUserService();
				List<String> sysRoleList = sysUserService.getSystemRoleNames(session, systemUserDN);

				
				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "System Role List:"
								+ sysRoleList.toString());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSystemUserService -operationName?getSystemRoleNames -principal?username -credential?password -itimAccountID?itimAccountID"
						+ "\nFor example: wsSystemUserService -operationName?getSystemRoleNames -principal?\"itim manager\" -credential?secret -itimAccountID?\"ITIM Manager\"";

			}
		},

		ADDDELEGATE {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				
				String delegateITIMAccountIDs = (String) map.get(PARAM_DELEGATE_ITIMACCOUNT_ID);
								
				WSSystemUserService sysUserService = getSystemUserService();
				
				WSSystemUser delegator = sysUserService.getSystemUser(session);
				WSDelegate delegate = createDelegate(session, delegateITIMAccountIDs);
				
				sysUserService.addDelegate(session, delegator, delegate);
				
				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "Add Delegate Done.");
				
				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSystemUserService -operationName?addDelegate -principal?username -credential?password -delegateItimAccountID?delegateItimAccountID"
						+ "\nFor example: wsSystemUserService -operationName?addDelegate -principal?\"itim manager\" -credential?\"secret\" -delegateItimAccountID?\"jHill\"";

			}
		},
		
		SETCHALLENGERESPONSEINFO {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				List<WSChallengeResponseInfo> newCRs = createCRInfos((String)map.get(PARAM_CR_QUESTIONS), (String)map.get(PARAM_CR_ANSWERS));
				
				WSSystemUserService sysUserService = getSystemUserService();
				sysUserService.setChallengeResponseInfo(session, newCRs);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "New Challenge-Response Set.");


				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSystemUserService -operationName?setChallengeResponseInfo -principal?username -credential?password -crQuestions?Question1,Question2 - crAnswers?Answer1,Answer2"
						+ "\nFor example: wsSystemUserService -operationName?setChallengeResponseInfo -principal?\"itim manager\" -credential?\"secret\" -crQuestions?What is your favorite city,What is your favorite dish - crAnswers?Paris,Sizzling brownie";

			}
		},

		GETDELEGATES {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				WSSystemUserService sysUserService = getSystemUserService();
				WSSystemUser delegator = sysUserService.getSystemUser(session);
				List<WSDelegateInfo> wsDelegateInfo = sysUserService.getDelegates(session, delegator);

				Utils.printMsg(WSPasswordServiceClient.class.getName(),
						methodName, this.name(), "No of delegates found" + wsDelegateInfo.size());


				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSystemUserService -operationName?getDelegates -principal?username -credential?password"
						+ "\nFor example: wsSystemUserService -operationName?getDelegates -principal?\"itim manager\" -credential?\"secret\"";

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

		WSDelegate createDelegate(WSSession session, String itimUser) throws Exception{
			WSDelegate wsDelegate = new WSDelegate();
			XMLGregorianCalendar startDate = null;
			XMLGregorianCalendar endDate = null;

			startDate = getCalendar(false);
			endDate = getCalendar(true);
			//find itim user DN
			wsDelegate.setItimDN(getAccountDNs(session, itimUser).get(0));
			wsDelegate.setStartDate(startDate);
			wsDelegate.setEndDate(endDate);
			return wsDelegate;
		}
		
		List<WSChallengeResponseInfo> createCRInfos(String questions, String answers){
			List<WSChallengeResponseInfo> wsCRIList = new ArrayList<WSChallengeResponseInfo>();
			
			String[] questionsList = questions.split(COMMA_DELIM);
			String[] answersList = answers.split(COMMA_DELIM);
			
			for (int i = 0; i < questionsList.length; i++){
				WSChallengeResponseInfo wsCRInfo = new WSChallengeResponseInfo();
				wsCRInfo.setQuestion(questionsList[i]);
				wsCRInfo.setAnswer(answersList[i]);
				wsCRIList.add(wsCRInfo);
			}
			 
			return wsCRIList;
		}
		
		XMLGregorianCalendar getCalendar(boolean oneDayLater) throws DatatypeConfigurationException{
			XMLGregorianCalendar date;
			
			date = DatatypeFactory.newInstance().newXMLGregorianCalendar();
			
			GregorianCalendar calendar = new GregorianCalendar();
			date = DatatypeFactory.newInstance()
			.newXMLGregorianCalendar(calendar);
			
			if (oneDayLater){
				date.setDay(date.getDay() + 1);
			}
			return date;
		}
	}

	@Override
	public boolean executeOperation(Map<String, Object> map) {
		boolean retCode = false;
		String operationName = (String) map.get(OPERATION_NAME);

		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSSYSTEMUSERSERVICE_OPERATIONS operation : WSSYSTEMUSERSERVICE_OPERATIONS
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
		WSSystemUserServiceClient client = new WSSystemUserServiceClient();
		client.run(args, true);
	}

}
