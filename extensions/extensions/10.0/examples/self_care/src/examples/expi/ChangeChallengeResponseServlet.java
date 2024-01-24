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
 * Servlet that handles logic for changing a user's challenge/response
 * answers.  Note that a limitation of the sample application is that
 * the challenge definition mode must be set to ADMIN-DEFINED,
 * and that a TIM administrator must have set up the challenges.
 * This servlet can be extended to handle the other modes.
 *********************************************************************/

package examples.expi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ChallengeResponseConfiguration;
import com.ibm.itim.apps.identity.ChallengeResponseManager;
import com.ibm.itim.apps.identity.ChallengesAndResponses;

public class ChangeChallengeResponseServlet extends HttpServlet {

	// Defines

	private static final long serialVersionUID = -3149326538669886990L;

	// a la the TIM GUI!
	private static final String NOT_CHANGED = "********";

	private static final String INFO_MESSAGE = "infoMessage";

	private static final String CR_MANAGER = "ChallengeResponseManager";

	// Map of challenges and responses that were displayed to the user
	private static final String DISPLAYED_CARS = "challengesAndResponses";

	// Current ChallengesAndResponses as set in TIM
	private static final String CURRENT_CARS = "timChallengesAndResponses";

	// These get loaded at init
	private static String ERROR_MESSAGE;

	private static String PLATFORM_CONTEXT;

	private static String SUBJECT;

	private static String MAIN;

	private static String LOGON;

	private static String CR_ANSWERS;

	private static String CR_ANSWERS_INFO;

	/**
	 * Method init Load up defines!
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */

	public void init() throws ServletException {

		ExpiUtil eu = new ExpiUtil();

		/**
		 * Defines
		 */
		ERROR_MESSAGE = ExpiUtil.ERR_LABEL;
		PLATFORM_CONTEXT = ExpiUtil.PLATFORM_CONTEXT;
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

		// Challenge response answer page
		CR_ANSWERS = eu.getProperty(ExpiUtil.CRANSWERPAGE_PAGE);
		log("init(): Challenge response answer page: " + CR_ANSWERS);
		if (isNullOrEmpty(CR_ANSWERS))
			throw new ServletException(
					"Could not load challenge response answer page location from properties file");

		// Challenge response answer info page
		CR_ANSWERS_INFO = eu.getProperty(ExpiUtil.CRANSWERPAGEINFO_PAGE);
		log("init(): Home page: " + CR_ANSWERS_INFO);
		if (isNullOrEmpty(CR_ANSWERS_INFO))
			throw new ServletException(
					"Could not load challenge response answer info location from properties file");

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

		doPost(request, response);

	} // doGet

	/**
	 * Method doPost
	 *
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		log("doPost() start");

		// Get the session
		HttpSession session = request.getSession(true);
		if (session.isNew()) {
			log("doPost(): No previous session; forwarding to logon page");
			goToPage(
					LOGON,
					"Your session has expired or is invalid.  Please sign on again.",
					session, request, response);
			return;
		}

		// Find out what action to take.
		// Note: no action specified means to get the challenges.
		String action = request.getParameter("action");
		log("ChangeChallengeResponseServlet.doPost(): action = " + action);

		if (isNullOrEmpty(action)) {
			getChallengesAndResponses(request, response, session);
		} else if (action.equals("setAnswers")) {
			setAnswers(request, response, session);
		} else {
			// Unknown action
		}

		log("doPost() end");
		return;

	} // doPost

	/**
	 * Method setAnswers. Update the user's answers with the ones they
	 * submitted.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @throws ServletException
	 * @throws IOException
	 */
	private void setAnswers(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		log("setAnswers() begin");

		// Get answers from request
		Map submittedCars = validateSubmittedAnswers(request, response, session);
		if (submittedCars == null) {
			log("setAnswers(): Submitted answers were invalid.");
			return;
		}

		// Update the answers!
		ChallengesAndResponses carsToSet = (ChallengesAndResponses) session
				.getAttribute(CURRENT_CARS);
		ChallengeResponseManager crm = (ChallengeResponseManager) session
				.getAttribute(CR_MANAGER);

		try {

			// Remove any old challenges
			removeOldChallenges(crm, submittedCars, carsToSet);

			// Update new or modified challenges
			updateNewOrModifiedChallenges(submittedCars, carsToSet);

			crm.setChallengesAndResponses(carsToSet);

			/**
			 * Reset updateCRAnswers to false so that the warning msg will not
			 * be displayed on main.jsp.
			 */
			session.setAttribute("updateCRAnswers", new Boolean(false));

			goToInfoPage(CR_ANSWERS_INFO,
					"Your challenge/response answers have been updated.",
					session, request, response);
		} catch (RemoteException e) {
			log("setAnswers(): Caught RemoteException: " + e.getMessage());
			goToPage(CR_ANSWERS_INFO,
					"An error was received while contacting the server.  The error message was:  "
							+ e.getMessage(), session, request, response);

		} catch (ApplicationException e) {
			log("setAnswers(): Caught ApplicationException: " + e.getMessage());
			goToPage(
					CR_ANSWERS_INFO,
					"An error was received while updating the challenge/response answers.  The error message was:  "
							+ e.getMessage(), session, request, response);
		}

		log("setAnswers() end");
		return;
	}

