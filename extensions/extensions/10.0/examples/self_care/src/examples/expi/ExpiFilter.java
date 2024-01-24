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
package examples.expi;

import java.io.IOException;
import java.util.Enumeration;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ChallengeResponseManager;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSLoginFailedException;
import com.ibm.websphere.security.auth.WSSubject;

/**
 * @author Administrator
 *
 */
public class ExpiFilter implements Filter {


	private FilterConfig m_filterConfig = null;
	
	private ExpiUtil m_expiUtil = null;
	
	private static String LOGON_CONTEXT_ID;

	private static String LOGON_PAGE;

	private static String HOME_PAGE;

	private static String SELF_CHANGEPWD_PAGE;

	private boolean bSSOEnabled = false;
	
	/**
	 * init() : init() method called when the filter is instantiated.
	 * This filter is instantiated the first time mapped url is 
	 * invoked for the application.
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		ExpiUtil.log("ExpiFilter.init(): begin");

		m_filterConfig = filterConfig;
		m_expiUtil = new ExpiUtil();

		m_expiUtil = new ExpiUtil();
		if (m_expiUtil != null && m_expiUtil.isProperties()) {
			LOGON_CONTEXT_ID = m_expiUtil.getLoginContextID();
			ExpiUtil.log("ExpiFilter.init(): LOGON_CONTEXT_ID = " + LOGON_CONTEXT_ID);

			LOGON_PAGE = m_expiUtil.getPropertySSOCheck(ExpiUtil.LOGON_PAGE);
			ExpiUtil.log("ExpiFilter.init(): LOGON_PAGE = " + LOGON_PAGE);

			HOME_PAGE = m_expiUtil.getProperty(ExpiUtil.HOME_PAGE);
			ExpiUtil.log("ExpiFilter.init(): HOME_PAGE = " + HOME_PAGE);

			bSSOEnabled = m_expiUtil.isSSOEnabled();
			ExpiUtil.log("ExpiFilter.init(): bSSOEnabled = " + bSSOEnabled);

			SELF_CHANGEPWD_PAGE = m_expiUtil
					.getProperty(ExpiUtil.SELF_CHANGEPWD_PAGE);
			ExpiUtil.log("ExpiFilter.init(): SELF_CHANGEPWD_PAGE = " + SELF_CHANGEPWD_PAGE);
		}

		ExpiUtil.log("ExpiFilter.init(): end");
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// pre login action
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		
		// check if we found a properties file
		if (m_expiUtil == null || m_expiUtil.isProperties() == false) {
			ExpiUtil.log("ExpiFilter.init(): Could not find properties file");
			ExpiUtil.forward(req, res,
					"Could not initialize: Property file missing?", LOGON_PAGE);	
			return;
		}

		String userID = req.getParameter(ExpiUtil.LOGON_ID);
		String password = req.getParameter("j_password");
		
		/**
		 * Need a session whether the login was successful or not! If
		 * unsuccessful login, need a session to hold the error info.
		 */
		HttpSession session = req.getSession(false);
		if (session == null) {
			session = req.getSession(true);
		}

		String sessionValues = sessionToString(session);
		ExpiResponseWrapper responseWrapper = new ExpiResponseWrapper(res);
		
		// call next filter in the chain (login)
		chain.doFilter(request, responseWrapper);

