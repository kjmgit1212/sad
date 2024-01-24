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

/*********************************************************************
 * FILE: %Z%%M%  %I%  %W% %G% %U%
 *
 * Description:
 *
 * This servlet handles changing a user's password.
 * It uses the changeOnlyTIMPassword property to determine whether
 * to change only the TIM Account password or to change all.
 *********************************************************************/

package examples.expi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.ITIMFailedLoginException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.InvalidPasswordException;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.identity.SelfPasswordManager;
import com.ibm.itim.apps.identity.SelfRequest;
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.apps.provisioning.PasswordManager;
import com.ibm.itim.apps.provisioning.PasswordRuleException;
import com.ibm.passwordrules.standard.PasswordRulesInfo;

public class ChangePasswordServlet extends HttpServlet {

	private static final long serialVersionUID = 5127739637646467706L;

	private static final String INFO_MESSAGE = "infoMessage";

	private static final String PWD_RULES = "passwordRules";

	private String tenantID = "";

	ExpiUtil eu = null;

	private boolean changeOnlyTIMPassword; // gets set in init()

	public static String PLATFORM_CONTEXT;

	public static String USERID;

	public static String ERROR_MESSAGE;

	public static String SUBJECT;

	public static String LOGON;

	public static String MAIN;

	public static String CHANGE_PWD;

	public static String CHANGE_PWD_INFO;

	public static String PWD_RULES_INFO;

	public static String SELF_CHANGE_PWD;

	public static String SELF_CHANGE_PWD_INFO;

	/**
	 * Method init Read in the properties file.
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {

		log("ChangePasswordServlet:init(): start");

		eu = new ExpiUtil();
		tenantID = eu.getTenantID();

		// Change only TIM password?
		changeOnlyTIMPassword = Boolean.parseBoolean(eu
				.getProperty(ExpiUtil.CHANGEONLYITIMPASSWORD));
		log("init(): Only change TIM password: " + this.changeOnlyTIMPassword);

		/**
		 * Defines
		 */
		ERROR_MESSAGE = ExpiUtil.ERR_LABEL;
		PLATFORM_CONTEXT = ExpiUtil.PLATFORM_CONTEXT;
		USERID = ExpiUtil.USER_ID;
		SUBJECT = ExpiUtil.SUBJECT;

		/**
		 * Pages to go to!
		 */

		// Logon page
		LOGON = eu.getPropertySSOCheck(ExpiUtil.LOGON_PAGE);
		log("init(): logon page: " + LOGON);
		if (isNullOrEmpty(LOGON))
			throw new ServletException(
					"Could not load logon page location from properties file");

		// Main page
		MAIN = eu.getProperty(ExpiUtil.HOME_PAGE);
		log("init(): Home page: " + MAIN);
		if (isNullOrEmpty(MAIN))
			throw new ServletException(
					"Could not load home page location from properties file");

		// Change password page
		CHANGE_PWD = eu.getProperty(ExpiUtil.CHANGEPWD_PAGE);
		log("init(): Change password page: " + CHANGE_PWD);
		if (isNullOrEmpty(CHANGE_PWD))
			throw new ServletException(
					"Could not load change password page location from properties file");

		// Change password info page
		CHANGE_PWD_INFO = eu.getProperty(ExpiUtil.CHANGEPWDINFO_PAGE);
		log("init():  Change password info page: " + CHANGE_PWD_INFO);
		if (isNullOrEmpty(CHANGE_PWD_INFO))
			throw new ServletException(
					"Could not load change password info page location from properties file");

		// Password rules info page
		PWD_RULES_INFO = eu.getProperty(ExpiUtil.PWDRULESINFO_PAGE);
		log("init(): Change password page: " + PWD_RULES_INFO);
		if (isNullOrEmpty(PWD_RULES_INFO))
			throw new ServletException(
					"Could not load password rules info page location from properties file");

		// Self-change password page
		SELF_CHANGE_PWD = eu.getProperty(ExpiUtil.SELF_CHANGEPWD_PAGE);
		log("init(): Self change password page: " + SELF_CHANGE_PWD);
		if (isNullOrEmpty(SELF_CHANGE_PWD))
			throw new ServletException(
					"Could not self change password page location from properties file");

