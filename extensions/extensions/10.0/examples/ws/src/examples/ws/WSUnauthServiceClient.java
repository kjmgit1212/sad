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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSChallengeResponseInfo;
import com.ibm.itim.ws.model.WSLocale;
import com.ibm.itim.ws.model.WSPasswordRulesInfo;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSVersionInfo;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSUnauthService;
import com.ibm.itim.ws.services.WSUnsupportedVersionException;

/**
 * @author administrator
 * 
 */
public class WSUnauthServiceClient extends GenericWSClient {

	/**
	 * @param args
	 */

	private enum WSUNAUTHSERVICE_OPERATIONS {
		GETITIMVERSIONINFO {
			public boolean execute(Map<String, Object> mpParams)
					throws WSUnsupportedVersionException {
				
				WSUnauthService wsUnauthService = getUnauthService();
				WSVersionInfo itimVersion = wsUnauthService.getItimVersionInfo();
				Utils.printMsg(this.getClass().getName(), "getItimVersionInfo",
						this.name(), "Build date ==> "
								+ itimVersion.getBuildDate() + "\n"
								+ "Build Nuumber ==>"
								+ itimVersion.getBuildNumber() + "\n"
								+ "Build Time ==> "
								+ itimVersion.getBuildTime() + "\n"
								+ "FixPack Level ==> "
								+ itimVersion.getFixPackLevel() + "\n"
								+ "Version ==> " + itimVersion.getVersion());
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsUnauthService -operationName?getItimVersionInfo";

			}
		},
		
		GETCHALLENGEQUESTIONS{
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				String principal = (String) mpParams.get(PRINCIPAL);
				String loc = (String) mpParams.get(LOCALE);
				if (loc == null){
					Utils.printMsg(WSUnauthServiceClient.class.getName(), "execute", "GETCHALLENGEQUESTIONS", "Lcoale parameter is not specified");
					return false;
				}
				StringTokenizer st = new StringTokenizer(loc, "_");
				String language = st.nextToken();
				String country = "";
				String variant = "";
				if (st.hasMoreTokens())
					country = st.nextToken();
				if (st.hasMoreTokens())
					variant = st.nextToken();
				Locale locale = new Locale(language, country, variant);
				WSLocale wsLocale = new WSLocale();
				wsLocale.setLanguage(locale.getLanguage());
				wsLocale.setCountry(locale.getCountry());
				wsLocale.setVariant(locale.getVariant());

				WSUnauthService wsUnauthService = getUnauthService();
				List<String> challengeQuestions = wsUnauthService.
										getChallengeQuestions(principal, wsLocale);
				Utils.printMsg(this.getClass().getName(),
						"getChallengeQuestions", this.name(), " principal ==> "
								+ principal + "\n" + " UnauthService ==> " 
								+ wsUnauthService + "\n");
				StringBuffer quesBuff = new StringBuffer();
				for (String question : challengeQuestions) {
					quesBuff.append(question);
					quesBuff.append("\n");
				}
				Utils.printMsg(this.getClass().getName(),
						"getChallengeQuestions", this.name(),
						"The challenge questions are ==> \n"
								+ quesBuff.toString());
				return true;
			}
			public String getUsage(String errMsg) {
				return "usage: wsUnauthService -operationName?getChallengeQuestions -principal?<username> -locale?<locale string>"
						+ "\n for example: wsUnauthService  -operationName?getChallengeQuestions " +
						  "-principal?\"itim manager\" -locale?en";
			}
			
		},
		
		LOSTPASSWORDLOGINRESETPASSWORD {

			public boolean execute(Map<String, Object> mpParams)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException {

				String principal = (String) mpParams.get(PRINCIPAL);
				String answers = (String) mpParams.get(ANSWERS);
				if (answers == null){
					Utils.printMsg(WSUnauthServiceClient.class.getName(), "execute", "LOSTPASSWORDLOGINRESETPASSWORD", "answers parameter is not specified");
					return false;
				}
				StringTokenizer stAnswers = new StringTokenizer(answers, ",");
				List<String> answersList = new ArrayList<String>();
				String answer = null;
				while (stAnswers.hasMoreTokens()){
					answer = stAnswers.nextToken();
					answersList.add(answer);
				}
				String loc = (String) mpParams.get(LOCALE);
				if (loc == null){
					Utils.printMsg(WSUnauthServiceClient.class.getName(), "execute", "LOSTPASSWORDLOGINRESETPASSWORD", "Lcoale parameter is not specified");
					return false;
				}
				StringTokenizer st = new StringTokenizer(loc, "_");
				String language = st.nextToken();
				String country = "";
				String variant = "";
				if (st.hasMoreTokens())
					country = st.nextToken();
				if (st.hasMoreTokens())
					variant = st.nextToken();
				Locale locale = new Locale(language, country, variant);
				WSLocale wsLocale = new WSLocale();
				wsLocale.setLanguage(locale.getLanguage());
				wsLocale.setCountry(locale.getCountry());
				wsLocale.setVariant(locale.getVariant());
				
				WSUnauthService wsUnauthService = getUnauthService();
				Utils.printMsg(this.getClass().getName(),
						"lostPasswordLoginResetPassword", this.name(),
						" principal ==> " + principal + "\n"
								+ " UnauthService ==> " + wsUnauthService);

				List<WSChallengeResponseInfo> criList = new ArrayList<WSChallengeResponseInfo>();
				List<String> challenges = wsUnauthService.
									getChallengeQuestions(principal, wsLocale);
				if (challenges.size() != answersList.size()) {
					Utils.printMsg(this.getClass().getName(),
							"lostPasswordLoginResetPassword", this.name(),
							"parameters dont match ");
					return false;
				}
				for (int i = 0; i < challenges.size(); i++) {
					WSChallengeResponseInfo cri = new WSChallengeResponseInfo();
					cri.setQuestion(challenges.get(i));
					cri.setAnswer(answersList.get(i));
					criList.add(cri);
				}
				String requestId = wsUnauthService.lostPasswordLoginResetPassword(
						principal, criList, wsLocale);
				Utils.printMsg(this.getClass().getName(),
						"lostPasswordLoginResetPassword", this.name(),
						" reset password request id  ==> " + requestId);
				return true;

			}

			public String getUsage(String errMsg) {
				return "usage: wsUnauthService -operationName?lostPasswordLoginResetPassword -principal?<username> -locale?<locale string> -answers?<answer1,answer2,...>\n" +
						" for example: wsUnauthService -operationName?lostPasswordLoginResetPassword " +
						"-principal?\"itim manager\" -locale?en -answers?myname,Mycolor";
			}
					
		},
		