		// post login actions...
		// check for login error
		Throwable t = WSSubject.getRootLoginException();
		if (t != null) {
			t = determineCause(t);
			addMessageToCookie(responseWrapper, t.getMessage());
			
		} else {
			// Authentication is successful.
			session.setAttribute(ExpiUtil.LOGON_ID, userID);


			try {
				Subject subject = WSSubject.getCallerSubject();
				if (userID.trim().length() == 0 || password.length() == 0) {
					ExpiUtil.log("ExpiFilter.init(): empty userID or password.");
					addMessageToCookie(responseWrapper, "Empty user ID or password is not allowed");
					
				} else if (subject == null ) {
					ExpiUtil.log("ExpiFilter.init(): subject is null");
					addMessageToCookie(responseWrapper, "WAS security is not enabled.");
					
				} else {

					session.setAttribute(ExpiUtil.SUBJECT, subject);

					ExpiUtil.log("ExpiFilter.doFilter(): logged user: " + req.getRemoteUser());

					PlatformContext platform = m_expiUtil.createPlatformContext();
					session.setAttribute(ExpiUtil.PLATFORM_CONTEXT, platform);

					// Check the ITIM user password should be changed
					boolean resetPwd = m_expiUtil.isPasswordChangeRequired(platform, subject);
					if (resetPwd) {
						session.setAttribute("passwordExpired", new Boolean(true));
						ExpiUtil.log("ExpiFilter.doFilter(): Password is expired, forwarding to expiredpassword page");
						ExpiUtil.forward(req, res, "", SELF_CHANGEPWD_PAGE);
						return;
					}

					// Get the PersonMO object that corresponds to the logged in user
					PersonMO personMo = m_expiUtil.lookupPerson(platform, subject);
					session.setAttribute(ExpiUtil.PERSONMO, personMo);

					// See if the user needs to update their challenge response answers
					// This sets msgs in the session as needed

					checkChallengeResponse(session, platform, subject);
				}
			} catch (WSSecurityException e) {
				e.printStackTrace();
				ExpiUtil.log("ExpiFilter.init(): " + e.getMessage());
				ExpiUtil.forward(req, res, e.getMessage(), LOGON_PAGE);	
				return;

			} catch (ApplicationException e) {
				e.printStackTrace();
				ExpiUtil.log("ExpiFilter.init(): " + e.getMessage());
				ExpiUtil.forward(req, res, e.getMessage(), LOGON_PAGE);	
				return;
				
			}
		}
		// redirect to the original request page.
		responseWrapper.sendMyRedirect();
	}
	
	private void addMessageToCookie(ExpiResponseWrapper responseWrapper, String msg) {
		Cookie c = new Cookie(ExpiUtil.EXPI_LOGIN_ERROR, msg);
		c.setMaxAge(-1);
		responseWrapper.addCookie(c);
	}
	
	private void removeMessageFromCookie(ExpiResponseWrapper responseWrapper) {
		Cookie c = new Cookie(ExpiUtil.EXPI_LOGIN_ERROR, "");
		c.setMaxAge(0);
		responseWrapper.addCookie(c);
	}

	/**
	 * Method checkChallengeResponse.
	 *
	 * Determine whether the user who is logging in needs to update his
	 * challenge response answers. This should be true: - The first time a user
	 * logs on - When an administrator makes a change to the system-wide
	 * challenge response settings that causes the user to update his settings
	 *
	 * Update the session with any needed messages. These will be used by
	 * main.jsp.
	 *
	 * @param session
	 * @param platform
	 * @param subject
	 */
	private void checkChallengeResponse(HttpSession session,
			PlatformContext platform, Subject subject) {

		try {
			ChallengeResponseManager crm = new ChallengeResponseManager(
					platform, subject);

			boolean changeNeeded = crm.isEnforceChallengeResponse();
			ExpiUtil.log("ExpiFilter.checkChallengeResponse(): challenge response updated needed = "
					+ changeNeeded);

			// Need to set to keep the session up-to-date
			session.setAttribute("updateCRAnswers", new Boolean(changeNeeded));
		} catch (Exception e) {
			ExpiUtil.log("EXPI: checkChallengeResponse(): Caught an exception: "
					+ e.getMessage());
			session.setAttribute("errorMessage", e.getMessage());
		}

	} // checkChallengeResponse
	
	private String sessionToString(HttpSession session) {
		StringBuffer sb = new StringBuffer("Session:[sessionID:" + session.getId() + "]");
		sb.append("  [isNew:" + session.isNew() + "]\n");
		for(Enumeration e = session.getAttributeNames(); e.hasMoreElements(); ) {
			String attrName = (String) e.nextElement();
			Object attrValue = session.getAttribute(attrName);
			sb.append("  [" + attrName + ":" + attrValue + "]\n");
		}
		return sb.toString();
	}


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		m_filterConfig = null;
	}
	
	public Throwable determineCause(Throwable e) {
		Throwable rootEx = e, tempEx = null;
		// keep looping until there are no more embedded
		// WSLoginFailedException or WSSecurityException exceptions

		while (true) {
			if (e instanceof WSLoginFailedException) {
				tempEx = ((WSLoginFailedException)e).getCause();

			} else if (e instanceof WSSecurityException) {
				tempEx = ((WSSecurityException)e).getCause();
				
			} else {
				// this is the root from the WebSphere 
				//  Application Server perspective
				return rootEx;
			}

			if (tempEx != null) {
				// we have nested exception, check it
				rootEx = tempEx;
				e = tempEx;
				continue;
			} else {
				// the cause was null, return parent
				return rootEx;
			}
		}	//while
	}
	
	class ExpiResponseWrapper extends HttpServletResponseWrapper {

		String m_originalRedirect; 
		String m_contextPath;

		public ExpiResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			// just store location, don't send redirect to avoid 
			// committing response
			m_originalRedirect = location;
		}

		// use this method to send redirect after modifying response
		public void sendMyRedirect() throws IOException {
			if (m_originalRedirect.indexOf("itim_expi") == -1) {
				m_originalRedirect = "/itim_expi/mainServlet";
			}
			super.sendRedirect(m_originalRedirect);
		}
	}
}
