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
 * Servlet that handles "forgot password".
 * Note that in order for a user to be able to use this feature, he
 * must have previously defined his challenge-response answers.
 *********************************************************************/

package examples.expi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.ITIMFailedLoginException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ForgotPasswordManager;

public class ForgotPasswordServlet extends HttpServlet {

	private static final long serialVersionUID = -7947473850048155469L;

	public static String LOGON = "/jsp/unprotected/logon.jsp";

	public static final String FORGOT_PWD = "/jsp/unprotected/forgotpwd.jsp";

	public static final String FORGOT_PWD_INFO = "/jsp/unprotected/forgotpwdinfo.jsp";

	public static final String USERID = "userid";

	public static final String ERROR_MESSAGE = "errorMessage";

	public static final String INFO_MESSAGE = "infoMessage";

	public static final String CHALLENGES = "challenges";

	public static final String FORGOT_PASSWORD_MANAGER = "forgotPasswordManager";

	public static final String PLATFORM_CONTEXT = "platform";

	// The following get set in init()
	private static String userIDAttribute;

	private static boolean changeOnlyTIMPassword;

	private static boolean ssoEnabled;

	/**
	 * Read in the properties file.
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */

	public void init() throws ServletException {
		ExpiUtil eu = new ExpiUtil();

		// Are we to only change the TIM Account password, or change all?
		String par = eu.getProperty(ExpiUtil.CHANGEONLYITIMPASSWORD).trim();
		changeOnlyTIMPassword = par.compareToIgnoreCase("true") == 0;
		log("init(): changeOnlyTIMPassword = " + changeOnlyTIMPassword);

		// Is SSO enabled?
		LOGON = eu.getPropertySSOCheck(ExpiUtil.LOGON_PAGE);
		ssoEnabled = eu.isSSOEnabled();

		/*
		 * If SSO is enabled, use the "username" attribute. Otherwise use
		 * whatever's set in the expiUtil.
		 */
		if (ssoEnabled)
			userIDAttribute = "username";
		else
			userIDAttribute = ExpiUtil.LOGON_ID;
		log("init(): userIDAttribute = " + userIDAttribute);

		log("ForgotPasswordServlet.init() complete");

	} // init

	/**
	 * Method destroy Right now this does nothing.
	 *
	 * @see javax.servlet.Servlet#destroy()
	 */

	public void destroy() {

	} // destroy

	/**
	 * Method doGet This just calls doPost().
	 *
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log("ForgotPasswordServlet.doGet() forwarding to doPost()");
		doPost(request, response);

	} // doGet

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		log("ForgotPasswordServlet.doPost()");

		// Need a session -- at minimum to do error handling
		HttpSession session = request.getSession(true);

		// Find out how we got here and figure out what to do!
		// Note: no action specified means to get the challenges.
		String action = request.getParameter("action");
		log("ForgotPasswordServlet.doPost(): action = " + action);

		if (isNullOrEmpty(action)) {
			if (!session.isNew()) {
				log("[ForgotPasswordServlet.doPost:] invalidating previous session");
				session.invalidate();
				session = request.getSession();
			}
			doGetChallenges(request, response, session);
		} else if (action.equals("authenticate")) {
			doAuthenticate(request, response, session);
		} else {
			// Unknown action
		}

		return;

	} // doPost

	/**
	 * Method doAuthenticate. Attempt to authenticate the user via challenge
	 * response.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doAuthenticate(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		// First check responses -- this will forward back to jsp
		Map<String,String> cars = validateResponses(request, response, session);
		if ((cars == null) || cars.isEmpty())
			return;

		// Get the ForgotPasswordManager and reset the password with the
		// challenges and responses.
		ForgotPasswordManager forgotPwdMgr = (ForgotPasswordManager) session.
		    getAttribute(FORGOT_PASSWORD_MANAGER);
		if (forgotPwdMgr == null) {
			log("doAuthenticate(): No ForgotPasswordManager in the session");
			goToPage(LOGON, null, session, request, response);
			return;
		}

		try {
			String userID = (String)session.getAttribute(ExpiUtil.LOGON_ID);
			forgotPwdMgr.resetPassword(userID, cars);

			String message = "You will be notified via email when the password change "
					+ "is complete. The email message will contain the new password.";

			goToInfoPage(FORGOT_PWD_INFO, message, session, request, response);
		} catch (LoginException e) {
			String eMsg ="";
			if ( e instanceof ITIMFailedLoginException) {
				eMsg = ((ITIMFailedLoginException)e).getMessageId();
			} else {
				eMsg = e.getMessage();
			}
			if (isNullOrEmpty(eMsg)) {
				goToPage(FORGOT_PWD,
						"An unknown error occurred.",
						session,
						request,
						response);
			} else if (eMsg.equals(ITIMFailedLoginException.INVALID_ANSWERS)) {
				//code to retrieve and check the number of attempts remaining
				int remainingAttempts = -1;
				if (remainingAttempts > -1) {
					ITIMFailedLoginException failedLoginException =
						new ITIMFailedLoginException(
								ITIMFailedLoginException.INVALID_ANSWERS_ATTEMPTS,
								new Object[]{Integer.toString(remainingAttempts)});

					log("doAuthenticate(): LoginContext.login() failed with error"
							+ failedLoginException.getMessage());
					goToPage(FORGOT_PWD,
							failedLoginException.getMessage(),
							session,
							request,
							response);
				} else {
					log("doAuthenticate(): LoginContext.login() failed with error"
							+ e.getMessage());
					goToPage(
							FORGOT_PWD,
							e.getMessage(),
							session,
							request,
							response);
				}

			} else {

				log("doAuthenticate(): LoginContext.login() failed with error"
							+ e.getMessage());
				goToPage(LOGON, e.getMessage(),
					session,
					request,
					response);
			}
		} catch (ApplicationException e) {
			log("doAuthenticate(): ForgotPasswordManager.resetPassword() failed with error" +
				e.getMessage());
			goToPage(LOGON, e.getMessage(), session, request, response);
		}

		return;
	}

	/**
	 * Method validateResponses. Check to make sure that there was an answer for
	 * each response. Setup the forward if there were any problems.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @return Map
	 * @throws ServletException
	 * @throws IOException
	 */
	private Map<String, String> validateResponses(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		Map<String, String> cars = (Map<String, String>) session.getAttribute(CHALLENGES);
		Iterator it = cars.keySet().iterator();

		while (it.hasNext()) {
			String challenge = (String) it.next();
			log("e(): challenge: " + challenge);
			String responseToChallenge = (String) request
					.getParameter(challenge);
			log("validateResponses(): response: " + responseToChallenge);
			if (isNullOrEmpty(responseToChallenge)) {
				goToPage(
						FORGOT_PWD,
						"One or more responses was empty.  You must provide a response for each challenge.",
						session, request, response);
				return null;
			} else {
				cars.put(challenge, responseToChallenge);
			}
		}

		return cars;
	}