		SELFREGISTER{

			@Override
			boolean execute(Map<String, Object> mpParams) throws WSApplicationException {
				
				mpParams.remove(OPERATION_NAME);
				
				String tenantId = (String)mpParams.get(PARAM_TENANT_ID);
				mpParams.remove(PARAM_TENANT_ID);
				
				WSPerson wsPerson = createWSPersonFromAttributes(mpParams);
				WSUnauthService wsUnauthService = getUnauthService();
				// tenantId is an optional parameter if not specified then the value specified 
				// in the enRole.properties for property "enrole.defaulttenant.id" is used.
				wsUnauthService.selfRegister(wsPerson, tenantId);
				Utils.printMsg(WSUnauthServiceClient.class.getName(), "execute", this.name(), "Self Registration of person completed successfully.");
				return true;
				
			}

			@Override
			String getUsage(String errMessage) {
				
				return "usage: wsUnauthService -operationName?selfRegister [-tenantId?<tenant id>]  -cn?<person name> -sn?<Last Name>  -l?<location name> [additional person attributes]"+
				"\n tenantId is an optional parameter if not specified then the value specified in the enRole.properties for property \"enrole.defaulttenant.id\" is used." +
				"\n for example: wsUnauthService -operationName?selfRegister -tenantId?org " +
				"-cn?\"Judith Hill\" -sn?Hill -l?\"IBM Location\" -uid?\"Judith Hill\" -givenname?Judith " ;
				
			}
			
		},
		
		GETSELFPASSWORDCHANGERULES {

			@Override
			boolean execute(Map<String, Object> map) throws WSApplicationException {
				String methodName = "execute";

				String accountDN = (String) map.get(PARAM_ACCOUNT_ID);
				if (accountDN == null){
					Utils.printMsg(WSUnauthServiceClient.class.getName(), "execute", "GETSELFPASSWORDCHANGERULES", "accountID parameter is not specified");
					return false;
				}
				Utils.printMsg(WSUnauthServiceClient.class.getName(),
						methodName, this.name(), "Account DN: "
								+ accountDN);

				WSUnauthService wsUnauthService = getUnauthService();
				WSPasswordRulesInfo wsRuleInfo = wsUnauthService
										.getSelfPasswordChangeRules(accountDN);

				Utils.printMsg(WSUnauthServiceClient.class.getName(),
						methodName, this.name(), "Password Change Rules Info: "
								+ wsRuleInfo.toString());
				return true;
			}

			@Override
			String getUsage(String errMessage) {
				return "Usage: wsUnauthService -operationName?getSelfPasswordChangeRules -accountID?<account ID>"
						+ "\nFor example: wsUnauthService -operationName?getSelfPasswordChangeRules " +
						"-accountID?jhill";

			}
		},
		
		;
		
		private static final String ANSWERS = "answers";
		private static final String LOCALE = "locale";
		private static final String PRINCIPAL = "principal";
		private static final String PARAM_TENANT_ID = "tenantId";
		private static final String PARAM_ACCOUNT_ID = "accountID";

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

		/**
		 * The method will be executed in case there is an exception occurred
		 * while executing the method. Since the usage of each of the methods
		 * will be different we to make sure that its taken care of.
		 * 
		 * @param errMessage
		 * @return
		 */

		static WSUNAUTHSERVICE_OPERATIONS getUnauthServiceOperation(
				String param) {
			return WSUNAUTHSERVICE_OPERATIONS.valueOf(param);
		}
		
		WSPerson createWSPersonFromAttributes(Map<String, Object> mpParams) {
			WSPerson wsPerson = new WSPerson();
			wsPerson.setProfileName(ObjectProfileCategory.PERSON);
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
					Utils.printMsg(WSUnauthServiceClient.class.getName(), "createWSPersonFromAttributes", "CREATEPERSON", "The parameter value datatype is not supported.");
					//System.out.println(" The parameter value datatype is not supported.");
				}
				wsAttr.setValues(arrStringValues);
				lstWSAttributes.add(wsAttr);
			}
			ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
			attrs.getItem().addAll(lstWSAttributes);
			wsPerson.setAttributes(attrs);

			return wsPerson;
		}
		
	}

	public static void main(String[] args) {
		WSUnauthServiceClient wsUnauthServiceClient = new WSUnauthServiceClient();
		wsUnauthServiceClient.run(args, true);

	}

	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSUNAUTHSERVICE_OPERATIONS operation : WSUNAUTHSERVICE_OPERATIONS
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
