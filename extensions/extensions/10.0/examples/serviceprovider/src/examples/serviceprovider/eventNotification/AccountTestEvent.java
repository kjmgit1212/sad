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
 * request message from the tester containing the test information for account
 * processing.
 */
public final class AccountTestEvent extends TestEvent {

	private final String userID, objectClass;

	/**
	 * Construct a new AccountTestEvent. This form of the constructor is ok for
	 * modify and delete because the object class is not required.
	 * 
	 * @param operationCode
	 *            one of ADD_OPERATION, MODIFY_OPERATION, DELETE_OPERATION, or
	 *            UNKNOWN_OPERATION
	 * @param userID
	 *            The user id for the account
	 */
	public AccountTestEvent(int operationCode, String userID) {
		super(operationCode);
		this.userID = userID;
		this.objectClass = null;
	}

	/**
	 * Construct a new AccountTestEvent. This form of the constructor should be
	 * used for because the object class is required.
	 * 
	 * @param operationCode
	 *            one of ADD_OPERATION, MODIFY_OPERATION, DELETE_OPERATION, or
	 *            UNKNOWN_OPERATION
	 * @param userID
	 *            The user id for the account
	 * @param objectClass
	 *            The objectClass for the account
	 */
	public AccountTestEvent(int operationCode, String userID, String objectClass) {
		super(operationCode);
		this.userID = userID;
		this.objectClass = objectClass;
	}

	/**
	 * The user id of the account.
	 * 
	 * @return The user id of the account.
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * The user id of the account.
	 * 
	 * @return The user id of the account.
	 */
	public String getObjectClass() {
		return objectClass;
	}

	/**
	 * For use in log statements.
	 * 
	 * @return Lists the values for the operation, user id, objectclass.
	 */
	public String toString() {
		return "<AccountTestEvent> operation: " + getOperationCode()
				+ ", userID: " + userID + ", objectClass: " + objectClass;
	}

}
