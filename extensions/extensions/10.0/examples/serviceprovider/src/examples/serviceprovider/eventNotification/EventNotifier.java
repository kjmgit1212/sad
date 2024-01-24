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

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.itim.dataservices.model.domain.Service;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.InitialPlatformContext;

/**
 * Example class to illustrate use of event notification API. The example will
 * look up a service and use the event notification API to notify TIM of account
 * changes. This class listens for HTTP events and dispatches them to delegates
 * classes to do the work of invoking the notification API methods.
 * 
 * @see AccountEventListener
 */
public final class EventNotifier {

	private static final String SERVICE_SESSION_KEY = "service";

	/** Key to look up state in the session. */
	private final static String STATE_SESSION_KEY = "state";

	/** Key to lookup ServiceLookupEvent in http session */
	private final static String SERVICE_EVENT_SESSION_KEY = "ServiceLookupEvent";

	/** Key to lookup password in http session */
	// XXX private final static String PASSWORD_SESSION_KEY = "password";
	/**
	 * The url of the start page.
	 */
	private final static String START_PAGE = "eventtest1of3.jsp";

	/**
	 * The url of the second page.
	 */
	private final static String PAGE2 = "eventtest2of3.jsp";

	/**
	 * The url of the third page.
	 */
	private final static String PAGE3 = "eventtest3of3.jsp";

	/**
	 * The url of the event test error page.
	 */
	private final static String ERROR_PAGE = "eventtesterror.jsp";

	/**
	 * Attribute lookup key for error page.
	 */
	private final static String ERROR_KEY = "error";

	/**
	 * HTML form field name for operation type.
	 */
	private final static String OPERATION = "operation";

	/**
	 * HTML form field value for get service operation type.
	 */
	private final static String GET_SERVICE_OPERATION = "get_service";

	/**
	 * HTML form field name for tenant.
	 */
	private final static String TENANT = "tenant";

	/**
	 * HTML form field example value for tenant.
	 */
	private final static String EXAMPLE_TENANT = "ups";

	/**
	 * HTML form field name for service filter.
	 */
	private final static String SERVICE_FILTER = "serviceFilter";

	/**
	 * HTML form field example value for service filter.
	 */
	private final static String EXAMPLE_SERVICE_FILTER = "(erServiceName=JNDI)";

	/**
	 * HTML form field name for principal to authenticate as.
	 */
	private final static String PRINCIPAL = "principal";

	/**
	 * HTML form field name for principal to authenticate as.
	 */
	private final static String EXAMPLE_PRINCIPAL = "agent";

	/**
	 * HTML form field name for password to authenticate with.
	 */
	private final static String PASSWORD = "password";

	/**
	 * HTML form field example value for password to authenticate with.
	 */
	private final static String EXAMPLE_PASSWORD = "agent";

	/**
	 * HTML form field value for account add operation.
	 */
	private final static String ADD = "add";

	/**
	 * HTML form field name for account user id.
	 */
	private final static String USERID = "userid";

	/**
	 * HTML form field name for account object class.
	 */
	private final static String OBJECTCLASS = "objectclass";

	/**
	 * HTML form field value for account modify operation.
	 */
	private final static String MODIFY = "modify";

	/**
	 * HTML form field value for account delete operation.
	 */
	private final static String DELETE = "delete";

