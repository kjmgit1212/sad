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

/********************************************************************
 * FILE: %Z%%M%  %I%  %W% %G% %U%
 * 
 * This servlet contains an initialization method that must be executed
 * before the main.jsp page gains control.  Its responsibility is to check
 * if the TAM Service (as configured in the itim_expi.properties file) 
 * is active on the targetted ITIM Server.
 ********************************************************************/
package examples.expi;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ChallengeResponseManager;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.apps.system.SystemSubjectUtil;
import com.ibm.itim.apps.system.SystemUserMO;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.system.SystemUser;

import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = -3467953916282518352L;
	private HttpSession session;
	private static ExpiUtil utilObject = null;
	private String LOGON_PAGE = "/jsp/unprotected/logon.jsp";
	
	/**
	 * Method init
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() {
		log("mainServlet:init(): start");

		try {
			utilObject = new ExpiUtil();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// in case of Single Sign-On, forward to ssoerror.jsp page 
		// else forward to logon.jsp page.
		LOGON_PAGE = utilObject.getPropertySSOCheck(ExpiUtil.LOGON_PAGE);
		log("EXPI: mainServlet:init(): LOGON_PAGE = " + LOGON_PAGE);
		log("mainServlet:init(): end");
	}

	/**
	 * Method doGet
	 * Obtains the necessary attributes from the Request (user must be authenticated) and all the required
	 * data items provided in the session.  If all items are provided, the list of applications (TAM groups) is
	 * processed and control is forwarded to the applications.jsp page.  The jsp page builds a table of 
	 * Applications that the user may pick (select or un-select).
	 * 
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		
		Subject subject = null;
		PlatformContext platform = null;
		PersonMO personMo = null;
		SystemUserMO systemuserMo = null;
		String userID = null;
		
		req.setCharacterEncoding("UTF-8");

		System.out.println("doGet(): start");
		
		try{
			session = req.getSession(false);
			if (session == null) {
				System.out.println("No session");
				// See if we got here via SSO through WebSEAL
				//Retrieve the WAS Subject
				subject = WSSubject.getCallerSubject(); 
							
				if(subject!=null) {
					session = req.getSession(true);
					session.setAttribute(ExpiUtil.SUBJECT, subject);
					
					platform = utilObject.createPlatformContext();
					session.setAttribute(ExpiUtil.PLATFORM_CONTEXT, platform);
							
					//get the userid and set it in session. 
					//also check if the account is active or not.
					systemuserMo = 
						SystemSubjectUtil.getSystemUser(platform, subject);
					SystemUser sysUser = systemuserMo.getData();
					if(sysUser.isSuspended()) {
						System.out.println("EXPI: mainServlet:doGet() - " + 
											"user account suspended");
						resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
						return;
					}
					userID = sysUser.getName();
					session.setAttribute(ExpiUtil.LOGON_ID, userID);
					
					personMo = utilObject.lookupPerson(platform, subject);
					session.setAttribute(ExpiUtil.PERSONMO, personMo);
					
					// See if the user needs to update their challenge 
					//response answers. This sets msgs in the session as needed
					ChallengeResponseManager crm = new 
							ChallengeResponseManager(platform, subject);
					boolean changeNeeded = crm.isEnforceChallengeResponse();
					session.setAttribute("updateCRAnswers", 
										new Boolean(changeNeeded));	
					
				} else {
					req.setAttribute("message", 
								"The session is no longer valid");
					resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
					return;
				}
			
			}

			// do we have a valid subject to work with - this will be the case
			// if the logon was successful, otherwise not.

			if (!isSubjectAssigned(req, resp))
				return;

			session = (HttpSession) req.getSession(false);
			if (session == null) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " + 
					"no session - redirecting to logo page");
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
			}

			subject = (Subject) session.getAttribute(ExpiUtil.SUBJECT);

			if (subject == null) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " + 
					"no subject - redirecting to logon page");
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
				return;
			}

			platform = (PlatformContext) 
				session.getAttribute(ExpiUtil.PLATFORM_CONTEXT);

			if (platform == null) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " + 
					"no Platform Context object - redirecting to logon page");
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
				return;
			}

			userID = (String) session.getAttribute(ExpiUtil.LOGON_ID);
			if (userID == null) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " +
					"no userID found - redirecting to logon.jsp");
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
				return;
			}

			personMo = (PersonMO) session.getAttribute(ExpiUtil.PERSONMO);

			if (personMo == null) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " + 
					"no PersonMO object found - redirecting to logon page");
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
				return;
			}

			// load the Account information respective to the person
			String serviceDN = utilObject.getProperty(ExpiUtil.APP_SERVICE_DN);
		
			if (serviceDN == null || serviceDN.equals("")) {
				System.out.println(
					"EXPI: mainServlet:doGet() - " +
					"TAM Service DN not specified in property file");
				session.setAttribute(ExpiUtil.TAM_SERVICE_ACTIVE, "false");
			}
			else {
			
				AccountMO acctMO =
					utilObject.lookupAccounts(
						platform,
						subject,
						personMo,
						utilObject.getProperty(ExpiUtil.APP_SERVICE_DN));
				Account account = utilObject.account;
	
				if (account != null) {
					System.out.println(
						"EXPI: mainServlet:doGet() - " +
						"TAM Service is configured...forwarding to main.jsp");
	
					// save off the AccountMO and respective account for this 
					// service in order to reduce processing in the doPost
	
					session.setAttribute(ExpiUtil.ACCOUNTMO, acctMO);
					session.setAttribute(ExpiUtil.ACCOUNT, account);
					session.setAttribute(ExpiUtil.TAM_SERVICE_ACTIVE, "true");
				} else {
					System.out.println(
						"EXPI: mainServlet:doGet() - "
						+ utilObject.getProperty(ExpiUtil.APP_SERVICE_NAME)
						+ " is NOT configured...forwarding to main.jsp");
					session.setAttribute(ExpiUtil.TAM_SERVICE_ACTIVE, "false");
				}
			}
		
		}catch(WSSecurityException e){
			e.printStackTrace();
			ExpiUtil.log("EXPI: mainServlet:doGet(): " + e.getMessage());
			ExpiUtil.forward(req, resp, e.getMessage(), LOGON_PAGE);	
			return;
		}catch(ApplicationException ae){
			ae.printStackTrace();
			ExpiUtil.log("EXPI: mainServlet:doGet() " + ae.getMessage());
			ExpiUtil.forward(req, resp, ae.getMessage(), LOGON_PAGE);	
			return;
		}

		// always forward to the Home page (main.jsp by default)

		utilObject.forward(
			req,
			resp,
			"",
			utilObject.getProperty(ExpiUtil.HOME_PAGE));
	}
	

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Method isSubjectAssigned.
	 * Verifies if a Subject is provided in the request.
	 * @param req
	 * @param resp
	 * @return boolean - true if Subject was found, false otherwise
	 * @throws IOException
	 */
	private boolean isSubjectAssigned(
		HttpServletRequest req,
		HttpServletResponse resp)
		throws IOException {
		Subject subject = (Subject) session.getAttribute("subject");
		if (subject == null) {
			System.out.println("EXPI: mainServlet:isSubjectAssigned() Session is not valid (no subject).");

			session.invalidate();

			req.setAttribute("message", "The Session is no longer valid.");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
			return false;
		}
		return true;
	}
}
