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

package examples.serviceprovider.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.itim.common.AttributeChangeIterator;
import com.ibm.itim.common.AttributeChangeOperation;
import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchCriteria;
import com.ibm.itim.remoteservices.provider.SearchResult;
import com.ibm.itim.remoteservices.provider.SearchResults;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;
import com.ibm.itim.remoteservices.provider.ServiceProviderV2;

/**
 * A minimal service provider implementation that writes to a flat file. May be
 * useful for testing policy or performance or experimenting with the API set.
 * 
 * Since each FileServiceProvider instance may be called concurrently and will
 * be accessing the same file, the methods are synchronized.
 */
public class FileServiceProvider implements ServiceProviderV2 {

	private final BufferedWriter writer;

	private final ServiceProviderInformation serviceProviderInfo;

	/**
	 * The FileServiceProviderFactory should use this constructor to create a
	 * FileServiceProvider object.
	 */
	FileServiceProvider(BufferedWriter writer,
			ServiceProviderInformation serviceProviderInfo) {
		this.writer = writer;
		this.serviceProviderInfo = serviceProviderInfo;
	}

	/**
	 * Logs an adds operation to the log file a user to the file
	 */
	public synchronized RequestStatus add(String objectClass,
			AttributeValues attributeValues, String requestID) {

		try {
			writer.write("add; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(attributeValues.get("eruid").getValueString());
			AttributeValue groupAV = attributeValues
					.get("erFileGroupMembership");
			if (groupAV != null) {
				writer.write("; ");
				Collection groups = groupAV.getValues();
				Iterator it = groups.iterator();
				while (it.hasNext()) {
					writer.write(it.next().toString());
					if (it.hasNext()) {
						writer.write(", ");
					}
				}
			}
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "add request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Logs a changePassword operation to the log file a user to the file
	 */
	public synchronized RequestStatus changePassword(String entityDN,
			byte[] newPassword, String requestID) {
		try {
			writer.write("changePassword; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(entityDN);
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "changePassword request.  Detailed message: "
					+ e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Logs a delete operation to the log file a user to the file
	 */
	public synchronized RequestStatus delete(String entityDN, String requestID) {
		try {
			writer.write("delete; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(entityDN);
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "delete request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Gets the ServiceProviderInfo for this type of ServiceProvider
	 * 
	 * @return ServiceProviderInformation Encapsulating the service provider
	 *         parameters
	 */
	public synchronized ServiceProviderInformation getServiceProviderInfo() {
		return serviceProviderInfo;
	}

	/**
	 * Logs a modify operation to the log file a user to the file
	 */
	public synchronized RequestStatus modify(String entityDN,
			AttributeChanges attributeChanges, String requestID) {
		try {
			writer.write("modify; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(entityDN);
			writer.write("; ");
			AttributeChangeIterator it = attributeChanges.iterator();
			while (it.hasNext()) {
				AttributeChangeOperation aco = it.next();
				switch (aco.getModificationAction()) {
				case AttributeChangeOperation.ADD_ACTION: {
					writer.write("add_action ");
					break;
				}
				case AttributeChangeOperation.REMOVE_ACTION: {
					writer.write("remove_action ");
					break;
				}
				case AttributeChangeOperation.REPLACE_ACTION: {
					writer.write("replace_action ");
					break;
				}
				}

				Collection changeData = aco.getChangeData();
				Iterator changeIt = changeData.iterator();
				while (changeIt.hasNext()) {
					AttributeValue av = (AttributeValue) changeIt.next();
					writer.write(av.getName());
					writer.write(" ");
					writer.write(av.getValues().toString());
				}
			}
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "modify request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Logs a suspend operation to the log file a user to the file
	 */
	public synchronized RequestStatus suspend(String entityDN, String requestID) {
		try {
			writer.write("suspend; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(entityDN);
			writer.flush();
			writer.newLine();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "modify request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Logs a restore operation to the log file a user to the file
	 */
	public synchronized RequestStatus restore(String entityDN,
			byte[] newPassword, String requestID) {
		try {
			writer.write("restore; ");
			writer.write(requestID);
			writer.write("; ");
			writer.write(entityDN);
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "restore request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			return new RequestStatus(RequestStatus.UNSUCCESSFUL, e.getMessage());
		}
	}

	/**
	 * Empty result set returned
	 */
	public synchronized SearchResults search(SearchCriteria searchCriteria,
			String requestID) {
		try {
			writer.write("search");
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "search request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
		}
		return new FileSearchResults(new RequestStatus(
				RequestStatus.UNSUCCESSFUL, "Not implemented"));
	}

	/**
	 * If the server got to here and we can write a test message to the file we
	 * assume that the FileServiceProvider is working OK.
	 */
	public synchronized boolean test() throws RemoteServicesException {
		return test2().succeeded();
	}

	public synchronized RequestStatus test2() throws RemoteServicesException {
		System.out.println(getClass().getName() + ": test");
		try {
			writer.write("test");
			writer.newLine();
			writer.flush();
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (IOException e) {
			System.err.println(getClass().getName() + ": Unable to process "
					+ "test request.  Detailed message: " + e.getMessage());
			e.printStackTrace();
			throw new RemoteServicesException(e.getMessage(), e);
		}
	}

	/**
	 * An empty set of search results
	 */
	private static class FileSearchResults implements SearchResults {

		private final RequestStatus status;

		FileSearchResults(RequestStatus status) {
			this.status = status;
		}

		/**
		 * Returns the status of the search operation.
		 */
		public RequestStatus getRequestStatus() {
			return status;
		}

		/**
		 * Empty
		 */
		public boolean hasNext() throws RemoteServicesException {
			return false;
		}

		/**
		 * not implemented
		 */
		public SearchResult next() throws RemoteServicesException {
			return null;
		}

		/**
		 * not implemented
		 */
		public void close() throws RemoteServicesException {
		}

	}

}