	/**
	 * Method validateSubmittedAnswers. Validate the answers that were submitted
	 * in the request to make sure: - Each answer has a value - The answer and
	 * confirmed answer are the same
	 *
	 * Returns a Map with the validated challenges/answers.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @return Map<String, String>
	 * @throws ServletException
	 * @throws IOException
	 */
	private Map<String, String> validateSubmittedAnswers(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) throws ServletException, IOException {

		boolean error = false;
		Map<String, String> submittedCars = new HashMap<String, String>();
		Map displayedCars = (Map) session.getAttribute(DISPLAYED_CARS);

		Iterator it = displayedCars.keySet().iterator();
		while (it.hasNext()) {
			String challenge = (String) it.next();
			log("validateSubmittedAnswers(): validating challenge: "
					+ challenge);

			String answer = (String) request.getParameter(challenge + "Answer");
			// log("\tanswer = <" + answer + ">");

			String confirmedAnswer = (String) request.getParameter(challenge
					+ "ConfirmAnswer");
			// log("\tconfirmedAnswer = <" + answer + ">");

			boolean isEmptyResponse = isNullOrEmpty(answer) && isNullOrEmpty(confirmedAnswer);
			if (!isEmptyResponse){
				if(answer.equals(confirmedAnswer)) {
					submittedCars.put(challenge, answer);
				} else {
					log("\tInvalid answer");
					error = true;
					submittedCars.put(challenge, "");
				}
			}
		}

		// Did we hit any errors?
		if (error) {
			goToPage(
					CR_ANSWERS,
					"One or more of the answers did not match the confirmed answer, or the answer was not provided.  You must provide an answer and confirmation of the answer for each challenge.",
					session, request, response);
			return null;
		} else {
			session.setAttribute(DISPLAYED_CARS, submittedCars);
		}

		return submittedCars;
	}

