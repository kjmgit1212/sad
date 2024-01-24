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

package examples.serviceprovider.rdbms;

import com.ibm.itim.remoteservices.exception.RemoteServicesException;

/**
 * Encapsulates an exception generated because the SQL statement entered in the
 * service form.
 */
class RDBMSSQLException extends RemoteServicesException {

	private static final long serialVersionUID = -3065700843981082060L;

	/**
	 * Create a new RDBMSSQLException object.
	 * 
	 * @param message
	 *            the message to propogate up the stack
	 */
	RDBMSSQLException(String message) {
		super(message);
	}

	/**
	 * Create a new RDBMSSQLException object.
	 * 
	 * @param message
	 *            the message to propogate up the stack
	 * @param throwable
	 *            original exception
	 */
	RDBMSSQLException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
