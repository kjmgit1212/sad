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
 * selfCareServlet.java
 *
 * This servlet contains methods that control the self-care requirements
 * for the Servlet Portfolio Example.
 *
 * The user must provide a set of attributes that are considered a necessity
 * for self care.
 *
 * The attributes and data are displayed to the user via the selfcare.jsp.
 * The table that is used is dynamically created from the set of attributes
 * defined in the itim_expi.properties file.
 *
 * These attributes must correspond to the Person attribute names (see
 * the ITIM Schema for more information, and refer to the itim_expi.properties
 * file in the SelfCare attribute section). .
 *
 * A list of attributes is stored in the "attibutes.selfcare" variable (coma
 * delimitted).Each attribute, in turn, must have a counterpart variable that
 * defines the verbose text displayed on the selfRegistration.jsp
 *
 * Date: 5/2003
 *
 ********************************************************************/
package examples.expi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.SchemaViolationException;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.dataservices.model.domain.Person;

/**
 * @version 1.0
 * 
 * The selfcare servlet builds the dynamic content based on the properties file
 * (as specfied in expiUtils.java)
 * @author Vitek Boruvka
 */
public class SelfCareServlet extends HttpServlet {

	private static final long serialVersionUID = 7193099952158249375L;

	private static ExpiUtil utilObject = null;

	/**
	 * Summary of functionality: 1) validate all required data (includes
	 * subject, platform context, userID and person 2) get the Person object
	 * (and all attributs) 3) get the set of attributes to manipulate from the
	 * properties file 4) build table of data items to manipulate
	 * 
	 * @see javax.servlet.http.HttpServlet#void
	 *      (javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");

		try {
			utilObject = new ExpiUtil();
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpSession session = (HttpSession) req.getSession(false);
		if (session == null) {
			System.out
					.println("EXPI: selfCareServlet:doGet() - no session - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		Subject subject = (Subject) session.getAttribute(ExpiUtil.SUBJECT);

		if (subject == null) {
			System.out
					.println("EXPI: selfCareServlet:doGet() - no subject - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}
		PlatformContext platform = (PlatformContext) session
				.getAttribute(ExpiUtil.PLATFORM_CONTEXT);

		if (platform == null) {
			System.out
					.println("EXPI: selfCareServlet:doGet() - no Platform Context object - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		String userID = (String) session.getAttribute(ExpiUtil.LOGON_ID);
		if (userID == null) {
			System.out
					.println("EXPI: selfCareServlet:doGet() - no userID found - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		PersonMO personMo = (PersonMO) session.getAttribute(ExpiUtil.PERSONMO);

		if (personMo == null) {
			System.out
					.println("EXPI: selfCareServlet:doGet() - no PersonMO object found - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		try {
			utilObject.printAttributes(personMo.getData());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out
					.println("EXPI: selfCareServlet:doGet() - getData - RemoteException");
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out
					.println("EXPI: selfCareServlet:doGet() - getData - ApplicationException");
		}

		setRequestAttributes(req, personMo);

		// if an error was passed in forward that to the SELF care page also

		String msg = (String) session.getAttribute(ExpiUtil.ERR_LABEL);

		if (msg == null)
			msg = "";

		utilObject.forward(req, resp, msg, utilObject
				.getProperty(ExpiUtil.SELFCARE_PAGE));
	}

	/**
	 * Method setRequestAttributes.
	 * 
	 * @param req
	 * @param selfcareAttrs
	 * @param person
	 */
	public void setRequestAttributes(HttpServletRequest req, PersonMO personMo) {
		Person person = null;

		// 1st get the list of attributes that we need to manipulate
		// By Default this is contain in the properties variable SELFCARE_ATTRS
		// Add the attributes onto the request

		String selfcareAttrs = utilObject.getProperty(ExpiUtil.SELFCARE_ATTRS);
		System.out.println("selfcare attrs: " + selfcareAttrs);

		if (selfcareAttrs != null) {
			try {
				person = personMo.getData();
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: selfCareServlet:doGet() - getData - RemoteException");
			} catch (ApplicationException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: selfCareServlet:doGet() - getData - ApplicationException");
			}
		}

		utilObject.printAttributes(person);

		StringTokenizer st = new StringTokenizer(selfcareAttrs, ",");
		while (st.hasMoreTokens()) {
			String sAttr = st.nextToken();

			AttributeValue sValue;

			sValue = person.getAttribute(sAttr);
			if (sValue != null && !sValue.isValueEmpty()) {
				// debug only - print the attributes we're playing with

				System.out.println("   Attr: " + sAttr + " = "
						+ sValue.getString());
				req.setAttribute(sAttr, sValue.getString());
			} else
				req.setAttribute(sAttr, "");
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#void
	 *      (javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");

		if (utilObject == null) {
			System.out
					.println("EXPI: selfCareServlet:doPost() - expected a utilObject - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		HttpSession session = (HttpSession) req.getSession(false);
		if (session == null) {
			System.out
					.println("EXPI: selfCareServlet:doPost() - no session - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		PersonMO personMo = (PersonMO) session.getAttribute(ExpiUtil.PERSONMO);

		if (personMo == null) {
			System.out
					.println("EXPI: selfCareServlet:doPost() - no PersonMO object found - redirecting to logon page");
			resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
		}

		String selfcareAttrs = utilObject.getProperty(ExpiUtil.SELFCARE_ATTRS);
		System.out.println("selfcare attrs: " + selfcareAttrs);

		if (selfcareAttrs != null) {
			Person person = null;

			try {
				person = personMo.getData();
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: selfCareServlet:doPost() - getData - RemoteException");
			} catch (ApplicationException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: selfCareServlet:doPost() - getData - ApplicationException");
			}

			// in order to provide additional flexibility - additional
			// attributes may be
			// added to the JSP that are NOT part of the property file. Thus we
			// enumerate
			// the paratemers rather than decipher then based only on the
			// property file.

			Enumeration _enum = req.getParameterNames();

			while (_enum.hasMoreElements()) {
				String element = (String) _enum.nextElement();
				String sValue = req.getParameter(element);

				System.out.println("EXPI: selfCareServlet:doPost() - Updating: " + element + ": " + sValue);

				AttributeValue attrValue;

				attrValue = new AttributeValue(element, sValue);
				person.setAttribute(attrValue);
			}

			try {
				//Request request = personMo.update(person, new Date());
				personMo.update(person, null);

				utilObject.forward(req, resp, "", utilObject
						.getProperty(ExpiUtil.SELFCARESUB_PAGE));
			} catch (AuthorizationException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: personMo.update() - getData - AuthorizationException");

				setRequestAttributes(req, personMo);

				utilObject.forward(req, resp, "Error - AuthorizationException",
						utilObject.getProperty(ExpiUtil.SELFCARE_PAGE));
			} catch (SchemaViolationException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: personMo.update() - getData - SchemaViolationException");
				utilObject.forward(req, resp,
						"Error - SchemaViolationException", utilObject
								.getProperty(ExpiUtil.SELFCARE_PAGE));
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: personMo.update) - getData - RemoteException");
				utilObject.forward(req, resp, "Error - RemoteException",
						utilObject.getProperty(ExpiUtil.SELFCARE_PAGE));
			} catch (ApplicationException e) {
				e.printStackTrace();
				System.out
						.println("EXPI: personMo.update() - getData - ApplicationException");
				utilObject.forward(req, resp, "Error - ApplicationException",
						utilObject.getProperty(ExpiUtil.SELFCARE_PAGE));
			}
		}

	}
}
