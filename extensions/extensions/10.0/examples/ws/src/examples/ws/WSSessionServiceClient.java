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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.ibm.itim.ws.model.WSChallengeResponseInfo;
import com.ibm.itim.ws.model.WSLocale;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.model.WSVersionInfo;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSInvalidSessionException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSUnsupportedVersionException;

/**
 * @author administrator
 * 
 */
public class WSSessionServiceClient extends GenericWSClient {

	/**
	 * @param args
	 */

	private enum WSSESSIONSERVICE_OPERATIONS {
		GETITIMVERSION {
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				
				WSSessionService proxy = getSessionService();
				float itimVersion = proxy.getItimVersion();
				
				Utils.printMsg(this.getClass().getName(), "getItimVersion",
						this.name(), "ITIM Version is : " + itimVersion);
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?getItimVersion";

			}
		},
		GETITIMVERSIONINFO {
			public boolean execute(Map<String, Object> mpParams)
					throws WSUnsupportedVersionException {
				
				WSSessionService proxy = getSessionService();
				WSVersionInfo itimVersion = proxy.getItimVersionInfo();
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
				return "usage: wsSessionService -operationName?getItimVersionInfo";

			}
		},
		GETITIMFIXPACKLEVEL {
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				
				WSSessionService proxy = getSessionService();
				int itimFixPack = proxy.getItimFixpackLevel();
				Utils.printMsg(this.getClass().getName(),
						"getItimFixPackLevel", this.name(), "Build date ==> "
								+ "FixPack Level ==> " + itimFixPack);
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?getItimFixPackLevel";

			}
		},
		GETWEBSERVICESBUILDNUMBER {
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				
				WSSessionService proxy = getSessionService();
				int buildNumber = proxy.getWebServicesBuildNumber();
				Utils.printMsg(this.getClass().getName(),
						"getWebServicesBuildNumber", this.name(),
						"WebServices build number ==> " + buildNumber);
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?getWebServicesBuildNumber";

			}
		},

		GETWEBSERVICESTARGETTYPE {
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				
				WSSessionService proxy = getSessionService();
				String webServicesTargetType = proxy.getWebServicesTargetType();
				Utils.printMsg(this.getClass().getName(),
						"getWebServicesTargetType", this.name(),
						"WebServices Target type ==> " + webServicesTargetType);
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?getWebServicesTargetType";

			}
		},

		GETWEBSERVICESVERSION {
			public boolean execute(Map<String, Object> mpParams)
					throws WSApplicationException {
				
				WSSessionService proxy = getSessionService();
				float webServicesVersion = proxy.getWebServicesVersion();
				Utils.printMsg(this.getClass().getName(),
						"getWebServicesVersion", this.name(),
						"WebServices Version ==> " + webServicesVersion);
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?getWebServicesVersion";

			}
		},

		LOGIN {
			public boolean execute(Map<String, Object> mpParams)
					throws WSInvalidLoginException, WSLoginServiceException {
				String principal = (String) mpParams.get(PRINCIPAL);
				String credential = (String) mpParams.get(CREDENTIAL);
				WSSessionService session = getSessionService();
				session.login(principal, credential);
				Utils.printMsg(this.getClass().getName(), "login", this.name(),
						" principal ==> " + principal + "\n"
								+ " credential ==> " + credential + "\n"								
								+ " session ==> " + session + "\n");
				
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?login -principal?<username> -credential?<password> \n for example: wsSessionService -operationName?getPrincipalPerson -principal?\"itim manager\" -credential?secret";
			}
		},

		ISPASSWORDEDITINGALLOWED {
			public boolean execute(Map<String, Object> mpParams)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException, WSInvalidSessionException {
				String principal = (String) mpParams.get(PRINCIPAL);
				String credential = (String) mpParams.get(CREDENTIAL);
				WSSessionService proxy = getSessionService();
				WSSession session = proxy.login(principal, credential);
				boolean isPasswordEditingAllowed = getSessionService().isPasswordEditingAllowed(session);
				Utils.printMsg(this.getClass().getName(),
						"isPasswordEditingAllowed", this.name(),
						" principal ==> " + principal + "\n"
								+ " credential ==> " + credential + "\n"								
								+ " session ==> " + session + "\n"
								+ "isPasswordEditingAllowed ==>"
								+ isPasswordEditingAllowed

				);
				
				return true;
			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?isPasswordEditingAllowed -principal?<username> -credential?<password> \n for example: wsSessionService -operationName?isPasswordEditingAllowed -principal?\"itim manager\" -credential?secret";
			}

		},
		
		GETCHALLENGEQUESTIONS{
			public boolean execute(Map<String, Object> mpParams)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException, WSInvalidSessionException {
				String principal = (String) mpParams.get(PRINCIPAL);
				String credential = (String) mpParams.get(CREDENTIAL);
				String loc = (String) mpParams.get(LOCALE);
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
				
				List<String> challengeQuestions = getSessionService().getChallengeQuestions(principal, wsLocale);
				Utils.printMsg(this.getClass().getName(),
						"getChallengeQuestions", this.name(), " principal ==> "
								+ principal + "\n" + " credential ==> "
								+ credential + "\n" + " sessionService ==> ");
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
				return "usage: wsSessionService -operationName?getChallengeQuestions -principal?<username> -credential?<password> -locale?<locale string>"
						+ "\n for example: wsSessionService -operationName?getChallengeQuestions -principal?\"itim manager\" -credential?secret -locale?en";
			}
			
		},
		
		LOSTPASSWORDLOGINDIRECTENTRY{
			public boolean execute(Map<String, Object> mpParams) throws WSInvalidLoginException, WSLoginServiceException, WSApplicationException, WSInvalidSessionException  
			{

				String principal = (String)mpParams.get(PRINCIPAL);
				//new password
				String newCredential = (String)mpParams.get(NEW_CREDENTIAL);
				String loc = (String)mpParams.get(LOCALE);
				List<String> answers =Utils.commaSpliter( mpParams.get(ANSWERS));
				Locale locale=new Locale(loc);
				WSLocale wsLocale= new WSLocale();
				wsLocale.setLanguage(locale.getLanguage());
				wsLocale.setCountry(locale.getCountry());
				wsLocale.setVariant(locale.getVariant());
				WSSessionService sessionPort = getSessionService();
				Utils.printMsg(this.getClass().getName(), "lostPasswordLoginDirectEntry", this.name()," principal ==> " + principal +"\n"+
						" new credential ==> " + newCredential + "\n" +
						" sessionService ==> \n"
				);
				
				List<WSChallengeResponseInfo> criList = new ArrayList<WSChallengeResponseInfo>(); // List to hold each challenge and response info.
			    List<String> challenges = sessionPort.getChallengeQuestions(principal,wsLocale);
			    if(challenges.size()!=answers.size()){
			    	Utils.printMsg(this.getClass().getName(), "lostPasswordLoginDirectEntry", this.name()," prameters dont match ");
						return false;
			    }
			    for (int i = 0; i < challenges.size(); i++) {
			     WSChallengeResponseInfo cri = new WSChallengeResponseInfo();
			     cri.setQuestion(challenges.get(i));
			     cri.setAnswer(answers.get(i));
			     criList.add(cri);
			     }
			   
			    WSSession session = sessionPort.lostPasswordLoginDirectEntry(principal, criList,newCredential,wsLocale);
			    Utils.printMsg(this.getClass().getName(), "lostPasswordLoginDirectEntry", this.name()," session ==> "+ session);
				return true;
		    				
			}
			public String getUsage(String errMsg){
				return "usage: wsSessionService -operationName?lostPasswordLoginDirectEntry -principal?<username> -locale?<language> -newCredential?<newPassword> -answers?<answer1>,<answer2>" +
						"\n for example: wsSessionService -operationName?lostPasswordLoginDirectEntry -principal?\"itim manager\" -principal?\"itim manager\" -newCredential?\"secret123\" -locale?en - -answers?\"Blue\",\"High School\"";
			}
					
					
		},
		
		LOSTPASSWORDLOGINRESETPASSWORD {

			public boolean execute(Map<String, Object> mpParams)
					throws WSInvalidLoginException, WSLoginServiceException,
					WSApplicationException, WSInvalidSessionException {

				String principal = (String) mpParams.get(PRINCIPAL);
				String credential = (String) mpParams.get(CREDENTIAL);
				List<String> answers = Utils.commaSpliter( mpParams.get(ANSWERS));
				String loc = (String) mpParams.get(LOCALE);
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
				
				Utils.printMsg(this.getClass().getName(),
						"lostPasswordLoginResetPassword", this.name(),
						" principal ==> " + principal + "\n"
								+ " credential ==> " + credential + "\n");

				WSSessionService sessionPort = getSessionService();

				List<WSChallengeResponseInfo> criList = new ArrayList<WSChallengeResponseInfo>();
				List<String> challenges = sessionPort.getChallengeQuestions(
						principal, wsLocale);
				if (challenges.size() != answers.size()) {
					Utils.printMsg(this.getClass().getName(),
							"lostPasswordLoginResetPassword", this.name(),
							"parameters dont match ");
					return false;
				}
				for (int i = 0; i < challenges.size(); i++) {
					WSChallengeResponseInfo cri = new WSChallengeResponseInfo();
					cri.setQuestion(challenges.get(i));
					cri.setAnswer(answers.get(i));
					criList.add(cri);
				}
				String requestId = sessionPort.lostPasswordLoginResetPassword(
						principal, criList, wsLocale);
				Utils.printMsg(this.getClass().getName(),
						"lostPasswordLoginResetPassword", this.name(),
						" reset password request id  ==> " + requestId);
				return true;

			}

			public String getUsage(String errMsg) {
				return "usage: wsSessionService -operationName?lostPasswordLoginResetPassword -principal?<username> -credential?<password> -locale?<locale string> -answers?<answer1> -answers?<answer2> ..\n for example: wsSessionService -operationName?lostPasswordLoginResetPassword -principal?\"itim manager\" -credential?secret -locale?en -answers?myname";
			}
					
					
			
		};
		
		private static final String ANSWERS = "answers";
		private static final String LOCALE = "locale";
		private static final String CREDENTIAL = "credential";
		private static final String NEW_CREDENTIAL = "newCredential";
		private static final String PRINCIPAL = "principal";

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

		static WSSESSIONSERVICE_OPERATIONS getSessionServiceOperation(
				String param) {
			return WSSESSIONSERVICE_OPERATIONS.valueOf(param);
		}
		
		
	}

	public static void main(String[] args) {
		WSSessionServiceClient wsSessionClient = new WSSessionServiceClient();
		wsSessionClient.run(args, true);

	}

	public boolean executeOperation(Map<String, Object> mpParams) {
		String operationName = (String) mpParams.get(OPERATION_NAME);
		boolean retCode = false;
		if (operationName != null && operationName.length() > 0) {
			operationName = operationName.toUpperCase();
			for (WSSESSIONSERVICE_OPERATIONS operation : WSSESSIONSERVICE_OPERATIONS
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
