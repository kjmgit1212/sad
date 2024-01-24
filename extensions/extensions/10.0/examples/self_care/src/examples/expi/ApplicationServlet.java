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
 * This servlet contains methods to control the display and selection of
 * Applications that correspond to TAM Group. It serves as the integration
 * piece between ITIM and TAM.
 ********************************************************************/
package examples.expi;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.domain.Account;

public class ApplicationServlet extends HttpServlet {

	private static final long serialVersionUID = -395926710463746600L;

	private HttpSession session;

	private static ExpiUtil utilObject = null;

	private String LOGON_PAGE = "logon.jsp";

	/**
	 * Method init
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() {
		log("applicationServlet:init(): start");

		try {
			utilObject = new ExpiUtil();
		} catch (Exception e) {
			e.printStackTrace();
		}

		LOGON_PAGE = utilObject.getPropertySSOCheck(ExpiUtil.LOGON_PAGE);
		log("EXPI:init(): LOGON_PAGE = " + LOGON_PAGE);
		log("applicationServlet:init(): end");
	}

	/**
	 * Method doGet Obtains the necessary attributes from the Request (user must
	 * be authenticated) and all the required data items provided in the
	 * session. If all items are provided, the list of applications (TAM groups)
	 * is processed and control is forwarded to the applications.jsp page. The
	 * jsp page builds a table of Applications that the user may pick (select or
	 * un-select).
	 * 
	 * @see javax.servlet.http.HttpServlet#void
	 *      (javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		System.out.println("applicationServlet:doGet()");

		session = req.getSession(false);
		if (session == null) {
			System.out.println("Session is not valid.");
			req.setAttribute("message", "The Session is no longer valid");
			resp.sendRedirect(req.getContextPath() + "/jsp/unprotected/logon.jsp");
			return;
		}

		// do we have a valid subject to work with - this will be the case
		// if the logon was successful, otherwise not.

		if (!isSubjectAssigned(req, resp))
			return;

		HttpSession session = (HttpSession) req.getSession(false);
		if (session == null) {
			System.out
					.println("EXPI: applicationServlet:doGet() - no session - redirecting to logo page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		Subject subject = (Subject) session.getAttribute(ExpiUtil.SUBJECT);

		if (subject == null) {
			System.out
					.println("EXPI: applicationServlet:doGet() - no subject - redirecting to logon page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		PlatformContext platform = (PlatformContext) session
				.getAttribute(ExpiUtil.PLATFORM_CONTEXT);

		if (platform == null) {
			System.out
					.println("EXPI: applicationServlet:doGet() - no Platform Context object - redirecting to logon page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		String userID = (String) session.getAttribute(ExpiUtil.LOGON_ID);
		if (userID == null) {
			System.out
					.println("EXPI: applicationServlet:doGet() - no userID found - redirecting to logon.jsp");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		PersonMO personMo = (PersonMO) session.getAttribute(ExpiUtil.PERSONMO);

		if (personMo == null) {
			System.out
					.println("EXPI: applicationServlet:doGet() - no PersonMO object found - redirecting to logon page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		// load the Account information respective to the person

		AccountMO acctMO = utilObject.lookupAccounts(platform, subject,
				personMo, utilObject.getProperty(ExpiUtil.APP_SERVICE_DN));
		Account account = utilObject.account;

		if (account != null) {
			// save off the AccountMO and respective account for this service in
			// order to reduce
			// processing in the doPost...

			session.setAttribute(ExpiUtil.ACCOUNTMO, acctMO);
			session.setAttribute(ExpiUtil.ACCOUNT, account);

			// add the group names from the property file to the request header.
			// Those placed in the
			// request header are currently selected groups. Those left off are
			// configured in the
			// property file but are not selected.

			setRequestAttributes(req, account.getAttributes());

			ExpiUtil.forward(req, resp, "", utilObject
					.getProperty(ExpiUtil.APPLICATIONS_PAGE));
		} else {
			System.out
					.println("EXPI: applicationServlet:doGet() - no Account found for Service '"
							+ utilObject.getProperty(ExpiUtil.APP_SERVICE_NAME)
							+ " - redirecting to selfCareServlet");
			ExpiUtil.forward(
							req,
							resp,
							"Application subscriptions have not been configured at this time.",
							utilObject.getProperty(ExpiUtil.HOME_PAGE));
		}
	}

	/**
	 * Method doPost Processes the selections made in the applications.jsp. A
	 * delta of the selected and unselected groups is made an any changes are
	 * submitted to the account update process.
	 * 
	 * @see javax.servlet.http.HttpServlet#void
	 *      (javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String sGroup;

		session = req.getSession(false);
		if (session == null) {
			System.out
					.println("EXPI: applicationServlet:doPost() - Session is not valid.");
			req.setAttribute("message", "The Session is no longer valid");

			// under SSO we can't error out to the logon servelet either.
			// In this case we use the mapped LOGON_PAGE (which should be the
			// ssoerror.jsp

			if (utilObject.isSSOEnabled())
				resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
			else
				resp.sendRedirect(ExpiUtil.LOGON_SERVLET);
			return;
		}

		System.out.println("EXPI: applicationServlet:doPost()");
		Enumeration _enum = req.getParameterNames();

		while (_enum.hasMoreElements()) {
			String element = (String) _enum.nextElement();
			String sValue = (String) req.getParameter(element);

			System.out.println("EXPI: Parameter: " + element + ": " + sValue);
		}

		if (!isSubjectAssigned(req, resp))
			return;

		AccountMO acctMO = (AccountMO) session.getAttribute(ExpiUtil.ACCOUNTMO);
		if (acctMO == null) {
			System.out
					.println("EXPI: applicationServlet:doPost() - no accountMO - redirecting to logon page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		Account account = (Account) session.getAttribute(ExpiUtil.ACCOUNT);
		if (account == null) {
			System.out
					.println("EXPI: applicationServlet:doPost() - no account - redirecting to logon page");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
		}

		// build the consolidated lists of new groups and those to be removed.

		AttributeValues attrValues = account.getAttributes();
		Collection<String> colExistingRoles = utilObject
				.getTamGroups(attrValues);
		Collection<String> colNewRoles = new Vector<String>(0);

		// get the set of applications (groups) that can are configured
		// in the properties file...

		Collection colDefinedRoles = getDefinedGroups();

		// get the roles selected in the JSP
		_enum = req.getParameterNames();
		while (_enum.hasMoreElements()) {
			String element = (String) _enum.nextElement();
			sGroup = req.getParameter(element);
			if (!sGroup.equals("")) {
				if (!colExistingRoles.contains(sGroup)) {
					colNewRoles.add(sGroup);
				}
				colDefinedRoles.remove(sGroup);
			}
		}

		// now run through the collection and consolidate the groups that are to
		// be
		// added and deleted

		boolean bChanges = false;

		// build collection on new groups
		Iterator it = colNewRoles.iterator();
		while (it.hasNext()) {
			sGroup = (String) it.next();
			colExistingRoles.add(sGroup);
			bChanges = true;
		}

		// update the list for those that are to be deleted (these were not part
		// of the parameters
		// that were selected...thus are deleted...

		it = colDefinedRoles.iterator();
		while (it.hasNext()) {
			sGroup = (String) it.next();
			colExistingRoles.remove(sGroup);
			bChanges = true;
		}

		// if any changes need to be made, set the attribute.
		// NOTE: only the groups in the property files get manipulated...
		// other groups are left untouched.

		if (bChanges) {
			System.out.println("EXPI: New Group Attr  = "
					+ colExistingRoles.toString());

			// add the new roles to the attribute list
			AttributeValue modAttrVal = new AttributeValue(utilObject
					.getProperty(ExpiUtil.APP_SERVICE_ATTR), colExistingRoles);

			account.setAttribute(modAttrVal);

			// update the account object with the new attributes...and if
			// successful,
			// forward to the applications submitted page...otherwise forward back
			// to the self-care page

			if (utilObject.updateAccount(acctMO, account)) {
				ExpiUtil.forward(req, resp, "", utilObject
						.getProperty(ExpiUtil.APPLICATIONSSUB_PAGE));
				return;
			}
		}

		ExpiUtil.forward(
						req,
						resp,
						"Application Subscription failed (please check the server logs).",
						utilObject.getProperty(ExpiUtil.SELFCARE_PAGE));
	}

	/**
	 * Method isSubjectAssigned. Verifies if a Subject is provided in the
	 * request.
	 * 
	 * @param req
	 * @param resp
	 * @return boolean - true if Subject was found, false otherwise
	 * @throws IOException
	 */
	private boolean isSubjectAssigned(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		Subject subject = (Subject) session.getAttribute("subject");
		if (subject == null) {
			System.out.println("EXPI: Session is not valid (no subject).");

			session.invalidate();

			req.setAttribute("message", "The Session is no longer valid.");
			resp.sendRedirect(req.getContextPath() + LOGON_PAGE);
			return false;
		}
		return true;
	}

