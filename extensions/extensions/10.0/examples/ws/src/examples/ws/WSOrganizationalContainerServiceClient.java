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
/**
 * 
 */
package examples.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfTns1WSOrganizationalContainer;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSSessionService;



/**
 * @author administrator
 *
 */
public class WSOrganizationalContainerServiceClient extends GenericWSClient {
	
	
	private static final String PARAM_PARENT_ORG_CONTAINER = "parentOrgContainer";
	
	private static final String ORG_CONTAINER_PROFILE_NAME = "OrganizationalUnit";
	
	private static final String PARAM_ORG_CONTAINER_ATTR_NAME = "attrName";
	
	private static final String PARAM_ORG_CONTAINER_ATTR_VALUE = "attrValue";
	
	private static final String PARAM_ORG_CONTAINER = "orgContainer";
	
	@Resource
	private WebServiceContext wsCtxt = null;
	
	enum WSORGANIZATIONALCONTAINER_OPERATIONS {
		GETORGANIZATIONSUBTREE{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				// WSSession session,
				// WSOrganizationalContainer parent)
				boolean executedSuccessfully = false;
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams
						.get(PARAM_ITIM_CREDENTIAL);
				String parentOrg = (String) mpParams
						.get(PARAM_PARENT_ORG_CONTAINER);
				// We need to get the WSOrganizationalContainer object
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				WSOrganizationalContainer parentWSOrgContainer = this
						.getParentWSOrgContainer(wsSession, parentOrg,
								wsOrgContainerService);

				WSOrganizationalContainer wsOrgContainerSubTree = wsOrgContainerService
						.getOrganizationSubTree(wsSession, parentWSOrgContainer);
				if (wsOrgContainerSubTree != null) {
					executedSuccessfully = true;
					printWSOrgContainerDetailswithChildren(wsOrgContainerSubTree);
				}

				return executedSuccessfully;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsOrgContainerService -operationName?getOrganizationSubTree -principal?<username> -credential?<password> -parentOrgContainer?<parentContainerName>"+
				"\n for example: wsOrgContainerService -operationName?getOrganizationSubTree -principal?\"itim manager\" -credential?secret -parentOrgContainer?ACME ";
			}
			
		},

		
		SEARCHCONTAINERBYATTRIBUTE{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				boolean executedSuccessfully = false;
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String parentOrg = (String)mpParams.get(PARAM_PARENT_ORG_CONTAINER);
				String attrName = (String)mpParams.get(PARAM_ORG_CONTAINER_ATTR_NAME);
				String attrValue = (String)mpParams.get(PARAM_ORG_CONTAINER_ATTR_VALUE);
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				
				WSOrganizationalContainer parentWSOrgContainer = this.getParentWSOrgContainer(wsSession, parentOrg, wsOrgContainerService);
				List<WSOrganizationalContainer> lstWSORganizationalContainers = wsOrgContainerService.searchContainerByAttribute(wsSession, parentWSOrgContainer, attrName, attrValue);
				if(lstWSORganizationalContainers != null && lstWSORganizationalContainers.size() > 0){
					executedSuccessfully = true;
					//Print Details of the Containers which are returned
					for(WSOrganizationalContainer wsOrgContainer : lstWSORganizationalContainers){
						printWSOrgContainerDetails(wsOrgContainer);
					}
				}else{
					System.out.println(" There are no organizational containers matching the attribute name " + attrName + " and attribute value " + attrValue);
				}
				
				return executedSuccessfully;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsOrgContainerService -operationName?searchContainerByAttribute -principal?<username> -credential?<password> -parentOrgContainer?<parentContainerName>"+
				"-attrName?<name of the attribute to be used for searching the organization> -attrValue?<Value of the attribute which is to be used for searching the organization>" +
				"\n for example: wsOrgContainerService -operationName?searchContainerByAttribute -principal?\"itim manager\" -credential?secret -parentOrgContainer?ACME -attrName?ou -attrValue?\"Test Org Unit\"";
			}
			
		},
		
		
		CREATECONTAINER{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				boolean executedSuccessfully = false;
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				String parentOrg = (String)mpParams.get(PARAM_PARENT_ORG_CONTAINER);
				
				//Removing the attributes so that we have only the organizational container attributes
				//in the mpParams map.
				mpParams.remove(OPERATION_NAME);
				mpParams.remove(PARAM_ITIM_PRINCIPAL);
				mpParams.remove(PARAM_ITIM_CREDENTIAL);
				mpParams.remove(PARAM_PARENT_ORG_CONTAINER);
				
				WSSession wsSession = this.loginIntoITIM(principal, credential);
				WSOrganizationalContainer newWSContainer = createWSOrganizationalContainerFromAttributes(mpParams);
				newWSContainer.setProfileName(ORG_CONTAINER_PROFILE_NAME);
				WSOrganizationalContainerService service = getOrganizationalContainerService();
				List<WSOrganizationalContainer> lstOrgContainers = service.searchContainerByName(wsSession, null, ORG_CONTAINER_PROFILE_NAME, parentOrg);
				WSOrganizationalContainer parent = null;
				if(lstOrgContainers != null && lstOrgContainers.size() > 0){
					parent = lstOrgContainers.get(0);
				}else{
					System.out.println(" Not able to locate the parent container with name " + parentOrg);
				}
				
				WSOrganizationalContainer wsOrgContainer = service.createContainer(wsSession, parent, newWSContainer);
				
				if(wsOrgContainer != null){
					executedSuccessfully = true;
					printWSOrgContainerDetails(wsOrgContainer);
				}
				return executedSuccessfully;
			}

			@Override
			String getUsage(String errMessage) {
				String usage = "usage: ";
				// TODO Auto-generated method stub
				return "usage: wsOrgContainerService -operationName?createContainer -principal?<username> -credential?<password> -parentOrgContainer?<parentContainerName> -attrName1?attrValue1 -attrName2?attrValue2 ...." +
						"\n for example: wsOrgContainerService -operationName?createContainer -principal?\"itim manager\" -credential?secret -parentOrgContainer?IBM -ou?\"Org Unit\" -description?\"Description for Org Unit\"";
			}
			
		}
		,
		
		REMOVECONTAINER{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				
				WSOrganizationalContainer wsContainer = null;
				String containerName = (String) mpParams
						.get(PARAM_ORG_CONTAINER);
				if (containerName != null) {
					List<WSOrganizationalContainer> lstWSOrgContainers = wsOrgContainerService
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

				} else {
					System.out.println("No Filter parameter passed for the container name.");
					return false;
				}
				
				String containerDN = wsContainer.getItimDN();
				wsOrgContainerService.removeContainer(wsSession, containerDN);
				System.out.println(" The request has been submitted successfully.");
				
				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsOrgContainerService -operationName?removeContainer -principal?<username> -credential?<password> -orgContainer?<Organization Unit Name>"+
				"\n for example: wsOrgContainerService -operationName?removeContainer -principal?\"itim manager\" -credential?secret -orgContainer?IBM_Test_Unit";
			}
			
		},
		
		LOOKUPCONTAINER{

			@Override
			boolean execute(Map<String, Object> mpParams) throws Exception {
				boolean executedSuccessfully = false;
				String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL);
				String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL);
				
				
				WSSession wsSession = loginIntoITIM(principal, credential);
				WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
				
				WSOrganizationalContainer wsContainer = null;
				String containerName = (String) mpParams.get(PARAM_ORG_CONTAINER);
				if (containerName != null) {
					List<WSOrganizationalContainer> lstWSOrgContainers = wsOrgContainerService
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
				} else {
					System.out.println("No Filter parameter passed for the container name.");
					return false;
				}
				
				String containerDN = wsContainer.getItimDN();
				WSOrganizationalContainer wsOrgContainer = wsOrgContainerService.lookupContainer(wsSession, containerDN);
				System.out.println(" The request has been submitted successfully.");
				if(wsOrgContainer != null){
					executedSuccessfully = true;
					printWSOrgContainerDetails(wsOrgContainer);
				}
				
				return executedSuccessfully;
			}

			@Override
			String getUsage(String errMessage) {
				return "usage: wsOrgContainerService -operationName?lookupContainer -principal?<username> -credential?<password> -orgContainer?<Organization Unit Name>"+
				"\n for example: wsOrgContainerService -operationName?lookupContainer -principal?\"itim manager\" -credential?secret -orgContainer?IBM_Test_Unit";
			}
			
		};
		
		

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

		static WSORGANIZATIONALCONTAINER_OPERATIONS getOrganizationalContainerServiceOperation(String param) {
			return WSORGANIZATIONALCONTAINER_OPERATIONS.valueOf(param);
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
		
		WSOrganizationalContainer getParentWSOrgContainer(WSSession wsSession, String parentOrg, WSOrganizationalContainerService wsOrgContainerService) throws Exception{
			List<WSOrganizationalContainer> lstWSOrganizationContainers = wsOrgContainerService.searchContainerByName(wsSession, null, ORG_CONTAINER_PROFILE_NAME, parentOrg);
			WSOrganizationalContainer parentWSOrgContainer = null;
			if(lstWSOrganizationContainers != null && lstWSOrganizationContainers.size() > 0){
				//We have found some organizational containers
				//Get the first container and proceed
				parentWSOrgContainer = lstWSOrganizationContainers.get(0);
			}else{
				System.out.println(" The parent organization unit passed in not valid. Taking null for the parent attribute.");
			}
			
			return parentWSOrgContainer;
		}
		
		void printWSOrgContainerDetails(WSOrganizationalContainer wsOrgContainer){
			String name = wsOrgContainer.getName();
			String DN = wsOrgContainer.getItimDN();
			String supervisorDN = wsOrgContainer.getSupervisorDN();
			
			System.out.println(" The Organization Sub Tree Details are : " );
			System.out.println(" Name : " + name);
			System.out.println(" Distinguished Name : " + DN);
			System.out.println(" Supervisor Distinguished Name : " + supervisorDN);
		}
		
		void printWSOrgContainerDetailswithChildren(WSOrganizationalContainer wsOrgContainer){
			if(wsOrgContainer==null) return ; 
			printWSOrgContainerDetails(wsOrgContainer);
			ArrayOfTns1WSOrganizationalContainer arrayOfChildren = wsOrgContainer.getChildren();
			if(arrayOfChildren!=null){
				List<WSOrganizationalContainer> childrens = arrayOfChildren.getItem();
				for(WSOrganizationalContainer child:childrens){
					System.out.println(" Child container of  : " + wsOrgContainer.getName());
					printWSOrgContainerDetailswithChildren(child);
				}
			}
			
		}
		
		WSOrganizationalContainer createWSOrganizationalContainerFromAttributes(Map<String, Object> mpParams) {
			WSOrganizationalContainer wsOrgContainer = new WSOrganizationalContainer();
			
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
			wsOrgContainer.setAttributes(attrs);

			return wsOrgContainer;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WSOrganizationalContainerServiceClient wsPSClient = new WSOrganizationalContainerServiceClient();
		wsPSClient.run(args, true);

	}

	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		System.out.println(" operationName " + operationName);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
				operationName = operationName.toUpperCase();
				for(WSORGANIZATIONALCONTAINER_OPERATIONS operation : WSORGANIZATIONALCONTAINER_OPERATIONS.values()){
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
