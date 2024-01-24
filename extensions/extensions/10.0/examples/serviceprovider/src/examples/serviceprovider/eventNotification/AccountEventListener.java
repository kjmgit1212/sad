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

package examples.serviceprovider.eventNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.itim.common.AttributeChangeOperation;
import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.InitialPlatformContext;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.ServiceCallbackHandler;
import com.ibm.itim.remoteservices.provider.UnsolicitedEventProcessor;

/**
 * Part of example to illustrate use of event notification API. This class
 * listens for account add, modify, and delete test events.
 */
public class AccountEventListener {

	private static final String STATUS_KEY = "status";

	/**
	 * Processes a test event based on a http request from the tester.
	 * 
	 * @param testEvent
	 *            an object encapsulating the test event data
	 * @param context
	 *            obtained after authentication to the server
	 * @param request
	 *            the http request object
	 * @param response
	 *            the http response object
	 * @throws LoginException
	 *             if the program could not be authenticated
	 * @throws RemoteServicesException
	 *             during the context lookup process
	 * @throws IOException
	 *             writing to output stream
	 */
	public void processTestEvent(TestEvent testEvent,
			InitialPlatformContext context, Service service,
			HttpServletRequest request, HttpServletResponse response)
			throws LoginException, RemoteServicesException, IOException {
		// XXX HttpSession session = request.getSession(true);
		switch (testEvent.getOperationCode()) {
		case TestEvent.ADD_OPERATION: {
			RequestStatus status = testAdd(context, service,
					(AccountTestEvent) testEvent);
			request.setAttribute(STATUS_KEY, status);
			break;
		}
		case TestEvent.MODIFY_OPERATION: {
			RequestStatus status = testModify(context, service,
					(AccountTestEvent) testEvent);
			request.setAttribute(STATUS_KEY, status);
			break;
		}
		case TestEvent.DELETE_OPERATION: {
			RequestStatus status = testDelete(context, service,
					(AccountTestEvent) testEvent);
			request.setAttribute(STATUS_KEY, status);
			break;
		}
		default: {
			break;
		}
		}
	}

	/**
	 * Test looking up a service via the event notification mechanism.
	 * 
	 * @param testEvent
	 *            encapsulates the data entered by the user
	 * @return The service found
	 * @throws LoginException
	 *             if the program could not be authenticated
	 * @throws RemoteServicesException
	 *             during the context lookup process
	 */
	public Service testLookupService(ServiceLookupEvent testEvent)
			throws LoginException, RemoteServicesException {
		InitialPlatformContext context = getInitialPlatformContext(testEvent);
		UnsolicitedEventProcessor processor = context
				.getUnsolicitedEventProcessor();
		return processor.findService(testEvent.getServiceFilter());
	}

	/**
	 * Logs into the application and gets the InitialPlatformContext object
	 * 
	 * @param testEvent
	 *            encapsulates the data entered by the user
	 * @return
	 * @throws LoginException
	 * @throws RemoteServicesException
	 */
	public InitialPlatformContext getInitialPlatformContext(
			ServiceLookupEvent testEvent) throws LoginException,
			RemoteServicesException {

		System.out.println("[AccountEventListener.getInitialPlatformContext] "
				+ "tenant: " + testEvent.getTenant() + ", principal: "
				+ testEvent.getPrincipal() + ", password: "
				+ new String(testEvent.getPassword()) + ", filter: "
				+ testEvent.getServiceFilter());
		// make a copy of the password because it will be cleared during
		// the login process
		char[] password = new char[testEvent.getPassword().length];
		System.arraycopy(testEvent.getPassword(), 0, password, 0, testEvent
				.getPassword().length);
		ServiceCallbackHandler handler = new ServiceCallbackHandler(testEvent
				.getTenant(), testEvent.getPrincipal(), password, testEvent
				.getServiceFilter());
		LoginContext loginContext = new LoginContext(
				ServiceCallbackHandler.SERVICE_LOGIN_CONTEXT, handler);
		loginContext.login();
		System.out.println("[AccountEventListener.getInitialPlatformContext] "
				+ "logged in");
		Subject subject = loginContext.getSubject();
		return new InitialPlatformContext(subject);
	}

