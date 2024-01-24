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

import com.ibm.itim.ws.model.WSAssignment;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSEntityWrapper;
import com.ibm.itim.ws.model.WSRFIWrapper;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSToDoService;

/**
 * This is an example client to exercise WSToDoService API methods. The example
 * supports three operations viz. getAssignments, approveOrReject and getRFI.
 * The other operations are not included for the sake of brevity but they can be
 * easily implemented by following the implementation details of the three
 * operations provided in this class.
 * 
 * @author prakash_chavan
 * 
 */
public class WSToDoServiceClient extends GenericWSClient {

	private static final String PARAM_APPROVAL_STATUS = "approvalStatus";
	private static final String PARAM_EXPLANATION = "explanation";
	private static final String PARAM_ASSIGNMENT_ID = "assignmentID";
	private static final String PARAM_JUSTIFICATION = "justification";

	private enum WSTODOSERVICE_OPERATIONS {
		APPROVEORREJECT {
			@Override
			public boolean execute(Map<String, Object> map)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String approvalStatus = (String) map.get(PARAM_APPROVAL_STATUS);
				String explanation = (String) map.get(PARAM_EXPLANATION);
				String justification = (String)map.get(PARAM_JUSTIFICATION);

				WSSession session = loginIntoITIM(principal, credential);

				WSToDoService wsToDoService = getToDoService();
				List<WSAssignment> wsAssignmentList = wsToDoService
						.getAssignments(session);

				List<Long> activityIds = new ArrayList<Long>();
				for (WSAssignment assignment : wsAssignmentList) {
					activityIds.add(assignment.getId());
				}
				wsToDoService.approveOrReject(session, activityIds,
						approvalStatus, explanation,justification);

				Utils.printMsg(WSToDoServiceClient.class.getName(), methodName,
						this.name(), "Approve or reject completed.");

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsToDoService -operationName?approveOrReject -principal?username -credential?password -approvalStatus?approvalStatus -explanation?explanation"
						+ "\nFor example: wsToDoService -operationName?approveOrReject -principal?\"itim manager\" -credential?\"secret\" -approvalStatus?\"AA\" -explanation?\"approved\"";

			}

		},
		GETASSIGNMENTS {
			@Override
			public boolean execute(Map<String, Object> map)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				WSSession session = loginIntoITIM(principal, credential);

				WSToDoService wsToDoService = getToDoService();
				List<WSAssignment> wsAssignmentList = wsToDoService
						.getAssignments(session);

				Utils.printMsg(WSToDoServiceClient.class.getName(), methodName,
						this.name(), "Number of assignments found: "
								+ wsAssignmentList.size());
				for (WSAssignment assignment : wsAssignmentList) {
					Utils.printMsg(WSToDoServiceClient.class.getName(),
							methodName, this.name(), "Assignment ID - "
									+ assignment.getId());

					Utils.printMsg(WSToDoServiceClient.class.getName(),
							methodName, this.name(),
							"Assignment activity name - "
									+ assignment.getActivityName());
				}

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsToDoService -operationName?getAssignments -principal?username -credential?password"
						+ "\nFor example: wsToDoService -operationName?getAssignments -principal?\"itim manager\" -credential?\"secret\"";

			}
		},

		GETRFI {
			@Override
			public boolean execute(Map<String, Object> map)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String assgnID = (String) map.get(PARAM_ASSIGNMENT_ID);

				if (assgnID == null) {
					Utils.printMsg(WSToDoServiceClient.class.getName(),
							"execute", this.name(),
							"No RFI assignment ID passed to get the details.");
					String usage = this.getUsage();
					System.out.println(usage);
					return false;
				}
				WSSession session = loginIntoITIM(principal, credential);

				WSToDoService wsToDoService = getToDoService();
				// List<WSAssignment> wsAssignmentList = wsToDoService
				// .getAssignments(session);
				//
				// List<Long> activityIds = new ArrayList<Long>();
				// for (WSAssignment assignment : wsAssignmentList) {
				// activityIds.add(assignment.getId());
				// }
				//				
				// //There should be only one to-do item based on the state
				// //of ITIM and it should be a RFI activity.
				// long rfiAssignmentId = activityIds.get(0).longValue();

				// Assuming that the participant involved in the RFI activity is
				// the one
				// who's principal and credentials are supplied to login.
				long rfiAssignmentId = Long.parseLong(assgnID.trim());

				WSRFIWrapper wrapperWSRFI = wsToDoService.getRFI(session,
						rfiAssignmentId);
				Utils.printMsg(WSToDoServiceClient.class.getName(), methodName,
						this.name(), "RFI Object Profile Name: "
								+ wrapperWSRFI.getObjectProfile());

				ArrayOfTns1WSAttribute rfiAttr = wrapperWSRFI.getWsAttrValues();
				List<WSAttribute> wsRFIAttr = rfiAttr.getItem();

				for (WSAttribute attr : wsRFIAttr) {
					Utils.printMsg(WSToDoServiceClient.class.getName(),
							methodName, this.name(), "RFI Attribute Name: "
									+ attr.getName());

					if (attr.getValues() != null) {
						Utils.printMsg(WSToDoServiceClient.class.getName(),
								methodName, this.name(),
								"RFI Attribute Value: "
										+ attr.getValues().getItem().get(0));
					}
				}
				// WSForm form = wrapperWSRFI.getWsForm();
				// ArrayOfTns1WSFormTab formTabs = form.getWSFormTabs();
				// WSFormTab formTab = formTabs.getItem().get(0);
				// ArrayOfTns1WSFormElement formElements =
				// formTab.getFormElements();
				// for (WSFormElement formElement : formElements.getItem()) {
				// Utils.printMsg(WSToDoServiceClient.class.getName(),
				// methodName,
				// this.name(), "RFI Attribute Name: "
				// + formElement.getAttrName());
				//					
				// if (formElement.getAttrValues() != null) {
				// Utils.printMsg(WSToDoServiceClient.class.getName(),
				// methodName,
				// this.name(), "RFI Attribute Value: "
				// + formElement.getAttrValues().getItem().get(0));
				// }
				// }

				return true;
			}

			@Override
			String getUsage() {
				return "Usage: wsToDoService -operationName?getRFI -principal?username -credential?password -assignmentID?<RFI Assignment ID>"
						+ "\nFor example: wsToDoService -operationName?getRFI -principal?\"itim manager\" -credential?\"secret\" -assignmentID?3806650663097748910";

			}
		},

		GETENTITYDETAIL {
			public boolean execute(Map<String, Object> map) throws Exception {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String assgnID = (String) map.get(PARAM_ASSIGNMENT_ID);

				if (assgnID == null) {
					Utils
							.printMsg(WSToDoServiceClient.class.getName(),
									methodName, this.name(),
									"No assignment ID passed to get the entity details.");
					String usage = this.getUsage();
					System.out.println(usage);
					return false;
				}
				WSSession session = loginIntoITIM(principal, credential);

				WSToDoService wsToDoService = getToDoService();
				long assignmentId = Long.parseLong(assgnID.trim());
				WSEntityWrapper entityWrapper = wsToDoService.getEntityDetail(
						session, assignmentId);
				ArrayOfTns1WSAttribute wsAttr = entityWrapper
						.getWsAllAttrValues();
				List<WSAttribute> listWSAttr = wsAttr.getItem();
				for (WSAttribute attr : listWSAttr) {
					Utils.printMsg(WSToDoServiceClient.class.getName(),
							methodName, this.name(), "Attribute Name: "
									+ attr.getName());

					if (attr.getValues() != null) {
						List<String> listVal = attr.getValues().getItem();

						for (String str : listVal) {
							Utils.printMsg(WSToDoServiceClient.class.getName(),
									methodName, this.name(),
									"Attribute Value: " + str);
						}
					}

				}
				return true;
			}

			String getUsage() {
				return "Usage: wsToDoService -operationName?getEntityDetail -principal?username -credential?password -assignmentID?<Assignment ID, entity details for which is to be requested>"
						+ "\nFor example: wsToDoService -operationName?getEntityDetail -principal?\"itim manager\" -credential?\"secret\" -assignmentID?3806650663097748910";
			}
		},

		SUBMITRFI {
			public boolean execute(Map<String, Object> map)
					throws Exception {
				String methodName = "execute";
				String principal = (String) map.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) map.get(PARAM_ITIM_CREDENTIAL);
				String assgnID = (String) map.get(PARAM_ASSIGNMENT_ID);
				String justification = (String)map.get(PARAM_JUSTIFICATION);
				if (assgnID == null) {
					Utils
							.printMsg(WSToDoServiceClient.class.getName(),
									methodName, this.name(),
									"No RFI assignment ID passed to complete the activity.");
					String usage = this.getUsage();
					System.out.println(usage);
					return false;
				}
				
				WSSession session = loginIntoITIM(principal, credential);
				WSToDoService wsToDoService = getToDoService();
				
				long rfiAssignmentId = Long.parseLong(assgnID.trim());
				WSRFIWrapper wrapperWSRFI = wsToDoService.getRFI(session,
						rfiAssignmentId);
				
				ArrayOfTns1WSAttribute rfiAttr = wrapperWSRFI.getWsAttrValues();
				List<WSAttribute> wsRFIAttr = rfiAttr.getItem();
				
				//Assuming that the RFI is for an account entity
				//and that all the attributes for which the input is requested has string syntax
				//Providing a constant string value "RFIVal" for input.
				for (WSAttribute attr : wsRFIAttr) {
					String attrName = attr.getName();
					if(!attrName.equalsIgnoreCase("erservice") && !attrName.equalsIgnoreCase("target_dn") && !attrName.equalsIgnoreCase("container_dn")) {
						ArrayOfXsdString attrVal = new ArrayOfXsdString();
						attrVal.getItem().add("RFIVal");
						attr.setValues(attrVal);
					}
				}
				wsToDoService.submitRFI(session, wrapperWSRFI,justification);
				
				return true;
			}

			String getUsage() {
				return "Usage: wsToDoService -operationName?submitRFI -principal?username -credential?password -assignmentID?<RFI Assignment ID>"
				+ "\nFor example: wsToDoService -operationName?submitRFI -principal?\"itim manager\" -credential?\"secret\" -assignmentID?3806650663097748910";
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
				Utils.printMsg(WSToDoServiceClient.class.getName(), methodName,
						this.name(), "Session Id: " + session.getSessionID());
			} else {
				Utils.printMsg(WSToDoServiceClient.class.getName(), methodName,
						this.name(), "Invalid session");
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
			for (WSTODOSERVICE_OPERATIONS operation : WSTODOSERVICE_OPERATIONS
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
		WSToDoServiceClient client = new WSToDoServiceClient();
		client.run(args, true);
	}

}
