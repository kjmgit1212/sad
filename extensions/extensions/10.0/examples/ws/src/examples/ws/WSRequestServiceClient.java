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

import java.util.List;
import java.util.Map;

import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSRequestService;
import com.ibm.itim.ws.services.WSSessionService;




/**
 * This is an example client to exercise WSRequestService API methods. The example supports
 * four operations viz. getPendingRequests, getRequest, getProcess, getCompletedRequests. 
 * The other operations are not included for the sake of brevity but they can be easily implemented
 * by following the implementation details of the four operations provided in this class. 
 * 
 * @author prakash_chavan
 * 
 */

public class WSRequestServiceClient extends GenericWSClient {
	
	private static final String PARAM_REQUEST_ID = "requestId";
	
	private static final String PARAM_PROCESS_ID = "processId";
	
	private static final String PARAM_TIME_TYPE = "timeType";
	
	private static final String PARAM_DATE_INTERVAL_TYPE = "dateIntervalType";
	
	
	
enum WSREQUESTSERVICE_OPERATIONS {
		
		GETPENDINGREQUESTS {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				WSRequestService requestService = getRequestService();
				
				List<WSRequest> lstRequests = requestService.getPendingRequests(session);

				Utils.printMsg(WSRequestServiceClient.class.getName(),
						methodName, this.name(), "Number of pending requests found : " + lstRequests.size());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsRequestService -operationName?getPendingRequests -principal?username -credential?password"
						+ "\nFor example: wsRequestService -operationName?getPendingRequests -principal?\"itim manager\" -credential?\"secret\"";

			}
		},
		
		GETREQUEST {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				WSRequestService requestService = getRequestService();
				
				WSRequest wsRequest  = requestService.getRequest(session, Long.parseLong((String)map.get(PARAM_REQUEST_ID)));

				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "requestee : "
								+ wsRequest.getRequestee());


				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsRequestService -operationName?getRequest -principal?username -credential?password -requestId?requestID"
						+ "\nFor example: wsRequestService -operationName?getRequest -principal?\"itim manager\" -credential?\"secret\" -requestId?\"8384334581924816405\"";

			}
		},
		GETCOMPLETEDREQUESTS {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				WSRequestService requestService = getRequestService();
				
				
				int timeType = Integer.parseInt((String) map.get(PARAM_TIME_TYPE));
				int dateIntervalType = Integer.parseInt((String) map.get(PARAM_DATE_INTERVAL_TYPE));
												
				List<WSRequest> lstRequests = requestService.getCompletedRequests(session, timeType, dateIntervalType);

				Utils.printMsg(WSRequestServiceClient.class.getName(),
						methodName, this.name(), "No of completed requests found" + lstRequests.size());

				return true;
			}

			@Override
			String getUsage() {
				/*Please refer to the WorkFlowQuery class to get the constants needed for this API*/
				return "Usage: wsRequestService -operationName?getCompletedRequests -principal?username -credential?password -timeType?timeType -dateIntervalType?dateIntervalType"
						+ "\nFor example: wsRequestService -operationName?getCompletedRequests -principal?\"itim manager\" -credential?\"secret\" -timeType?\"2\" -dateIntervalType?\"3\"  ";

			}
		},
		GETPROCESS {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);
				WSRequestService requestService = getRequestService();
								
				long processId = Long.parseLong((String) map.get(PARAM_PROCESS_ID));
																
				WSRequest wsRequest = requestService.getProcess(session, processId);

				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "requestee : "
								+ wsRequest.getRequestee());

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsRequestService -operationName?getProcess -principal?username -credential?password -processId?processId"
						+ "\nFor example: wsRequestService -operationName?getProcess -principal?\"itim manager\" -credential?\"secret\" -prcessId?\"123123\" ";

			}
		};
		
		
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

}

@Override
public boolean executeOperation(Map<String, Object> map) {
	boolean retCode = false;
	String operationName = (String) map.get(OPERATION_NAME);

	if (operationName != null && operationName.length() > 0) {
		operationName = operationName.toUpperCase();
		for (WSREQUESTSERVICE_OPERATIONS operation : WSREQUESTSERVICE_OPERATIONS
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
	WSRequestServiceClient client = new WSRequestServiceClient();
	client.run(args, true);
}



}