	/**
	 * Test adding an account via the event notification mechanism.
	 * 
	 * @param service
	 *            The service the account is to be added to
	 * @param serviceLookupEvent
	 *            service lookup data entered by the user
	 * @param testEvent
	 *            encapsulates the data entered by the user
	 * @return The status of the request
	 * @throws LoginException
	 *             if the program could not be authenticated
	 * @throws RemoteServicesException
	 *             during the context lookup process
	 */
	private RequestStatus testAdd(InitialPlatformContext context,
			Service service, AccountTestEvent testEvent) throws LoginException,
			RemoteServicesException {

		System.out.println("[AccountEventListener.testAdd] context = "
				+ context);
		UnsolicitedEventProcessor processor = context
				.getUnsolicitedEventProcessor();
		String entityDN = Account.ACCOUNT_ATTR_USERID + '='
				+ testEvent.getUserID();
		Collection<String> objectClasses = new ArrayList<String>();
		objectClasses.add(testEvent.getObjectClass());
		AttributeValues attributes = new AttributeValues();
		AttributeValue uid = new AttributeValue(Account.ACCOUNT_ATTR_USERID,
				testEvent.getUserID());
		attributes.put(uid);
		System.out
				.println("[AccountEventListener.testAdd] testing processAddRequest");
		return processor.processAddRequest(service, entityDN, objectClasses,
				attributes);
	}

	/**
	 * Test deleting an account via the event notification mechanism.
	 * 
	 * @param service
	 *            The service the account is to be added to
	 * @param serviceLookupEvent
	 *            service lookup data entered by the user
	 * @param testEvent
	 *            encapsulates the data entered by the user
	 * @return The status of the request
	 * @throws LoginException
	 *             if the program could not be authenticated
	 * @throws RemoteServicesException
	 *             during the context lookup process
	 */
	private RequestStatus testDelete(InitialPlatformContext context,
			Service service, AccountTestEvent testEvent) throws LoginException,
			RemoteServicesException {

		UnsolicitedEventProcessor processor = context
				.getUnsolicitedEventProcessor();
		String entityDN = Account.ACCOUNT_ATTR_USERID + '='
				+ testEvent.getUserID();
		Collection<AttributeChangeOperation> data = new ArrayList<AttributeChangeOperation>();
		AttributeValue status = new AttributeValue(Account.ACCOUNT_ATTR_STATUS,
				Account.INACTIVE_STATUS);
		Collection<AttributeValue> changeData = new ArrayList<AttributeValue>();
		changeData.add(status);
		AttributeChangeOperation change = new AttributeChangeOperation();
		changeData.add(status);
		change.setModificationAction(AttributeChangeOperation.REPLACE_ACTION);
		data.add(change);
		// XXX AttributeChanges changes = new AttributeChanges(data);
		return processor.processDeleteRequest(service, entityDN);
	}

	/**
	 * Test modifying an account via the event notification mechanism.
	 * 
	 * @param service
	 *            The service the account is to be added to
	 * @param serviceLookupEvent
	 *            service lookup data entered by the user
	 * @param testEvent
	 *            encapsulates the data entered by the user
	 * @return The status of the request
	 * @throws LoginException
	 *             if the program could not be authenticated
	 * @throws RemoteServicesException
	 *             during the context lookup process
	 */
	private RequestStatus testModify(InitialPlatformContext context,
			Service service, AccountTestEvent testEvent) throws LoginException,
			RemoteServicesException {
		UnsolicitedEventProcessor processor = context
				.getUnsolicitedEventProcessor();
		String entityDN = Account.ACCOUNT_ATTR_USERID + '='
				+ testEvent.getUserID();
		Collection<AttributeChangeOperation> data = new ArrayList<AttributeChangeOperation>();
		AttributeValue status = new AttributeValue(Account.ACCOUNT_ATTR_STATUS,
				Account.INACTIVE_STATUS);
		Collection<AttributeValue> changeData = new ArrayList<AttributeValue>();
		changeData.add(status);
		AttributeChangeOperation change = new AttributeChangeOperation();
		changeData.add(status);
		change.setModificationAction(AttributeChangeOperation.REPLACE_ACTION);
		change.setChangeData(changeData);
		data.add(change);
		AttributeChanges changes = new AttributeChanges(data);
		return processor.processModifyRequest(service, entityDN, changes);
	}

}
