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
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchResult;
import com.ibm.itim.remoteservices.provider.SearchResults;

/**
 * In case an error occurred executing a select statement for a reconciliation,
 * this class encapsulates an empty set of results and the error that led to it.
 */
class NoSearchResults implements SearchResults {

	private final RequestStatus status = new RequestStatus(
			RequestStatus.UNSUCCESSFUL);

	private final Exception e;

	NoSearchResults(Exception e) {
		this.e = e;
		status.setReasonMessage(e.getMessage());
	}

	/**
	 * Always unsuccessful. Encapsulates the original error.
	 * 
	 * @return Encapsulates the original error.
	 */
	public RequestStatus getRequestStatus() {
		return status;
	}

	/**
	 * Always returns throws a RemoteServicesException
	 * 
	 * @return never returns a result
	 * @throws RemoteServicesException
	 *             Encapsulates the original error.
	 */
	public boolean hasNext() throws RemoteServicesException {
		throw new RemoteServicesException(e.getMessage(), e);
	}

	/**
	 * Always returns throws a RemoteServicesException
	 * 
	 * @return never returns a result
	 * @throws RemoteServicesException
	 *             Encapsulates the original error.
	 */
	public SearchResult next() throws RemoteServicesException {
		throw new RemoteServicesException(e.getMessage(), e);
	}

	/**
	 * Always returns throws a RemoteServicesException
	 * 
	 * @throws RemoteServicesException
	 *             Encapsulates the original error.
	 */
	public void close() throws RemoteServicesException {
		throw new RemoteServicesException(e.getMessage(), e);
	}

}