	/**
	 * Method getChallengesAndResponses. Get the challenges for the userid that
	 * was specified in the request, set them in the session, and forward to
	 * forgotpwd.jsp.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getChallengesAndResponses(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		log("getChallengesAndResponses() begin");

		// Get session data
		Subject subject = (Subject) session.getAttribute(SUBJECT);
		PlatformContext platform = (PlatformContext) session
				.getAttribute(PLATFORM_CONTEXT);

		if (!validateSessionData(session, request, response, subject, platform)) {
			return;
		}

		Map carsToDisplay = null;
		try {
			ChallengeResponseManager crm = new ChallengeResponseManager(
					platform, subject);
			session.setAttribute(CR_MANAGER, crm);

			ChallengesAndResponses cars = crm.getChallengesAndResponses();
			session.setAttribute(CURRENT_CARS, cars);

			// getChallengesAndResponsesToDisplay() sets up any forwards
			carsToDisplay = getChallengesAndResponsesToDisplay(request,
					response, session, crm, cars);
			if ((carsToDisplay != null) && (!carsToDisplay.isEmpty())) {
				session.setAttribute(DISPLAYED_CARS, carsToDisplay);
				goToPage(CR_ANSWERS, null, session, request, response);
			}

		} catch (ApplicationException e) {

			log("getChallenges(): Caught ApplicationException: "
					+ e.getMessage());
			goToPage(MAIN, e.getMessage(), session, request, response);

		} catch (RemoteException e) {

			log("getChallenges(): Caught RemoteException: " + e.getMessage());
			goToPage(MAIN, e.getMessage(), session, request, response);
		}

		log("getChallengesAndResponses() complete");
		return;

	} // getChallenges

	/**
	 * Method getChallengesAndResponsesToDisplay. Given the
	 * ChallengesAndResponses for the subject, build up a list of challenges and
	 * responses to display.
	 *
	 * Note: If the challenge was already there it must have a response. So mark
	 * it as NOT_CHANGED so that after the submit we'll be able to tell if it
	 * has changed.
	 *
	 * @param request
	 * @param response
	 * @param session
	 * @param crm
	 * @param cars
	 * @return Map
	 * @throws ServletException
	 * @throws IOException
	 */
	private Map getChallengesAndResponsesToDisplay(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			ChallengeResponseManager crm, ChallengesAndResponses cars)
			throws ServletException, IOException {

		ChallengeResponseConfiguration crc = null;
		Locale locale = null;

		try {
			ExpiUtil expiUtilObj = ExpiUtil.getInstance();
			String language = expiUtilObj.getProperty(ExpiUtil.LANGUAGE);
			String country = expiUtilObj.getProperty(ExpiUtil.COUNTRY);
			if (language != null && country != null) {
				locale = new Locale(language, country);
			} else if (language != null) {
				locale = new Locale(language);
			}
			crc = crm.getChallengeResonseConfiguration(locale);

			/*
			 * Do not proceed further if the 'challenge response' feature is
			 * disabled in enrole application
			 */
			if (!crc.isChallengeResponseEnabled()) {
				log("getChallengesAndResponsesToDisplay(): Challenge Response is not enabled");
				goToPage(
						CR_ANSWERS_INFO,
						"You cannot use the \"Change Challenge/Response Answers\" feature because the challenge response feature is disabled in enrole application",
						session, request, response);
				return null;
			}

			/*
			 * One of the limitations of this sample is that it doesn't have
			 * support for users adding their own challenges; we must be in
			 * admin-defined mode.
			 */
			if (crc.areChallengesUserDefined()) {
				log("getChallengesAndResponsesToDisplay(): Not in admin defined mode");
				goToPage(
						CR_ANSWERS_INFO,
						"You cannot use the \"Change Challenge/Response Answers\" feature because the challenge definition mode is \"USER-DEFINED\".  The sample code only supports the \"ADMIN-DEFINED\" challenge definition mode.",
						session, request, response);
				return null;
			}

			Collection<String> adminChallenges = crc.getAdminDefinedChallenges();
			if (locale != null) {
				// To handle the case of 'default locale'
				Collection<String> adminChallengesDefLocale = crm
						.getChallengeResonseConfiguration(null)
						.getAdminDefinedChallenges();
				if (adminChallenges != null) {
					if (adminChallengesDefLocale != null) {
						adminChallenges.addAll(adminChallengesDefLocale);
					}
				} else {
					adminChallenges = adminChallengesDefLocale;
				}
			}

			if ((adminChallenges == null) || (adminChallenges.isEmpty())) {
				log("getChallengesAndResponsesToDisplay(): No admin challenges defined");
				goToPage(
						CR_ANSWERS_INFO,
						"You cannot use the \"Change Challenge/Response Answers\" feature at this time. The sample code only supports configuring challenge/response answers when the challenges are defined by an Administrator, however, there are no Administrator defined challenges at this time.",
						session, request, response);
				return null;
			}

			/*
			 * If this is the first time the user is configuring his responses,
			 * then just return the admin-defined challenges.
			 */
			Collection userChallenges = cars.getChallenges();
			if ((userChallenges == null) || (userChallenges.isEmpty())) {
				log("getChallengesAndResponsesToDisplay(): user doesn't have challenges defined");
				return collectionToMap(adminChallenges);
			}

			/*
			 * The user has some challenges defined; go through and mark these
			 * as not changed in the list to display.
			 *
			 * Note: if there were any old challenges that are no longer in use,
			 * setChallenges() will take care of wiping them out. Don't do it
			 * here because the user hasn't committed the change yet.
			 */
			Map<String, String> carsToDisplay = new HashMap<String, String>();
			Iterator it = adminChallenges.iterator();
			while (it.hasNext()) {
				String challenge = (String) it.next();
				if (userChallenges.contains(challenge))
					carsToDisplay.put(challenge, NOT_CHANGED);
				else
					carsToDisplay.put(challenge, "");
			}

			return carsToDisplay;

		} catch (RemoteException e) {
			log("getChallengesAndResponsesToDisplay(): Caught RemoteException: "
					+ e.getMessage());
			return null;
		} catch (ApplicationException e) {
			log("getChallengesAndResponsesToDisplay(): Caught ApplicationException: "
					+ e.getMessage());
			return null;
		} catch (Exception e) {
			log("getChallengesAndResponsesToDisplay(): Caught Exception: "
					+ e.getMessage());
			return null;
		}
	} // getChallengesAndResponsesToDisplay

