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
 * Class is part of the event notification api example. It encapsulates a
 * request message from the tester containing the information to look up a
 * service.
 */
public final class ServiceLookupEvent extends TestEvent {

	private final String serviceFilter;

	private final String tenant;

	private final String principal;

	private final char[] password;

	/**
	 * Construct a new TestEvent.
	 * 
	 * @param serviceFilter
	 *            The filter identifying the service
	 * @param tenant
	 *            The tenant for the TIM installation
	 * @param principal
	 *            The principal to authenticate to the server as
	 * @param password
	 *            The plain text password to authenticate to the server with
	 */
	public ServiceLookupEvent(String serviceFilter, String tenant,
			String principal, char[] password) {
		super(GET_SERVICE_OPERATION);
		this.serviceFilter = serviceFilter;
		this.tenant = tenant;
		this.principal = principal;
		this.password = password;
		System.out.println("[ServiceLookupEvent.<init>] "
				+ new String(password));
	}

	/**
	 * Gets the password to authenticate to the server with
	 * 
	 * @return the plain text password
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * Gets the principal to authenticate to the server as
	 * 
	 * @return the principal to authenticate to the server as
	 */
	public String getPrincipal() {
		return principal;
	}

	/**
	 * Gets the filter identifying the service
	 * 
	 * @return the service filter
	 */
	public String getServiceFilter() {
		return serviceFilter;
	}

	/**
	 * Gets the tenant for the TIM installation
	 * 
	 * @return the tenant for the TIM installation
	 */
	public String getTenant() {
		return tenant;
	}

	/**
	 * For use in log statements.
	 * 
	 * @return Lists the values for the event attributes.
	 */
	public String toString() {
		return "<ServiceLookupEvent> operation: " + getOperationCode()
				+ ", tenant: " + tenant + ", serviceFilter: " + serviceFilter
				+ ", principal: " + principal;
	}

}
