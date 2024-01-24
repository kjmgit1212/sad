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
 * This file contains helper methods and definitions used in the
 * Servlet Portfolio Example that interfaces to ITIM via
 * External APIs.
 *
********************************************************************/
package examples.expi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.apps.ApplicationException;
import com.ibm.itim.apps.AuthorizationException;
import com.ibm.itim.apps.InitialPlatformContext;
import com.ibm.itim.apps.PlatformContext;
import com.ibm.itim.apps.identity.ContainerManager;
import com.ibm.itim.apps.identity.OrganizationalContainerMO;
import com.ibm.itim.apps.identity.PersonMO;
import com.ibm.itim.apps.provisioning.AccountMO;
import com.ibm.itim.apps.provisioning.AccountManager;
import com.ibm.itim.apps.system.SystemSubjectUtil;
import com.ibm.itim.apps.system.SystemUserMO;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DirectoryObject;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.Person;

/**
 *
 * This class contains several statics constants that provide initialization support
 * to the EXPI code base.  In addition, helper functions provide ITIM API support.
 */
public class ExpiUtil {
	public static final String EXPI_LOGIN_ERROR = "expiLoginError";
	public static final String ERR_LABEL = "errorMessage";
	public static final String SERVICE_ENROLE_PROFILE_NAME = "ITIMService";
	public static final String LOGON_SERVLET = "logonServlet";
	public static final String LOGON_PAGE = "page.logon";
	public static final String LOGON_ERROR = "logon.error";
	public static final String SSO_ERROR_PAGE = "page.ssoerror";
	public static final String LOGOUT_PAGE = "page.logout";
	public static final String HOME_PAGE = "page.home";
	public static final String SELF_CHANGEPWD_PAGE = "page.selfchangepwd";
	public static final String SELF_CHANGEPWD_INFO_PAGE = "page.selfchangepwdinfo";
	public static final String CHANGEPWD_PAGE = "page.changepwd";
	public static final String CHANGEPWDINFO_PAGE = "page.changepwdinfo";
	public static final String PWDRULESINFO_PAGE = "page.pwdrulesinfo";
	public static final String FORGOTPWD_PAGE = "page.forgotpwd";
	public static final String FORGOTPWDINFO_PAGE = "page.forgotpwdinfo";
	public static final String CRANSWERPAGE_PAGE = "page.cranswers";
	public static final String CRANSWERPAGEINFO_PAGE = "page.cranswersinfo";
	public static final String CRFORGOTPWD_PAGE = "page.crforgotpwd";
	public static final String SELFCARE_PAGE = "page.selfcare";
	public static final String SELFCARESUB_PAGE = "page.selfcare.submitted";
	public static final String SELFCARE_FOOTER_PAGE = "page.selfcare_footer";
	public static final String SELFREGISTRATION_PAGE = "page.selfregister";
	public static final String SELFREGISTRATIONSUB_PAGE = "page.selfregister.submitted";
	public static final String APPLICATIONS_PAGE = "page.applications";
	public static final String APPLICATIONSSUB_PAGE = "page.applications.submitted";
	public static final String SELFCARE_ATTRS = "attributes.selfcare";

	public static final String CHANGEONLYITIMPASSWORD = "changeonlytimpassword";

	public static final String TAM_SERVICE_NAME = "service.tam.name";
	public static final String TAM_SERVICE_ACTIVE = "tamserviceactive";

	public static final String PROPS_FILE = "itim_expi.properties";
    public static final String PLATFORMCONTEXTFACTORY = "platformContextFactory";
	public static final String LOGIN_CONTEXT = "loginContext";
	public static final String PLATFORM_CONTEXT = "platform";
	public static final String SUBJECT = "subject";
	public static final String ITIM_PASSWORD2 = "erpassword2";
	public static final String ITIM_PASSWORD = "erpassword";
	public static final String PERSON_DEFAULT_PROFILE = "Person";
	public static final String PLATFORM_URL = "platform.url";
	public static final String PLATFORM_PRINCIPAL = "platform.principal";
	public static final String PLATFORM_CREDENTIALS = "platform.credentials";