	/**
	 * Entry point for processing of servlet form submission.
	 * 
	 * @param request
	 *            The servlet request
	 * @param response
	 *            The servlet response
	 * @throws IOException
	 *             if there is an application exception
	 */
	public String service(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String nextPage = START_PAGE;

		// state of previous screen
		NavigationState state = getNavigationState(request);
		System.out.println("[EventNotifier.service] previous state: " + state);
		TestEvent testEvent = parseRequest(request);
		// move state to next screen based on the event found in the request
		state.next(testEvent);
		System.out.println("[EventNotifier.service] next state: " + state);
		try {
			HttpSession session = request.getSession(true);

			switch (state.getState()) {
			case NavigationState.NOT_STARTED: {
				nextPage = START_PAGE;
				break;
			}
			case NavigationState.SELECT_ACCOUNT_OPER: {
				AccountEventListener listener = new AccountEventListener();
				Service service = listener
						.testLookupService((ServiceLookupEvent) testEvent);
				session.setAttribute(SERVICE_EVENT_SESSION_KEY, testEvent);
				session.setAttribute(SERVICE_SESSION_KEY, service);
				nextPage = PAGE2;
				break;
			}
			case NavigationState.DISPLAY_STATUS: {
				Service service = (Service) session
						.getAttribute(SERVICE_SESSION_KEY);
				ServiceLookupEvent serviceLookupEvent = (ServiceLookupEvent) session
						.getAttribute(SERVICE_EVENT_SESSION_KEY);
				AccountEventListener listener = new AccountEventListener();
				InitialPlatformContext context = listener
						.getInitialPlatformContext(serviceLookupEvent);

				listener.processTestEvent(testEvent, context, service, request,
						response);
				nextPage = PAGE3;
				break;
			}
			default: {
				nextPage = ERROR_PAGE;
				break;
			}
			}
		} catch (LoginException e) {
			e.printStackTrace();
			state.value = NavigationState.NOT_STARTED;
			request.setAttribute(ERROR_KEY, e);
			nextPage = ERROR_PAGE;
		} catch (RemoteServicesException e) {
			e.printStackTrace();
			state.value = NavigationState.NOT_STARTED;
			request.setAttribute(ERROR_KEY, e);
			nextPage = ERROR_PAGE;
		}
		return nextPage;
	}

	/**
	 * Gets the navigate state that the user is in.
	 * 
	 * @param request
	 * @return
	 */
	private NavigationState getNavigationState(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		NavigationState state = (NavigationState) session
				.getAttribute(STATE_SESSION_KEY);
		if (state == null) {
			state = new NavigationState();
			session.setAttribute(STATE_SESSION_KEY, state);
		}
		return state;
	}

	/**
	 * Parses the data sent from the HTML form and creates a test event object.
	 * 
	 * @param request
	 *            the servlet request
	 * @return an object encapsulated the data posted to this url
	 */
	private TestEvent parseRequest(HttpServletRequest request) {

		String operation = request.getParameter(OPERATION);
		System.out.println("[EventNotifier.parseRequest] operation: "
				+ operation);

		TestEvent testEvent = null;
		if (operation == null) {
			testEvent = new TestEvent();
		} else if (operation.equals(GET_SERVICE_OPERATION)) {
			String tenant = request.getParameter(TENANT);
			if (tenant == null) {
				tenant = EXAMPLE_TENANT;
			}
			String serviceFilter = request.getParameter(SERVICE_FILTER);
			if (serviceFilter == null) {
				serviceFilter = EXAMPLE_SERVICE_FILTER;
			}

			String principal = request.getParameter(PRINCIPAL);
			if (principal == null) {
				principal = EXAMPLE_PRINCIPAL;
			}

			String passwordStr = request.getParameter(PASSWORD);
			if (passwordStr == null) {
				passwordStr = EXAMPLE_PASSWORD;
			}

			testEvent = new ServiceLookupEvent(serviceFilter, tenant,
					principal, passwordStr.toCharArray());

		} else if (operation.equals(ADD)) {
			String userID = request.getParameter(USERID);
			String objectClass = request.getParameter(OBJECTCLASS);
			testEvent = new AccountTestEvent(TestEvent.ADD_OPERATION, userID,
					objectClass);
		} else if (operation.equals(MODIFY)) {
			String userID = request.getParameter(USERID);
			testEvent = new AccountTestEvent(TestEvent.MODIFY_OPERATION, userID);
		} else if (operation.equals(DELETE)) {
			String userID = request.getParameter(USERID);
			testEvent = new AccountTestEvent(TestEvent.DELETE_OPERATION, userID);
		} else {
			testEvent = new TestEvent();
		}
		System.out.println("[EventNotifier.parseRequest] testEvent: "
				+ testEvent);
		return testEvent;
	}

