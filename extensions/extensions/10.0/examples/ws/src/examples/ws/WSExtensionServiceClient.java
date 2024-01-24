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

import com.ibm.itim.ws.client.util.xml.XMLBeanReader;
import com.ibm.itim.ws.client.util.xml.XMLBeanWriter;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSExtensionService;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSPersonServiceService;
import com.ibm.itim.ws.services.WSSessionService_Service;

public class WSExtensionServiceClient extends GenericWSClient {
	private static final String PARAM_CLASSNAME = "className";
	private static final String PARAM_METHODNAME = "methodName";

	enum WSEXTENSIONSERVICE_OPERATIONS {
		EXTENDWITHXML {
			public boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);

				WSSession session = loginIntoITIM(principal, credential);

				WSPersonServiceService service = new WSPersonServiceService();
				WSPersonService port = service.getWSPersonService();
				// Assuming that a person with Full Name as 'John Smith' exist
				// and that this person is owner of few services in IBM Security
				// Identity Manager.
				String filter = "(cn=John Smith)";
				List<WSPerson> searchResults = port.searchPersonsFromRoot(
						session, filter, null);
				if (searchResults == null) {
					Utils.printMsg(WSExtensionServiceClient.class.getName(),
							"execute", this.name(),
							"A person 'John Smith' must exist.");
					return false;
				}

				WSPerson wsPerson = searchResults.get(0);
				// Convert the object to XML string
				String xmlParam = "";
				xmlParam = XMLBeanWriter.writeXMLBean(wsPerson);
				WSExtensionService extensionService = getExtensionService();
				// Execute the sample logic to get the service group names of
				// the services which 'John Smith' owns
				String resultStr = extensionService.extendWithXML(session,
						"sample.extension.SampleWSExtension",
						"GetServiceGroups", xmlParam);
				List<String> result = (List<String>) XMLBeanReader.readXMLBean(
						resultStr, List.class);

				for (String str : result) {
					str = str.trim();
					int i = str.indexOf('|');
					String str1 = str.substring(0, i);
					String str2 = str.substring(i+1);
					System.out.println("Service Name : " + str1);
					System.out.println("Group Name : " + str2);
				}
				return true;
			}

			String getUsage() {
				return "Usage: wsExtensionService -operationName?extendWithXML -principal?username -credential?password"
						+ "\nFor example: wsExtensionService -operationName?extendWithXML -principal?\"itim manager\" -credential?\"secret\"";
			}
		};
		abstract boolean execute(Map<String, Object> map) throws Exception;

		abstract String getUsage();

		WSSession loginIntoITIM(String principal, String credential)
				throws WSInvalidLoginException, WSLoginServiceException {
			String methodName = "loginIntoITIM";

			WSSessionService_Service sessionService = new WSSessionService_Service();
			WSSession session = sessionService.getWSSessionServicePort().login(
					principal, credential);

			if (session != null) {
				Utils.printMsg(WSExtensionServiceClient.class.getName(),
						methodName, this.name(), "Session Id: "
								+ session.getSessionID());
			} else {
				Utils.printMsg(WSExtensionServiceClient.class.getName(),
						methodName, this.name(), "Invalid session");
			}
			return session;
		}

	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);

		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSEXTENSIONSERVICE_OPERATIONS operation : WSEXTENSIONSERVICE_OPERATIONS
					.values()) {
				if (operation.name().equalsIgnoreCase(operationName)) {
					try {
						retCode = operation.execute(mpParams);
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


	public static void main(String[] args) {
		WSExtensionServiceClient client = new WSExtensionServiceClient();
		client.run(args, true);
	}

}