	public static final String APP_SERVICE_NAME = "application.service.name";
	public static final String APP_SERVICE_DN = "application.service.dn";
	public static final String APP_SERVICE_ATTR = "application.service.attribute";
	public static final String APP_LIST = "application.list";
	public static final String TENANT_ID = "tenantid";
	public static final String TENANT_DN = "tenantdn";
	public static final String USER_ID = "userid";
	public static final String LOGON_ID = "j_username";
	public static final String PWD = "password";
	public static final String LOGGED_ON= "loggedOn";
	public static final String PERSONMO = "personMO";
	public static final String ACCOUNTMO = "accountMO";
	public static final String ACCOUNT = "account";
	public static final String SSO_ENABLED = "ssoEnabled";
	public static final String SSO_SESSION_LOGON_ID = "ssoSessionID";

	public static final String SELF_REG_LOCATION = "orgContainer.selfregister.location.org";
	public static final String SELF_REG_LOCATION_ATTR = "orgContainer.selfregister.location.attr";

	public static final String DEFAULT_ORG_NAME = "default.org";
	public static final String GET_PER_ATTR_USERID = "person.getPerson.attr.uid";
        public static final String LANGUAGE = "language";
	public static final String COUNTRY = "country";
	public static String ACCOUNT_ATTR_ROLE = "erroles";

	private static ExpiUtil instance = null;
	private Properties properties = null;
	;
	public PlatformContext platform = null;
	public Account account = null;
	private boolean bTAMInstalled = false;