		// Self-change password page info
		SELF_CHANGE_PWD_INFO = eu
				.getProperty(ExpiUtil.SELF_CHANGEPWD_INFO_PAGE);
		log("init(): Self change password page: " + SELF_CHANGE_PWD_INFO);
		if (isNullOrEmpty(SELF_CHANGE_PWD_INFO))
			throw new ServletException(
					"Could not self change password info page location from properties file");

		log("init(): complete");
	} // init

	/**
	 * destroy()
	 * 
	 * Right now this does nothing.
	 */
	public void destroy() {

	} // destroy

	/***************************************************************************
	 * doGet() This just calls doPost().
	 **************************************************************************/
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);

	} // doGet

	/**
	 * Method doPost Executes the change password function.
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// Used for various constants
		ExpiUtil eu = new ExpiUtil();

		// Get session data
		HttpSession session = (HttpSession) request.getSession(true);
		if (session.isNew()) {
			log("doPost(): No previous session; forwarding to logon page");
			goToPage(LOGON, "Your session has expired.  Please login again.",
					session, request, response);
			return;
		}

		PlatformContext platform = (PlatformContext) session
				.getAttribute(PLATFORM_CONTEXT);
		if (platform == null) {
			try {
				platform = eu.createPlatformContext();
			} catch (Exception e) {
				log("doPost(): Caught Exception trying to create a PlatformContext: "
						+ e.getMessage());
			}
		}

		// Find out what action we're supposed to take
		String action = request.getParameter("action");
		log("doPost(): action = " + action);

		// Get form data
		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmNewPassword = request.getParameter("confirmNewPassword");

		// No action specified -> use PasswordManager
		if (isNullOrEmpty(action)) {
			Subject subject = (Subject) session.getAttribute(SUBJECT);
			if (!validateSessionData(session, request, response, subject,
					platform))
				return;

			if (!validateFormData(session, request, response, currentPassword,
					newPassword, confirmNewPassword, CHANGE_PWD))
				return;

			changePassword(session, request, response, platform, subject,
					currentPassword, newPassword);

			// Action specified is useSelfPasswordManager
		} else if (action.equals("useSelfPasswordManager")) {

			String logonID = request.getParameter("logonID");
			if (isNullOrEmpty(logonID)) {
				log("doPost(): logonID is null");
				goToPage(SELF_CHANGE_PWD, "User ID was not specified.",
						session, request, response);
				return;
			}
			session.setAttribute(ExpiUtil.LOGON_ID, logonID);

			if (!validateFormData(session, request, response, currentPassword,
					newPassword, confirmNewPassword, SELF_CHANGE_PWD))
				return;

			noLoginChangePassword(session, request, response, platform,
					logonID, currentPassword, newPassword);

			if(platform != null) {
				platform.close();
				platform = null;
			}
			// Unknown action specified -> error!
		} else {
			log("doPost():  Unknown action specified");
		}

		return;

	} // doPost

	/**
	 * Method noLoginChangePassword. Changes the TIM Account password without a
	 * Subject (therefore, no login). * Note that TIM will athenticate the user
	 * before making the chaneg by comparing the current password with the one
	 * that's stored in LDAP.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param platform
	 * @param currentPassword
	 * @param newPassword
	 * @return none
	 * @throws ServletException
	 * @throws IOException
	 */

	private void noLoginChangePassword(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			PlatformContext platform, String logonID, String currentPassword,
			String newPassword) throws ServletException, IOException {

		SelfPasswordManager spm = new SelfPasswordManager(platform);
		if (spm == null) {
			log("noLoginChangePassword():  Could not get a SelfPasswordManager");
			return;
		}

		try {
			SelfRequest sreq = spm.changeExpiredPassword(logonID,
					currentPassword, newPassword);
			goToInfoPage(
					SELF_CHANGE_PWD_INFO,
					"Request ID is: "
							+ sreq.getID()
							+ ".  <br>You will be notified via email when the change is complete.",
					session, request, response);
		} catch (RemoteException e) {
			log("noLoginChangePassword(): Caught RemoteException:"
					+ e.getMessage());
			// Dunno what went wrong, use a generic message
			goToPage(SELF_CHANGE_PWD,
					"Error contacting the server.  Please try again.", session,
					request, response);
			return;
		} catch (FailedLoginException e) {
			handleFailedLoginException(e, session, request, response,
					currentPassword, logonID, platform, SELF_CHANGE_PWD);
			return;
		} catch (InvalidPasswordException e) {
			String errorMessage = e.getMessage();
			if (isNullOrEmpty(errorMessage))
				errorMessage = "One or more password rules have been violated.";
			log("noLoginChangePassword(): Caught InvalidPasswordException:"
					+ errorMessage);
			goToPage(SELF_CHANGE_PWD, errorMessage, session, request, response);
		} catch (ApplicationException e) {
			log("noLoginChangePassword(): Caught ApplicationException:"
					+ e.getMessage());
		}

	} // noLoginChangePassword

	/**
	 * Method getAccountsToChange. Consults the changeOnlyTIMPassword property
	 * and retrieves the appropriate accounts.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param pm
	 * @param pmo
	 * @return Collection
	 * @throws ServletException
	 * @throws IOException
	 */

	private Collection getAccountsToChange(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			PasswordManager pm, PersonMO pmo) throws ServletException,
			IOException {

		Collection<AccountMO> accounts = null;

		try {

			accounts = pm.getPasswordAccounts(pmo);
			if (accounts == null) {
				log("getAccountsToChange(): Accounts is null");
				goToPage(
						LOGON,
						"Unable to retrieve your accounts, including the account you are currently logged in with.  Please login again.",
						session, request, response);
				return null;
			}

			if (changeOnlyTIMPassword == true) {
				accounts = getTIMAccount(session, request, response, accounts);
			}

			// Setup the forward if needed
			if (accounts == null) {
				log("getAccountsToChange(): Accounts is null");
				goToPage(
						LOGON,
						"Your password cannot be changed because your accounts cannot be retrieved, including the account you are currently logged in with. Please login again.",
						session, request, response);
			}

		} catch (ApplicationException e) {
			/*
			 * Thrown if unable to obtain the accounts. This may possibly be
			 * caused by the subject being removed by another client previous to
			 * this call. Note that since the user is logged in, they should
			 * have at least one account!
			 */
			log("getAccountsToChange(): Caught ApplicationException: "
					+ e.getMessage());
			goToPage(
					LOGON,
					"Your password cannot be changed because your accounts cannot be retrieved, including the account you are currently logged in with.  Please login again.",
					session, request, response);

		} catch (RemoteException e) {
			log("getAccountsToChange(): Caught RemoteException: "
					+ e.getMessage());

			// Thrown if unable to communicate with platform.
			goToPage(
					CHANGE_PWD,
					"Unable to retrieve your accounts.  The server may be busy or there may be a more serious problem.  Please try again.  If the problem persists, contact your system administrator.",
					session, request, response);
		}

		return accounts;

	} // getAccountsToChange

	/**
	 * Method goToPage. Set the message in the request and forward it on to the
	 * specified page.
	 * 
	 * @param page
	 * @param errorMessage
	 * @param session
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void goToPage(String page, String errorMessage,
			HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		session.setAttribute(ERROR_MESSAGE, errorMessage);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/" + page);

		dispatcher.forward(request, response);

	} // goToPage

	/**
	 * Method isNullOrEmpty. Returns true if the given object is null or equals
	 * "".
	 * 
	 * @param o
	 * @return boolean
	 */
	private boolean isNullOrEmpty(Object o) {

		if ((o == null) || (o.equals("")))
			return true;
		else
			return false;

	} // isNullOrEmpty

	/**
	 * Method validateSessionData. Validates the session data. If anything is
	 * amiss, forward back to the logon page.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param subject
	 * @param platform
	 * @return boolean
	 * @throws ServletException
	 * @throws IOException
	 */
	private boolean validateSessionData(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Subject subject, PlatformContext platform) throws ServletException,
			IOException {

		boolean isValid = true;

		if ((subject == null) || (platform == null)) {
			goToPage(LOGON, "Your session is invalid, please login again.",
					session, request, response);
			isValid = false;
		}

		return isValid;

	} // validateSessionData

	/**
	 * Method validateFormData. Validates the form data. If anything is amiss,
	 * set the error message and forward back to the change password jsp.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param currentPassword
	 * @param newPassword
	 * @param confirmNewPassword
	 * @return boolean
	 * @throws ServletException
	 * @throws IOException
	 */
	private boolean validateFormData(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			String currentPassword, String newPassword,
			String confirmNewPassword, String page) throws ServletException,
			IOException {

		// Make sure the user filled in all the values
		if (isNullOrEmpty(currentPassword) || isNullOrEmpty(newPassword)
				|| isNullOrEmpty(confirmNewPassword)) {
			goToPage(page,
					"You must fill in all of the fields before clicking OK.",
					session, request, response);
			return false;
		}

		// Make sure new password and confirmed password are the same
		if (!newPassword.equals(confirmNewPassword)) {
			goToPage(page,
					"New password does not match confirmed new password.",
					session, request, response);
			return false;
		}

		return true;

	} // validateFormData

	/**
	 * Method changePassword. Try to change the password. If it fails for any
	 * reason, forward back to the jsp so that the jsp can display any errors.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param pm
	 * @param platform
	 * @param subject
	 * @param password
	 * @throws ServletException
	 * @throws IOException
	 */
	private void changePassword(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			PlatformContext platform, Subject subject, String currentPassword,
			String newPassword) throws ServletException, IOException {

		SelfPasswordManager spm = new SelfPasswordManager(platform);
		String userID = "";
		if (spm == null) {
			log("changePassword():  Could not get a SelfPasswordManager");
			return;
		}

		try {
			userID = (String) session.getAttribute(ExpiUtil.LOGON_ID);
			SelfRequest sreq = spm.changeExpiredPassword(userID,
					currentPassword, newPassword);

			goToInfoPage(
					CHANGE_PWD_INFO,
					"Request ID is: "
							+ sreq.getID()
							+ ".  <br>You will be notified via email when the change is complete.",
					session, request, response);
		} catch (FailedLoginException e) {
			handleFailedLoginException(e, session, request, response,
					currentPassword, userID, platform, CHANGE_PWD);
			return;

		} catch (PasswordRuleException e) {

			/*
			 * Thrown if the rules defined in the password policies for each of
			 * the services hosting the accounts could not be merged. * They are
			 * mutually exclusive.
			 */
			log("changePassword(): Caught PasswordRuleException: "
					+ e.getMessage());

		} catch (InvalidPasswordException e) {

			/*
			 * Password violates one or more rules. Call checkPasswordRules to
			 * get the info to display to the user.
			 */
			String errorMessage = e.getMessage();
			if (isNullOrEmpty(errorMessage))
				errorMessage = "One or more password rules have been violated.";
			log("changePassword(): Caught InvalidPasswordException:"
					+ errorMessage);
			displayPasswordRules(session, request, response, platform, subject,
					errorMessage);

		} catch (RemoteException e) {

			// Unable to communicate with TIM.
			log("changePassword(): Caught RemoteException: " + e.getMessage());
			goToPage(CHANGE_PWD,
					"Error contacting the server.  Please try again.", session,
					request, response);

		} catch (ApplicationException e) {

			/*
			 * May possibly be caused by an account being removed by another
			 * client previous to this call.
			 */
			log("changePassword(): Caught ApplicationException: "
					+ e.getMessage());
			goToPage(CHANGE_PWD,
					"Error contacting the server.  Please try again.", session,
					request, response);
		}

		return;

	} // changePassword

	/**
	 * Method displayPasswordRules. If the password doesn't meet the rules for
	 * the given accounts send the user back to the jsp so that they can try
	 * another one. Display the rules in the error so they know what makes a
	 * good one.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param exceptionMessage
	 * @throws ServletException
	 * @throws IOException
	 */
	private void displayPasswordRules(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			PlatformContext platform, Subject subject, String exceptionMessage)
			throws ServletException, IOException {

		// First off, need to get a PasswordManager
		PasswordManager pm = new PasswordManager(platform, subject);
		if (pm == null) {
			log("changePassword(): Could not get a PasswordManager");
			goToPage(
					CHANGE_PWD,
					"Unable to get a connection to the server. Please try again later.",
					session, request, response);
			return;
		}

		PasswordRulesInfo pri = null;

		try {

			/*
			 * Get the accounts we're trying to change so that we can get the
			 * rules for them.
			 * 
			 * Note: the user should own at least one account, that is, the one
			 * that they are logged in as! If there was a problem retrieving the
			 * accounts, getAccountsToChange sets up the forward.
			 */

			ExpiUtil expiu = new ExpiUtil();
			PersonMO pmo = expiu.lookupPerson(platform, subject);
			if (pmo == null) {
				log("changePassword(): PersonMO is null");
				return;
			}

			Collection accounts = null;
			accounts = getAccountsToChange(session, request, response, pm, pmo);
			if (accounts == null)
				return;

			pri = pm.getRules(accounts);
			log("checkPasswordRules(): Here are the rules: "
					+ (pm.getRules(accounts)).toString());
		} catch (PasswordRuleException e) {
			log("checkPasswordRules(): Caught PasswordRuleException: "
					+ e.getMessage());
		} catch (ApplicationException e) {
			log("checkPasswordRules(): Caught ApplicationException: "
					+ e.getMessage());
		} catch (RemoteException e) {
			log("checkPasswordRules(): Caught RemoteException: "
					+ e.getMessage());
		}

		/**
		 * Build up a "human readable" Map of all the password rules for the jsp
		 * to display.
		 */

		Map<String, Object> rules = new HashMap<String, Object>();

		// Boolean rules
		addBooleanRule(rules, "Dictionary words allowed", pri
				.getAllowInDictionary());
		addBooleanRule(rules, "User ID allowed", pri.getAllowUserID());
		addBooleanRule(rules, "Words present in User ID allowed", pri
				.getAllowUserIDCaseInsensitive());
		addBooleanRule(rules, "User Name allowed", pri.getAllowUserName());
		addBooleanRule(rules, "Words present in User Name allowed", pri
				.getAllowUserNameCaseInsensitive());

		// String rules
		addStringRule(rules, "Invalid characters", pri.getInvalidChars());
		addStringRule(rules, "Required characters", pri.getRequiredChars());
		addStringRule(rules, "Must start with these characters", pri
				.getStartsWithChars());
		addStringRule(rules, "Must only consist of these characters", pri
				.getRestrictedToChars());

		// Numerical rules
		addNumericRule(rules, "Maximum length", pri.getMaxLength());
		addNumericRule(rules, "Maximum number of sequential characters", pri
				.getMaxSequentialCharacters());
		addNumericRule(rules, "Minimum number of alphabetic characters", pri
				.getMinAlphabeticCharacters());
		addNumericRule(rules, "Minimum number of digits", pri
				.getMinDigitCharacters());
		addNumericRule(rules, "Minimum number of distinct characters", pri
				.getMinDistinctCharacters());
		addNumericRule(rules, "Miniumum length", pri.getMinLength());
		addNumericRule(rules,
				"Number of password changes before a password can be re-used",
				pri.getRepeatedHistoryLength());
		addNumericRule(
				rules,
				"Number of password changes before a \"reversed\" password can be re-used",
				pri.getReversedHistoryLength());

		/**
		 * These rules are currently not being used by TIM as there is no way to
		 * enforce them.
		 */
		// addNumericRule(rules, "getMaxValidityPeriod",
		// pri.getMaxValidityPeriod());
		// addNumericRule(rules, "getMinValidityPeriod",
		// pri.getMinValidityPeriod());
		/**
		 * Custom rules. Can't add meaning to these; just stick 'em in!
		 */

		Map<String, Object> customRules = pri.getCustomRules();
		if ((customRules != null) && !customRules.isEmpty())
			rules.putAll(customRules);

		session.setAttribute(ERROR_MESSAGE, exceptionMessage);
		session.setAttribute(PWD_RULES, rules);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/" + PWD_RULES_INFO);

		dispatcher.forward(request, response);

		return;

	} // displayPasswordRules

	/**
	 * Method addStringRule. If the value is not null or empty then the rule
	 * applies, so add it to the map.
	 * 
	 * @param rules
	 * @param label
	 * @param value
	 */
	private void addStringRule(Map<String, Object> rules, String label,
			String value) {

		if (!isNullOrEmpty(value))
			rules.put(label, value);

	} // addStringRule

	/**
	 * Method addBooleanRule. Add a boolean value to the Map in a "human
	 * readable" fashion. NOTE: The TIM rules are set up such that if a rule is
	 * set to "true", then that means that something is allowed. So * there is
	 * no need to show it. For example, there's no need to display: "User Name
	 * allowed = Yes".
	 * 
	 * @param rules
	 * @param label
	 * @param value
	 */
	private void addBooleanRule(Map<String, Object> rules, String label,
			boolean value) {

		if (value == false)
			rules.put(label, "No");

	} // addBooleanRule

	/**
	 * Method addNumericRule.
	 * 
	 * @param rules
	 * @param label
	 * @param value
	 */
	private void addNumericRule(Map<String, Object> rules, String label,
			long value) {

		// -1 means that the rule does not apply
		if (value != -1)
			rules.put(label, "" + value); // Wants an Object

	} // addNumericRule

	/**
	 * Method getTIMAccount. Returns a Collection that contains the TIM Account
	 * of the user who is trying to reset his password. Note that the user could
	 * have more than one TIM Account, that's why we have to do the check.
	 * 
	 * @param session
	 * @param request
	 * @param response
	 * @param accounts
	 * @return Collection
	 * @throws ServletException
	 * @throws IOException
	 */
	private Collection<AccountMO> getTIMAccount(HttpSession session,
			HttpServletRequest request, HttpServletResponse response,
			Collection accounts) throws ServletException, IOException {

		String userID = (String) session.getAttribute(ExpiUtil.LOGON_ID);
		log("getTIMAccount(): user ID = " + userID);
		if (userID == null) {
			log("getTIMAccount(): could not get userID from session");
			return null;
		}

		Collection<AccountMO> onlyTimAccount = new Vector<AccountMO>();
		Iterator it = accounts.iterator();

		try {
			while (it.hasNext()) {
				AccountMO amo = (AccountMO) it.next();

				/*
				 * Note: SERVICE_ENROLE_PROFILE_NAME = "ITIMService" This is the
				 * service name of the account. If you were searching for an
				 * Solaris account, you would put that service name here
				 * instead.
				 */
				log("getTIMAccount(): Profile name "
						+ amo.getService().getData().getServiceProfileName());
				if (amo.getService().getData().getServiceProfileName().equals(
						ExpiUtil.SERVICE_ENROLE_PROFILE_NAME)) {
					log("getTIMAccount(): AccountMO user ID: "
							+ amo.getData().getUserId());
					String aUserID = amo.getData().getUserId();
					if (aUserID.equals(userID)) {
						onlyTimAccount.add(amo);
						return onlyTimAccount;
					}
				}
			} // while
		} catch (Exception e) {
			accounts = null;
		}

		if (onlyTimAccount == null) {
			log("getTIMAccount(): Could not find TIM Account for user.");
		}

		return onlyTimAccount;

	} // getTIMAccount

	/**
	 * Method goToInfoPage. Set the message in the request and forward it on to
	 * the specified page.
	 * 
	 * @param page
	 * @param infoMessage
	 * @param session
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void goToInfoPage(String page, String infoMessage,
			HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		session.setAttribute(INFO_MESSAGE, infoMessage);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/" + page);

		dispatcher.forward(request, response);

	} // goToInfoPage

	private void handleFailedLoginException(FailedLoginException e,
			HttpSession session, HttpServletRequest request,
			HttpServletResponse response, String currentPassword,
			String logonID, PlatformContext platform, String goToPageName)
			throws ServletException, IOException {
		String eMsg = "";
		String errorMessage = "";
		if (e instanceof ITIMFailedLoginException) {
			eMsg = ((ITIMFailedLoginException) e).getMessageId();
		} else {
			eMsg = e.getMessage();
		}
		if (eMsg == null || eMsg.equals("")) {
			errorMessage = "An unknown error occurred";
		}
		if (errorMessage.equals("")) {
			errorMessage = e.getMessage();
		}
		log("handleFailedLoginException(): Caught FailedLoginException: "
				+ errorMessage);
		goToPage(goToPageName, errorMessage, session, request, response);
		return;
	}

} // ChangePasswordServlet