	/**
	 * Method setRequestAttributes.
	 * 
	 * @param req
	 * @param selfcareAttrs
	 * @param person
	 */
	public void setRequestAttributes(HttpServletRequest req,
			AttributeValues attrValues) {
		System.out.println("EXPI: setRequestAttributes - Groups");

		Collection roles = utilObject.getTamGroups(attrValues);

		System.out.println("EXPI: Account contains:: ");
		Iterator iter = roles.iterator();
		while (iter.hasNext()) {
			System.out.println("Group: " + iter.next());
		}

		String groups = utilObject.getProperty(ExpiUtil.APP_LIST);

		StringTokenizer st = new StringTokenizer(groups, ",");
		while (st.hasMoreTokens()) {
			String sAttr = st.nextToken();
			String appPropertyName = new String("application." + sAttr
					+ ".name");
			String appPropertyDN = new String("application." + sAttr + ".dn");

			if (appPropertyName != null) {
				// System.out.println("EXPI: sAttr=" + sAttr + " App Property: "
				// + appPropertyName);

				String appName = new String(utilObject
						.getProperty(appPropertyName));
				if (appName != null) {
					String appNameDN = new String(utilObject
							.getProperty(appPropertyDN));

					if (roles.contains(appNameDN)) {
						req.setAttribute(sAttr, appName);
						System.out.println("EXPI: sAttr: (dn=" + appNameDN
								+ ") " + sAttr + " - " + appName);
					} else {
						System.out.println("EXPI: sAttr: (dn=" + appNameDN
								+ ") " + sAttr + " - (blank)");
						req.setAttribute(sAttr, "");
					}
				} else
					req.setAttribute(sAttr, "");
			} else
				req.setAttribute(sAttr, "");
		}
	}

	/**
	 * Method getDefinedApplications. Returns a collection of defined
	 * application names (DN's of the Groups) from the property file.
	 * 
	 * @return Collection
	 */
	public Collection<String> getDefinedGroups() {
		Collection<String> colRoles = new Vector<String>(0);

		System.out.println("EXPI: getDefinedApplications - Groups: ");

		String appString = utilObject.getProperty(ExpiUtil.APP_LIST);
		System.out.println("EXPI: Application List: " + appString);

		StringTokenizer st = new StringTokenizer(appString, ",");
		while (st.hasMoreTokens()) {
			String sAttr = st.nextToken();
			String appPropertyName = new String("application." + sAttr
					+ ".name");
			String appPropertyDN = new String("application." + sAttr + ".dn");

			if (appPropertyName != null) {
				System.out.println("EXPI: App Property: " + appPropertyName);

				String appName = new String(utilObject
						.getProperty(appPropertyName));
				if (appName != null) {
					System.out.println("EXPI: App Name: " + appName);

					String appNameDNStr = new String(utilObject
							.getProperty(appPropertyDN));

					colRoles.add(new String(appNameDNStr));
				}
			}
		}

		return colRoles;
	}

}