	/**
	 * Object to track the user's state in navigating between screens. These are
	 * the navigation rules:
	 * 
	 * <ul>
	 * <li>event = null -> NOT_STARTED</li>
	 * <li>state = DISPLAY_STATUS and event = START_AGAIN -> NOT_STARTED</li>
	 * <li>state = NOT_STARTED and event = UNKNOWN_OPERATION -> NOT_STARTED</li>
	 * <li>state = NOT_STARTED and event = GET_SERVICE_OPERATION ->
	 * SELECT_ACCOUNT_OPER</li>
	 * <li>state = SELECT_ACCOUNT_OPER and event = ADD_OPERATION ->
	 * DISPLAY_STATUS</li>
	 * <li>state = SELECT_ACCOUNT_OPER and event = MODIFY_OPERATION ->
	 * DISPLAY_STATUS</li>
	 * <li>state = SELECT_ACCOUNT_OPER and event = DELETE_OPERATION ->
	 * DISPLAY_STATUS</li>
	 * <li>state = DISPLAY_STATUS and event = UNKNOWN_OPERATION ->
	 * DISPLAY_ERROR</li>
	 * <li>state = SELECT_ACCOUNT_OPER and event = UNKNOWN_OPERATION ->
	 * DISPLAY_ERROR</li>
	 * </ul>
	 */
	static class NavigationState {

		/** The user has not yet started or is starting fresh. */
		final static int NOT_STARTED = 101;

		/** The user is ready to select an account operation. */
		final static int SELECT_ACCOUNT_OPER = 102;

		/** The user has completed navigation through the screens. */
		final static int DISPLAY_STATUS = 103;

		/** An error has occurred. */
		final static int DISPLAY_ERROR = 104;

		private int value;

		/**
		 * Construct a new NavigationState object in the NOT_STARTED state.
		 */
		NavigationState() {
			this.value = NOT_STARTED;
		}

		/**
		 * Given the present state and the event received go to the next state.
		 * 
		 * @param testEvent
		 *            the event generated by the user.
		 * @return the next state the user should go to
		 */
		void next(TestEvent testEvent) {
			System.out.println("[NavigationState.next] value: " + toString());
			if (testEvent == null) {
				value = NOT_STARTED;
			} else {
				switch (testEvent.getOperationCode()) {
				case TestEvent.GET_SERVICE_OPERATION: {
					if (value == NOT_STARTED) {
						value = SELECT_ACCOUNT_OPER;
					} else {
						value = DISPLAY_STATUS;
					}
					break;
				}
				case TestEvent.ADD_OPERATION:
				case TestEvent.MODIFY_OPERATION:
				case TestEvent.DELETE_OPERATION: {
					value = DISPLAY_STATUS;
					break;
				}
				case TestEvent.START_AGAIN: {
					value = NOT_STARTED;
					break;
				}
				case TestEvent.UNKNOWN_OPERATION: {
					if (value == NOT_STARTED) {
						value = NOT_STARTED;
					} else {
						value = DISPLAY_ERROR;
					}
					value = NOT_STARTED;
					break;
				}
				default: {
					value = DISPLAY_STATUS;
					break;
				}
				}
			}
		}

		/**
		 * Get the current state in the user's navigation.
		 * 
		 * @return one of NOT_STARTED, SERVICE_SELECTED, or COMPLETED.
		 */
		int getState() {
			return value;
		}

		/**
		 * For use in log statements.
		 * 
		 * @return string abreviation of the state.
		 */
		public String toString() {
			String description = null;
			switch (value) {
			case NOT_STARTED: {
				description = "NOT_STARTED";
				break;
			}
			case SELECT_ACCOUNT_OPER: {
				description = "SELECT_ACCOUNT_OPER";
				break;
			}
			case DISPLAY_STATUS: {
				description = "DISPLAY_STATUS";
				break;
			}
			case DISPLAY_ERROR: {
				description = "DISPLAY_ERROR";
				break;
			}
			default: {
				description = "DISPLAY_ERROR";
				break;
			}
			}
			return description;
		}
	}

}
