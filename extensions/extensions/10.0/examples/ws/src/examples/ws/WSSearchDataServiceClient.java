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
 * This is an example client to exercise WSSearchDataService API methods. The example supports some of the 
 * operations the other operations are not included for the sake of brevity but they can be easily 
 * implemented by following the implementation details of the four operations provided in this class. 
 * 
 * @author prakash_chavan
 */
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ibm.itim.ws.model.WSDelegatePerson;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSSearchArguments;
import com.ibm.itim.ws.model.WSSelectItem;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSSearchDataService;
import com.ibm.itim.ws.services.WSSessionService;

public class WSSearchDataServiceClient extends GenericWSClient {

	private static final String PARAM_SEARCH_BASE = "base";
	private static final String PARAM_SEARCH_FILTER = "filter";
	private static final String PARAM_SEARCH_CATEGORY = "category";
	private static final String PARAM_SEARCH_RETURN_ATTR = "returnAttribute";
	private static final String PARAM_OBJECTCLASS_NAME = "objectClassName";
	private static final String PARAM_ATTRIBUTE_LIST = "attributeList";
	private static final String COMMA_DELIM = ",";
	private static final String HOST="host";
	private static final String PORT="port";
	
	enum WSSYSTEMUSERSERVICE_OPERATIONS {
		
		FINDSEARCHFILTEROBJECTS {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String base = (String) map.get(PARAM_SEARCH_BASE);
				String filter = (String) map.get(PARAM_SEARCH_FILTER);
				String category = (String) map.get(PARAM_SEARCH_CATEGORY);
				String returnAttr = (String) map.get(PARAM_SEARCH_RETURN_ATTR);

				WSSession session = loginIntoITIM(principal, credential);
				WSSearchDataService searchDataService = getSearchDataService();
				WSSearchArguments wsSearchArgs = new WSSearchArguments();
				wsSearchArgs.setBase(base);
				wsSearchArgs.setCategory(category);
				wsSearchArgs.setReturnedAttributeName(returnAttr);
				wsSearchArgs.setFilter(filter);
				List<String> strList = searchDataService.findSearchFilterObjects(session, wsSearchArgs);

				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "findSearchFilterObjects: "
								+ strList.toString());
				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSearchDataService -operationName?findSearchFilterObjects -principal?username -credential?password -base?search_base -filter?ldap_filter -category?search_category -returnAttribute?return_attribute_name"
						+ "\nFor example: wsSearchDataService -operationName?findSearchFilterObjects -principal?\"itim manager\" -credential?\"secret\" -base?\"global\" or \"org\" -filter?\"(sn=*)\" -category?\"Person\" -returnAttribute?\"cn\"";

			}
		},
	
		GETATTRIBUTENAMES {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String objectClassName = (String) map.get(PARAM_OBJECTCLASS_NAME);

				WSSession session = loginIntoITIM(principal, credential);
				WSSearchDataService searchDataService = getSearchDataService();
				List<WSSelectItem> wsSelectItemList = searchDataService.getAttributeNames(session, objectClassName);

				StringBuffer sb = new StringBuffer();
				for (WSSelectItem wsSItem: wsSelectItemList){
					sb.append("Name: " + wsSItem.getLabel()) ;
					sb.append(" Value: " + wsSItem.getValue());
					sb.append("\r\n");
				}
				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "getAttributeNames: "
								+ sb.toString());
				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSearchDataService -operationName?getAttributeNames -principal?username -credential?password -objectClassName?objectClassName"
						+ "\nFor example: wsSearchDataService -operationName?getAttributeNames -principal?\"itim manager\" -credential?\"secret\" -objectClassName?\"erpersonitem\"";

			}
		},
		
		SEARCHFORDELEGATES {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) map.get(PARAM_SEARCH_FILTER);
				String attrListStr = (String) map.get(PARAM_ATTRIBUTE_LIST);

				WSSession session = loginIntoITIM(principal, credential);
				String[] attrListArray = attrListStr.split(COMMA_DELIM);
				List<String> attrList = Arrays.asList(attrListArray);
				
				WSSearchDataService searchDataService = getSearchDataService();
				List<WSDelegatePerson> wsDelegatePersonList = searchDataService.searchForDelegates(session, filter, attrList);

				StringBuffer sb = new StringBuffer();
				for (WSDelegatePerson wsDelegatePerson: wsDelegatePersonList){
					sb.append("Name: " + wsDelegatePerson.getName()) ;
					sb.append(" Delegators: " + wsDelegatePerson.getSystemUsers().getItem().toString());
					sb.append("\r\n");
				}
				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "searchForDelegates: "
								+ sb.toString());
				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSearchDataService -operationName?searchForDelegates -principal?username -credential?password -filter?filter -attributeList?attr1,attr2,attr3"
						+ "\nFor example: wsSearchDataService -operationName?searchForDelegates -principal?\"itim manager\" -credential?\"secret\" -filter?\"(sn=*)\" -attributeList?\"cn,sn\"";

			}
		},
		
		

		SEARCHPERSONSFROMROOT {

			@Override
			boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";

				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String filter = (String) map.get(PARAM_SEARCH_FILTER);
				String attrListStr = (String) map.get(PARAM_ATTRIBUTE_LIST);
				List<String> attrList=null;

				WSSession session = loginIntoITIM(principal, credential);
				if(attrListStr!=null){
					String[] attrListArray = attrListStr.split(COMMA_DELIM);
					attrList = Arrays.asList(attrListArray);
				}

				WSSearchDataService searchDataService = getSearchDataService();
				List<WSPerson> wsPersonList = searchDataService.searchPersonsFromRoot(session, filter, attrList);

				for (WSPerson wsPersonItem: wsPersonList){
				Utils.printMsg(WSSystemUserServiceClient.class.getName(),
						methodName, this.name(), "Person Name: "
								+ wsPersonItem.getName());
				}
				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsSearchDataService -operationName?searchPersonsFromRoot -principal?username -credential?password -filter?filter -attributeList?attr1,attr2,attr3"
						+ "\nFor example: wsSearchDataService -operationName?searchPersonsFromRoot -principal?\"itim manager\" -credential?\"secret\" -filter?\"(sn=*)\" -attributeList?\"cn,sn\"";

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
				Utils.printMsg(WSSearchDataServiceClient.class.getName(),
						methodName, this.name(), "Session Id: "
								+ session.getSessionID());
			} else {
				Utils.printMsg(WSSearchDataServiceClient.class.getName(),
						methodName, this.name(), "Invalid session");
			}
			return session;
		}
		
	}

	@Override
	public boolean executeOperation(Map<String, Object> map) {
		boolean retCode = false;
		String operationName = (String) map.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);

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
		WSSearchDataServiceClient client = new WSSearchDataServiceClient();
		client.run(args, true);
	}

}