	/**
	 * Method createClientContext.
	 * returns the PlatformCallbackHandler for the PlatformContext (as specified by the properties file),
	 * used to logon the user via the JAAS interface. The user ID and password must be supplied by the
	 * request header.
	 *
	 * @param req
	 */
	public void createClientContext(HttpServletRequest req) {
		String password = null;
		String userID = null;

		// get the parameters from the request header

		userID = (String) req.getParameter("logonID");
		password = (String) req.getParameter("password");

		System.out.println("EXPI: User: " + userID + " - pwd: " + password);

		try {
			platform = createPlatformContext();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method createPlatformContext.
	 *
	 * 	returns a PlatformContext created from the properties
	 * 	PLATFORMCONTEXTFACTORY, PLATFORM_URL, PLATFORM_PRINCIPAL, PLATFORM_CREDENTIALS.
	 *
	 * @return PlatformContext
	 * @throws ApplicationException
	 * @throws RemoteException
	 */
	public PlatformContext createPlatformContext()
		throws ApplicationException, RemoteException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		try {
			env.put(
				InitialPlatformContext.CONTEXT_FACTORY,
				getProperty(PLATFORMCONTEXTFACTORY));
			env.put(PlatformContext.PLATFORM_URL, getProperty(PLATFORM_URL));
			env.put(
				PlatformContext.PLATFORM_PRINCIPAL,
				getProperty(PLATFORM_PRINCIPAL));
			env.put(
				PlatformContext.PLATFORM_CREDENTIALS,
				getProperty(PLATFORM_CREDENTIALS));
		} catch (RuntimeException e) {
			e.printStackTrace();
			return null;
		}

		try {
			platform = new InitialPlatformContext(env);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw e;
		} catch (ApplicationException e) {
			e.printStackTrace();
			throw e;
		}

		return platform;
	}

	/**
	 * Returns the platform.
	 * @return PlatformContext
	 */
	public PlatformContext getPlatform() {
		return platform;
	}

	/**
	 * Sets the platform.
	 * @param platform The platform to set
	 */
	public void setPlatform(PlatformContext platform) {
		this.platform = platform;
	}

	/**
	 * Creator
	 * Responsible for loading the property file on startup.
	 * @see java.lang.Object#Object()
	 */
	public ExpiUtil() {
		instance = this;
		properties = null;

		try {
			loadProperties();
		} catch (Exception e) {
			System.err.println("expiUtil: Failed to initialize");
			e.printStackTrace();
		}

		if (properties == null)
			System.err.println("expiUtil: properties not initialized");
	}

	/**
	 * Returns the singleton instance.
	 */
	public static ExpiUtil getInstance() throws Exception {
		if (instance == null) {
			instance = new ExpiUtil();
		}
		return instance;
	}

	/**
	 * Method getProperty.
	 * returns the value for the supplied property (attribute)
	 * @param key - attribute name to return a value for
	 * @return String
	 */
	public String getProperty(String key) {
		try {
			return properties.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}

	/**
	 * Method getPropertySSOCheck.
	 * If SSO is enabled, and we're trying to fetch the LOGON_PAGE property,
	 * map the returned value to the SSO_ERRO_PAGE.  The logon page is not a valid
	 * target for redirection or forwards. This control can be overridden by calling
	 * the getProperty(String key) method.
	 * @param key
	 * @return String
	 */
	public String getPropertySSOCheck(String key) {
		try {
			/*
			System.out.println(
				"getPropertySSOCheck: " + key + "=" + properties.getProperty(key));
				*/
			if (isSSOEnabled() && key.equals(LOGON_PAGE)) {
				// override the key we are looking for.
				key = SSO_ERROR_PAGE;
				System.out.println(
					"getPropertySSOCheck: remapped - " + key + "=" + properties.getProperty(key));

			}
			return properties.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}

	/**
	 * Method loadProperties.
	 * Loads the file into a property object (local object)
	 * @throws Exception
	 */
	public void loadProperties() throws Exception {
		properties = new Properties();
		System.out.println("EXPI: loadProperties:");
		try {
			String dir = findDir();
			properties.load(
				new FileInputStream(dir + File.separator + PROPS_FILE));
		} catch (IOException ioe) {
			ioe.printStackTrace();

			// failed the properties load so reset the properties variable

			properties = null;
			throw ioe;
		}
	}

	/**
	 * Method findDir.
	 * Finds the location of the property file from the system classpath
	 * @return String - directory in which the property file was found.
	 * 					 If not found, returns an empty string.
	 * @throws Exception
	 */
	private static String findDir() throws Exception {
		String dir = "";
		try {
			String classPath = System.getProperty("java.class.path");
			StringTokenizer st =
				new StringTokenizer(classPath, File.pathSeparator);
			while (st.hasMoreTokens()) {
				String dirName = st.nextToken();

				System.out.println("EXPI: Searching: " + dirName);
				File file = new File(dirName, PROPS_FILE);
				if (file.isFile()) {
					dir = dirName;
					System.out.println("EXPI: Found property file: " + dirName);
					return dir;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return dir;
	}

	/**
	 * Method lookupPerson.
	 * Objects the personMO object for the specified subject
	 * @param platformLogin
	 * @param subject - subject for which we're searching for
	 * @return PersonMO - null if subject was not found, otherwise
	 * 					returns the PersonMO object respective to the subject.
	 */
	public PersonMO lookupPerson(
		PlatformContext platformLogin,
		Subject subject) {
		System.out.println("expiUtil.lookupPerson(): start");

		SystemUserMO systemUserMO = null;
		try {
			systemUserMO = SystemSubjectUtil.getSystemUser(platformLogin, subject);
			System.out.println("expiUtil.lookupPerson(): SystemUserMO: " + systemUserMO.toString());
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out.println(
				"EXPI: SystemSubjectUtil.getSystemUser - ApplicationException");
		}
		if (systemUserMO == null) return null;

		PersonMO personMO = null;
		try {
			personMO = systemUserMO.getOwner();
			System.out.println("expiUtil.lookupPerson(): Got personMO: " + personMO.toString());
		} catch (AuthorizationException e) {
			e.printStackTrace();
			System.out.println(
				"EXPI: accountMO.getOwner - AuthorizationException");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("expiUtil.lookupPerson(): accountMO.getOwner - RemoteException");
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out.println(
				"EXPI: accountMO.getOwner - ApplicationException");
		}

		if (personMO != null) {
			Person person = null;
			try {
				person = personMO.getData();
				System.out.println("expiUtil.lookupPerson(): Got person: " + person.toString());

			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("EXPI: personMO.getData - RemoteException");
			} catch (ApplicationException e) {
				e.printStackTrace();
				System.out.println(
					"EXPI: personMO.getData - ApplicationException");
			}

			if (person != null) {
				AttributeValue cnAV = person.getAttribute("cn");
				System.out.println("AttributeValue: cn = " + cnAV.getString());
				return personMO;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if the ITIM account password should be changed.  The ITIM account
	 * password should be changed if the authentication user repository is ITIM custom
	 * user repository and the password of an authenticated ITIM system user is expired.
	 *  
	 * @param platform PlatformContext holding platform connection information.
	 * @param subject Subject representing the authenticated caller.
	 * @return true if the ITIM account password should be changed; false otherwise.
	 */
	public boolean isPasswordChangeRequired(PlatformContext platform, Subject subject)
	{
		boolean isPwdChangeRequired = false;
		try {
			SystemUserMO systemUserMO = SystemSubjectUtil.getSystemUser(platform, subject);
			isPwdChangeRequired = systemUserMO.isChangePasswordRequired();
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out.println(
				"EXPI: SystemSubjectUtil.isPasswordChangeRequired - ApplicationException");
			
		}
		return isPwdChangeRequired;
	}

	/**
	 * Method lookupAccounts
	 * Attempts to find the account (for the specified serviceDN) owned by the
	 * Person object (PersonMO).
	 * @param platformLogin
	 * @param subject
	 * @param personMO
	 * @return Account
	 */
	public AccountMO lookupAccounts(
		PlatformContext platformLogin,
		Subject subject,
		PersonMO personMO,
		String serviceDN) {
		System.out.println("EXPI: lookupAccounts: type " + serviceDN);

		AccountManager acctMgr = new AccountManager(platformLogin, subject);

		// for TESTING only
		Locale locale = Locale.US;

		Collection accountsByOwner = null;

		try {
			System.out.println("EXPI: getAccounts");
			accountsByOwner = acctMgr.getAccounts(personMO, locale);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("EXPI: acctMO.getData - RemoteException");
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out.println("EXPI: acctMO.getData - ApplicationException");
		}

		if (accountsByOwner == null || accountsByOwner.isEmpty()) {
			System.out.println(
				"EXPI: acctMO.getData - no authorized accounts found.");
			return null;
		}

		Iterator it = accountsByOwner.iterator();

		AccountMO acctMO = null;
		Account anAccount = null;
		int iCount = 1;

		System.out.println(
			"EXPI: Person has " + accountsByOwner.size() + " Accounts");

		while (it.hasNext()) {
			acctMO = (AccountMO) it.next();
			System.out.println("EXPI: checking acctMO " + iCount++);

			anAccount = getAccountByService(acctMO, serviceDN);
			if (anAccount != null) {
				System.out.println("Returning AccountMO:");
				this.account = anAccount;
				return acctMO;
			}
		}

		return null;

	}

	/**
	 * Method updateAccountRoles.
	 * Updates the Groups (roles) for the specified Account.
	 * @param acctMO
	 * @param account
	 * @param attrValues
	 * @param colRoles
	 */
	public boolean updateAccount(AccountMO acctMO, Account account) {

		try {
			System.out.println("EXPI: Calling accountMO update: ");
			acctMO.update(account, null);
			System.out.println("EXPI: Update accountMO returned");
		} catch (Exception e) {
			System.out.println("EXPI: accountMO threw an Exception: ");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Method getAccountByService.
	 * @param acctMO
	 * @param serviceDN
	 * @return Account
	 */
	public Account getAccountByService(AccountMO acctMO, String serviceDN) {
		System.out.println(
			"EXPI: getAccountByService - entering: " + serviceDN);
		if (acctMO == null) {
			System.out.println("EXPI: getAccountByService - no acctMO");
			return null;
		}

		Account account = null;

		try {
			account = (Account) acctMO.getData();
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("EXPI: acctMO.getData - RemoteException");
			return null;
		} catch (ApplicationException e) {
			e.printStackTrace();
			System.out.println("EXPI: acctMO.getData - ApplicationException");
			return null;
		}

		System.out.println("EXPI: dn = " + acctMO.getDistinguishedName());

		DistinguishedName dn = account.getServiceDN();

		System.out.println("EXPI: Account: Service DN = " + dn);
		System.out.println("EXPI: Account Name: " + account.getName());

		if (dn.toString().equalsIgnoreCase(serviceDN)) {
			System.out.println("EXPI: Found Group DN");
			printAttributes(account);
			System.out.println("EXPI:returning account ");
			return account;
		} else {
			System.out.println("EXPI:Service DN not found");
		}
		return null;
	}

	/**
	 * Method getTamGroups.
	 * Returns a collection of Roles (Groups) from the supplied Attribute Values.
	 * @param attrValues
	 * @return Collection
	 */
	public Collection<String> getTamGroups(AttributeValues attrValues) {

		AttributeValue attr = attrValues.get(getProperty(APP_SERVICE_ATTR));

		if (attr == null) {
			System.out.println("EXPI: No TAM Group members specified");
			return new Vector<String>(0);
		}

		Collection<String> colTamGroups = attr.getValues();

		System.out.println("EXPI: TAM Group(s): ");
		Iterator itc = colTamGroups.iterator();
		while (itc.hasNext()) {
			System.out.println("EXPI: Group: " + itc.next());
		}

		return colTamGroups;
	}

	/**
	 * Method setRoles.
	 * Adds the collection of Roles to the supplied Attribute Values, checking for any new additions.
	 * @param attrValues
	 * @param roles - collection of roles for this account.
	 * @return boolean - true is returned if modifications were made. false otherwise.
	 */
	public void setRoles(AttributeValues attrValues, Collection roles) {

		System.out.println("EXPI: setRoles (" + ACCOUNT_ATTR_ROLE + ")");
		Iterator iter = roles.iterator();
		while (iter.hasNext()) {
			System.out.println("Group: " + iter.next());
		}

		AttributeValue attrVal = new AttributeValue(ACCOUNT_ATTR_ROLE, roles);
		attrValues.put(attrVal);
	}

	/**
	* Get the OrganizationalContainerMO from the Name.
	* @param PlatformContext
	* @param Subject
	* @param tenantID The tenant Id. (null permissible)
	* @param toOrgName The Org name.
	*/
	public static OrganizationalContainerMO getOrganizationContainerbyName(
		PlatformContext platform,
		Subject subject,
		String tenantId,
		String toOrgName)
		throws RemoteException, ApplicationException {

		System.out.println(
			"getOrganizationContainerbyName: " + tenantId + " " + toOrgName);
		ContainerManager cManager = new ContainerManager(platform, subject);
		OrganizationalContainerMO rootOrgContainer = null;
		if (tenantId == null)
			rootOrgContainer = cManager.getRoot();
		else
			rootOrgContainer = cManager.getRoot(tenantId);

		Collection cColl =
			cManager.getContainers(rootOrgContainer, "o", toOrgName);

		OrganizationalContainerMO toOrgNameCMO = null;
		if (!cColl.isEmpty()) {
			toOrgNameCMO = (OrganizationalContainerMO) cColl.iterator().next();
			System.out.println(
				"getContainers: " + toOrgNameCMO.getDistinguishedName());
		} else
			System.out.println("getContainers: no data");
		return toOrgNameCMO;

	}

	/**
	 * Method getFirstContainer.
	 * @param collection
	 * @return OrganizationalContainerMO
	 * @throws ApplicationException
	 */
	public static OrganizationalContainerMO getFirstContainer(Collection collection)
		throws ApplicationException {

		Iterator it = collection.iterator();
		while (it.hasNext()) {
			OrganizationalContainerMO po =
				(OrganizationalContainerMO) it.next();
			return po;
		}
		throw new ApplicationException("Container to be deleted can't be found");
	}

	/**
	 * Method getLoginContextID.
	 */
	public String getLoginContextID() {
		return getProperty(LOGIN_CONTEXT);
	}

	/**
	 * Method getTenantID
	 */
	public String getTenantID() {
		return getProperty(TENANT_ID);
	}


	/**
	 * Method forward. If an error occurs during the processing of the Form data
	 * in the registerServlet, this method is used to forward the request to another
	 * JSP page along with an error message assigned to the specified attribute.
	 * @param req Request
	 * @param resp Response
	 * @param msgAttr Name of the attribute to assign the message
	 * @param msg The message
	 * @param jspPage name of the JSP page that will handle the forward.
	 */
	public static void forward(
		HttpServletRequest req,
		HttpServletResponse resp,
		String msg,
		String jspPage) {

		System.out.println("EXPI: ExpiUtil.forward() - forward: to " + jspPage);

		if (msg != null)
			System.out.println("EXPI:  ExpiUtil.forward() - forward: Msg = " + msg);

		if (msg != null) {
			HttpSession session = req.getSession(false);

			if (session != null) {
				System.out.println("EXPI:  ExpiUtil.forward() - Msg added to session");
				session.setAttribute(ExpiUtil.ERR_LABEL, msg);
			}
		} else {
			HttpSession session = req.getSession(false);
			if (session != null)
				session.setAttribute(ExpiUtil.ERR_LABEL, "");
		}

		RequestDispatcher rd = req.getRequestDispatcher("/" + jspPage);

		try {
			rd.forward(req, resp);
		} catch (IOException e) {
			System.err.println(  
				"ExpiUtil.forward() - IO exception on RequestDispatcher.forward because : " + e);
		} catch (ServletException e) {
			System.err.println(
				"ExpiUtil.forward() - Servlet exception on RequestDispatcher.forward because: " + e);
		}
	}

	/**
	 * Method printAttributes.
	 * Debug helper routine that outputs the attributes and values
	 * respective to the supplied DirectoryObject
	 * @param valueObject - object that contians the AttributeValues
	 */
	public void printAttributes(DirectoryObject valueObject) {

		if (valueObject == null) {
			System.out.println("printAttributes: null object specified");
			return;
		}

		System.out.println("\n---------------------------------------------");
		if (valueObject.getDistinguishedName() != null) {
			System.out.println(
				"DN: " + valueObject.getDistinguishedName().toString());
		}

		Map attributes = valueObject.getRawAttributes().getMap();
		Set keys = attributes.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			AttributeValue valu = (AttributeValue) attributes.get(name);
			System.out.println("   " + name + " = " + valu.getValueString());
		}
	}

	/**
	 * Method isProperties.
	 * @return boolean - true if the properties are loaded
	 */
	public boolean isProperties() {
		return (properties != null && !properties.isEmpty());
	}

	/**
	 * Method isSSOEnabled.
	 *
	 * @return boolean - true if the ssoEnabled=true in the property file
	 */
	public boolean isSSOEnabled() {
		String sSSOEnabled = getProperty(SSO_ENABLED);

		return (sSSOEnabled != null && sSSOEnabled.equalsIgnoreCase("true"));
	}

	/**
	 * Method setTAMService.
	 * Following the detection of the TAM service, call this routine to
	 * set the appropriate state of whether TAM Service is installed or not
	 * in ITIM.
	 *
	 * @param bFlag - true - indicates TAM Service is available, false otherwise
	 */
	public void setTAMService( boolean bFlag) {
		bTAMInstalled = bFlag;
	}

	/**
	 * Method isTAMService.
	 * Returns the state indicating if a TAM Service is installed or not.
	 * @return boolean - true if TAM Service was found, false otherwise
	 */
	public boolean isTAMService() {
		return bTAMInstalled ;
	}
	
	/**
	 * Logs the message to the SystemOut.
	 * @param caller The caller information in the format of class.method(). 
	 * @param msg The messsage to be logged.
	 */
	public static void log(Object msg) {
		System.out.println("EXPI:" + msg.toString());
	}
}