	/**
	 * Method doGetChallenges. Get the challenges for the userid that was
	 * specified in the request, set them in the session, and forward to
	 * forgotpwd.jsp.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @throws ServletException
	 * @throws IOException
	 */

	private void doGetChallenges(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		log("doGetChallenges() begin");

		// Get the userID from the request; if there wasn't one
		// this will handle setting up the forward back to the logon page.
		String userID = getUserID(request, response, session);
		if (userID == null)
			return;

		Collection challenges = null;
		Map<String,String> challengesMap = new HashMap<String,String>();

		try {
			ExpiUtil eu = new ExpiUtil();
			PlatformContext platform = eu.createPlatformContext();
			ForgotPasswordManager forgotPwdMgr = new ForgotPasswordManager(platform);
			challenges = forgotPwdMgr.getSecretQuestion(userID);
			for (Object challenge : challenges) {
				String challengeStr = (String)challenge;
			    challengesMap.put(challengeStr.trim(), "");
			}

			session.setAttribute(CHALLENGES, challengesMap);
			session.setAttribute(FORGOT_PASSWORD_MANAGER, forgotPwdMgr);
			session.setAttribute(PLATFORM_CONTEXT, platform);
			session.setAttribute(ExpiUtil.LOGON_ID, userID);

			/*
			 * See if there are challenges.
			 *
			 * NOTE: If challenges come back empty but there are no errors, we
			 * can't tell if it's because C/R is disabled or if the user just
			 * doesn't have any challenges defined. The API that we could use to
			 * find out requires a Subject.
			 */
			if ((challenges == null) || challenges.isEmpty()) {
				goToPage(
						LOGON,
						"Either you do not have any challenges configured, or challenge-resonse is disabled.",
						session, request, response);
			} else {
				goToPage(FORGOT_PWD, null, session, request, response);
			}
		} catch (ApplicationException e) {
			log("getChallenges(): Caught ApplicationException: "
					+ e.getMessage());
			goToPage(LOGON, e.getMessage(), session, request, response);
		} catch (RemoteException e) {
			log("getChallenges(): Caught RemoteException: " + e.getMessage());
			goToPage(LOGON, e.getMessage(), session, request, response);
		} catch (FailedLoginException e) {
			log("getChallenges(): Caught FailedLoginException: " + e.getMessage());
			goToPage(LOGON, e.getMessage(), session, request, response);
		}

		log("doGetChallenges() complete");

	} // getChallenges

	/**
	 * Method getUserID. Get the userID from the request based on the setting of
	 * userIDAttribute. If there isn't one, setup the forward back to the logon
	 * page.
	 *
	 * Note: the logon page checks to make sure that the userID has a value. But
	 * the user might have tried to get here directly.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @return String
	 * @throws ServletException
	 * @throws IOException
	 */

	private String getUserID(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		String userID = (String) request.getParameter(userIDAttribute);
		if(userID == null || userID.trim().length() == 0) {
			goToPage(
					LOGON,
					"Enter your User ID and then click on the \"Forgot Your Password\" link",
					session, request, response);
		userID=null;
		}

		log("ForgotPasswordServlet.getUserID(): userID = " + userID);

		return userID;

	} // getUserID

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

		if (ssoEnabled) {
			request.setAttribute("ERROR", infoMessage);
		}

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

} // ForgotPasswordServlet
