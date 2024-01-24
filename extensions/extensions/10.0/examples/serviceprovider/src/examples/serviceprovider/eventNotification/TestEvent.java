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

/**
 * Interface is part of the event notification api example. It encapsulates a
 * request message from the tester containing the desired test information.
 */
public class TestEvent {

	/** Indicates that the type of the operation couldn't be determined */
	public static final int UNKNOWN_OPERATION = 0;

	/** Indicates that the type of the operation was an add */
	public static final int ADD_OPERATION = 1;

	/** Indicates that the type of the operation was a modify */
	public static final int MODIFY_OPERATION = 2;

	/** Indicates that the type of the operation was a delete */
	public static final int DELETE_OPERATION = 3;

	/** Indicates that the type of the operation was a service lookup */
	public static final int GET_SERVICE_OPERATION = 4;

	/** Indicates that the type of the operation was to start test again */
	public static final int START_AGAIN = 5;

	private final int operationCode;

	/**
	 * Construct a new TestEvent with an unknown state.
	 */
	public TestEvent() {
		this.operationCode = UNKNOWN_OPERATION;
	}

	/**
	 * Construct a new TestEvent.
	 * 
	 * @param operationCode
	 *            one of ADD_OPERATION, MODIFY_OPERATION, DELETE_OPERATION, or
	 *            UNKNOWN_OPERATION
	 * @param userID
	 *            The user id for the account
	 */
	public TestEvent(int operationCode) {
		this.operationCode = operationCode;
	}

	/**
	 * Gets the type of the operation
	 * 
	 * @return one of ADD_OPERATION, MODIFY_OPERATION, DELETE_OPERATION,
	 *         GET_SERVICE_OPERATION, or UNKNOWN_OPERATION
	 */
	int getOperationCode() {
		return operationCode;
	}

	/**
	 * For use in log statements.
	 * 
	 * @return Lists the values for the operation, user id, objectclass.
	 */
	public String toString() {
		return "<TestEvent> operation: " + operationCode;
	}

}