	/**
	 * Method collectionToMap. Turn a Collection into a Map
	 *
	 * @param c
	 * @return Map
	 */
	private Map collectionToMap(Collection<String> c) {

		if (c == null) {
			return new HashMap<String, String>();
		}

		Map<String, String> map = new HashMap<String, String>(c.size());

		for (String str : c) {
			map.put(str, "");
		}

		return map;

	} // collectionToMap

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

		RequestDispatcher dispatcher = getServletContext()
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

		RequestDispatcher dispatcher = getServletContext()
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
		return o == null || o.equals("");
	} // isNullOrEmpty

	/**
	 * Method validateSessionData.
	 *
	 * Validates the session data. If anything is amiss, forward back to the
	 * logon page.
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

		log("ChangeChallengeResponseServlet.validateSessionData()");

		boolean isValid = true;

		if ((subject == null) || (platform == null)) {
			goToPage(LOGON, "Your session is invalid, please login again.",
					session, request, response);
			isValid = false;
		}

		return isValid;

	} // validateSessionData

	/**
	 * Method removeOldChallenges.
	 *
	 * Removes old challenges. This can happen if an administrator removes a
	 * challenge.
	 *
	 * @param ChallengeResponseManager
	 *            crm - ChallengeResponseManager for the user
	 * @param Map
	 *            submittedCars - challenges/responses that the user submitted
	 * @param ChallengesAndResponses
	 *            carsToSet - building this up to use later
	 * @throws RemoteException
	 * @throws ApplicationException
	 */

	private void removeOldChallenges(ChallengeResponseManager crm,
			Map submittedCars, ChallengesAndResponses carsToSet)
			throws RemoteException, ApplicationException {

		ChallengesAndResponses origCars = crm.getChallengesAndResponses();

		Iterator it = origCars.getChallenges().iterator();
		while (it.hasNext()) {
			String challenge = (String) it.next();
			if (!submittedCars.containsKey(challenge)) {
				log("setAnswers(): removing challenge: " + challenge);
				carsToSet.removeChallengeResponse(challenge);
			}
		}

		return;
	} // removeOldChallenges

	/**
	 * Method updateNewOrModifiedChallenges
	 *
	 * Updates ChallengesAndResponses with any new or modified challenges.
	 *
	 * @param Map
	 *            submittedCars - challenges/responses that the user submitted
	 * @param ChallengesAndResponses
	 *            carsToSet - building this up to use later
	 * @throws RemoteException
	 * @throws ApplicationException
	 */

	private void updateNewOrModifiedChallenges(Map submittedCars,
			ChallengesAndResponses carsToSet) throws RemoteException,
			ApplicationException {

		Iterator it = submittedCars.keySet().iterator();
		while (it.hasNext()) {
			String challenge = (String) it.next();
			String response = (String) submittedCars.get(challenge);

			// Note: modifyChallengeResponse() does the same thing as
			// addChallengeResponse(), so it can be used for a modify or add
			// ondition!
			if (!response.equals(NOT_CHANGED)) {
				log("setAnswers(): updating challenge: " + challenge);
				carsToSet.modifyChallengeResponse(challenge, response);
			}
		}

	} // updateNewOrModifiedChallenges

} // ChangeChallengeResponseServlet
