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

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.dataservices.model.DistinguishedName;
import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.domain.Account;
import com.ibm.itim.dataservices.model.domain.AccountEntity;
import com.ibm.itim.dataservices.model.domain.AccountSearch;
import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchCriteria;
import com.ibm.itim.remoteservices.provider.SearchResults;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;
import com.ibm.itim.remoteservices.provider.ServiceProviderV2;

/**
 * The main class demonstrating connection from TIM to a database for management
 * of accounts. TIM will call methods add, changePassword, delete, modify,
 * restore, suspend, search, and test when driven by user actions within TIM.
 * 
 * It uses the application server connection pool mechanism to connect to the
 * database. The DataSource object is looked up based on the JNDI name in the
 * service definition.
 * 
 * The ServiceProvider implementation does not make assumptions about the
 * account schema, except that there is only one type of supporting data
 * allowed. This is referred to as group data or supporting data, however, it
 * may correspond to role or some other type on the remote database. In
 * addition, it does make assumptions about the service schema, in particular
 * the presence of these attributes:
 * <ul>
 * <li>erRDBMSDataSource - the data source name</li>
 * <li>erRDBMSAddSQL - an SQL statement to add an account</li>
 * <li>erRDBMSChangePasswordSQL - an SQL statement to change passwords</li>
 * <li>erRDBMSDeleteSQL - an SQL statement to delete an account</li>
 * <li>erRDBMSModifySQL - an SQL statement to modify an account</li>
 * <li>erRDBMSRestoreSQL - an SQL statement to restore an account</li>
 * <li>erRDBMSSearchSQL - an SQL statement to search for account data</li>
 * <li>erRDBMSSearchGroupSQL - an SQL statement to search for group data</li>
 * <li>erRDBMSSuspendSQL - an SQL statement to suspend an account</li>
 * <li>erRDBMSAttributeMap - used to map account and group data field names in
 * the database to names used within TIM</li>
 * <li>erRDBMSGroupDNAttr - The field in the group table that should be used to
 * identify the group</li>
 * </ul>
 * 
 * In addition, the property groupObjectClass should be defined in the
 * resource.def file and have the value of the group object class as specified
 * in schema.dsml.
 */
class RDBMSConnector implements ServiceProviderV2 {

	private static final String DATASOURCE_ATTR_NAME = "erRDBMSDataSource";

	private static final String ADD_SQL_PROP_NAME = "erRDBMSAddSQL";

	private static final String CHANGE_PWD_SQL_PROP_NAME = "erRDBMSChangePasswordSQL";

	private static final String DELETE_SQL_PROP_NAME = "erRDBMSDeleteSQL";

	private static final String MODIFYPROP_NAME = "erRDBMSModifySQL";

	private static final String RESTORE_SQL_PROP_NAME = "erRDBMSRestoreSQL";

	private static final String SUSPEND_SQL_PROP_NAME = "erRDBMSSuspendSQL";

	private static final String UTF8 = "UTF-8";

	private static final String ATTR_MAP_PROP_NAME = "erRDBMSAttributeMap";

	private static final String RECON_TRANS_PROP_NAME = "erRDBMSReconTransactional";

	private Context context;

	private final ServiceProviderInformation serviceProviderInfo;

	private final String dataSourceName;

	/**
	 * Creates an RDBMSConnector object.
	 * 
	 * @param serviceProviderInfo
	 *            Encapsulates information about the service instance
	 */
	public RDBMSConnector(ServiceProviderInformation serviceProviderInfo) {
		this.serviceProviderInfo = serviceProviderInfo;
		dataSourceName = serviceProviderInfo.getProperties().getProperty(
				DATASOURCE_ATTR_NAME);
	}

	/**
	 * Adds an entity to the remote resource with the given attributes. The SQL
	 * statement to execute is read from the service object.
	 * 
	 * @param attributes
	 *            The attribute values to be added with the entity.
	 * @param objectClass
	 *            The class of object to add
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus add(String objectClass, AttributeValues attributes,
			String requestID) {

		SystemLog.getInstance().logInformation(this, "[add] " + requestID);

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			String attributeMapString = serviceProviderInfo.getProperties()
					.getProperty(ATTR_MAP_PROP_NAME);
			RDBMSAttributeMap attributeMap = new RDBMSAttributeMap();
			attributeMap.parsePropertiesString(attributeMapString);
			String addSQL = (String) serviceProviderInfo.getProperties()
					.getProperty(ADD_SQL_PROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(addSQL);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), attributes, attributeMap);
			SystemLog.getInstance().logInformation(this, "[add] sql: " + sql);
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;
	}

	/**
	 * Change password for an entity on the remote database. The format of the
	 * password is plain text.
	 * 
	 * @param entityDN
	 *            The identity of the entity in the local data store
	 * @param newPassword
	 *            The new password
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus changePassword(String entityDN, byte[] newPassword,
			String requestID) {

		SystemLog.getInstance().logInformation(this,
				"[changePassword] " + requestID);

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			// find uid attribute in given attributes to create DN with
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entityDN));
			Account account = (Account) accountEntity.getDirectoryObject();
			String changePwdSQL = (String) serviceProviderInfo.getProperties()
					.getProperty(CHANGE_PWD_SQL_PROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(changePwdSQL);
			String password = new String(newPassword, UTF8);
			AttributeValue passwordAV = new AttributeValue(
					Account.ACCOUNT_ATTR_PASSWORD, password);
			AttributeValues attributes = account.getAttributes();
			attributes.put(passwordAV);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), attributes);
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ModelCommunicationException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ObjectNotFoundException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;

	}

	/**
	 * Delete an entity on the remote database.
	 * 
	 * @param entityDN
	 *            The identity of the entity in the local data store
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus delete(String entityDN, String requestID) {

		SystemLog.getInstance().logInformation(this, "[delete] " + requestID);

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			// find uid attribute in given attributes to create DN with
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entityDN));
			Account account = (Account) accountEntity.getDirectoryObject();
			String deleteSQL = (String) serviceProviderInfo.getProperties()
					.getProperty(DELETE_SQL_PROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(deleteSQL);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), account.getAttributes());
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ModelCommunicationException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ObjectNotFoundException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;
	}

	/**
	 * Gets the ServiceProviderInfo for this type of ServiceProvider
	 * 
	 * @return ServiceProviderInformation Encapsulating the service provider
	 *         parameters
	 */
	public ServiceProviderInformation getServiceProviderInfo() {
		return this.getServiceProviderInfo();
	}

	/**
	 * Modify an entity on the remote database with the given attributes. If no
	 * operation is specified it will be assumed to be replace.
	 * 
	 * @param entityDN
	 *            The identity of the entity in the local data store
	 * @param attributeChanges
	 *            The attribute values and operations to be changed
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus modify(String entityDN,
			AttributeChanges attributeChanges, String requestID) {

		SystemLog.getInstance().logInformation(
				this,
				"[modify] " + requestID + ", entityDN: " + entityDN
						+ ", attributeChanges.size(): "
						+ attributeChanges.size());

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			// find uid attribute in given attributes to create DN with
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entityDN));
			Account account = (Account) accountEntity.getDirectoryObject();
			String modifySQL = (String) serviceProviderInfo.getProperties()
					.getProperty(MODIFYPROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(modifySQL);
			String attributeMapString = serviceProviderInfo.getProperties()
					.getProperty(ATTR_MAP_PROP_NAME);
			RDBMSAttributeMap attributeMap = new RDBMSAttributeMap();
			attributeMap.parsePropertiesString(attributeMapString);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), account.getAttributes(), attributeMap,
					attributeChanges);
			SystemLog.getInstance()
					.logInformation(this, "[modify] sql: " + sql);
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ModelCommunicationException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ObjectNotFoundException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;
	}

	/**
	 * Suspend an entity on the database.
	 * 
	 * @param entityDN
	 *            The identity of the entity in the local data store
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus suspend(String entityDN, String requestID) {
		SystemLog.getInstance().logInformation(this, "[suspend] " + requestID);

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			// find uid attribute in given attributes to create DN with
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entityDN));
			Account account = (Account) accountEntity.getDirectoryObject();
			String suspendSQL = (String) serviceProviderInfo.getProperties()
					.getProperty(SUSPEND_SQL_PROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(suspendSQL);
			AttributeValue statusAV = new AttributeValue(
					Account.ACCOUNT_ATTR_STATUS, Account.INACTIVE_STATUS);
			AttributeValues attributes = account.getAttributes();
			attributes.put(statusAV);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), attributes);
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ModelCommunicationException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ObjectNotFoundException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;
	}

	/**
	 * Restore an entity on the remote resource.
	 * 
	 * @param entityDN
	 *            The identity of the entity in the local data store
	 * @param newPassword
	 *            The password given to the restored entity
	 * @param requestID
	 *            The id of the request
	 * @return The status of the request
	 */
	public RequestStatus restore(String entityDN, byte[] newPassword,
			String requestID) {
		SystemLog.getInstance().logInformation(this, "[restore] " + requestID);

		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		Connection con = null;
		Statement statement = null;
		try {
			// find uid attribute in given attributes to create DN with
			AccountSearch accountSearch = new AccountSearch();
			AccountEntity accountEntity = accountSearch
					.lookup(new DistinguishedName(entityDN));
			Account account = (Account) accountEntity.getDirectoryObject();
			String suspendSQL = (String) serviceProviderInfo.getProperties()
					.getProperty(RESTORE_SQL_PROP_NAME);
			SQLStatement sQLStatement = new SQLStatement(suspendSQL);
			AttributeValue statusAV = new AttributeValue(
					Account.ACCOUNT_ATTR_STATUS, Account.ACTIVE_STATUS);
			AttributeValues attributes = account.getAttributes();
			attributes.put(statusAV);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), attributes);
			con = getConnection();
			statement = con.createStatement();
			statement.executeUpdate(sql);
			status.setStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (RDBMSSQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ModelCommunicationException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} catch (ObjectNotFoundException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setReasonMessage(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Throwable t) {
				}
			}
		}
		return status;
	}

	/**
	 * Searches the database and translates the result from JDBC terms to ITIM
	 * native terms. The SQL statement to execute is read from the service
	 * object.
	 * 
	 * @param searchCriteria
	 *            entities returned should matching this criteria
	 * @param requestID
	 *            The id of the request
	 * @return the set of accounts and supporting data matching the search
	 *         criteria
	 */
	public SearchResults search(SearchCriteria searchCriteria, String requestID) {

		SystemLog.getInstance().logInformation(this, "[search] " + requestID);
		try {
			String attributeMapString = serviceProviderInfo.getProperties()
					.getProperty(ATTR_MAP_PROP_NAME);
			RDBMSAttributeMap attributeMap = new RDBMSAttributeMap();
			attributeMap.parsePropertiesString(attributeMapString);
			AccountQueryMetaDataFactory accountMDFactory = new AccountQueryMetaDataFactory();
			QueryMetaData accountMD = accountMDFactory.createQueryMetaData(
					serviceProviderInfo, attributeMap);
			GroupQueryMetaDataFactory groupMDFactory = new GroupQueryMetaDataFactory();
			QueryMetaData groupMD = groupMDFactory
					.createQueryMetaData(serviceProviderInfo);
			QueryMetaData[] queryMetaData = { accountMD, groupMD };
			String reconTransString = serviceProviderInfo.getProperties()
					.getProperty(RECON_TRANS_PROP_NAME);
			SystemLog.getInstance().logInformation(this,
					"[search] " + "reconTransString: " + reconTransString);
			boolean reconTrans = false;
			if (reconTransString != null) {
				reconTrans = new Boolean(reconTransString).booleanValue();
			}
			SearchResults results = null;
			if (!reconTrans) {
				CompositeSearchResults searchResults = new CompositeSearchResults(
						queryMetaData, serviceProviderInfo);
				searchResults.execute();
				results = searchResults;
			} else {
				RDBMSSearchObject rDBMSSearchObject = new RDBMSSearchObject(
						queryMetaData, serviceProviderInfo);
				results = rDBMSSearchObject.execute();
			}
			return results;
		} catch (RemoteServicesException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			return new NoSearchResults(e);
		}
	}

	/**
	 * Test a connection to the database.
	 * 
	 * @return The status of the test
	 * @throws RemoteServicesException
	 *             If there was a problem communicating with the resource
	 */
	public boolean test() throws RemoteServicesException {
		return test2().succeeded();
	}
	
	public RequestStatus test2() throws RemoteServicesException {
		Connection con;
		try {
			con = getConnection();
			
			if (con == null) {
				return new RequestStatus(RequestStatus.UNSUCCESSFUL);
			}
			
			return new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			throw new RemoteServicesException(e.getMessage(), e);
		} catch (SQLException e) {
			throw new RemoteServicesException(e.getMessage(), e);
		}
	}

	// Gets a connection from the application server connection pool.
	private Connection getConnection() throws NamingException, SQLException {
		if (context == null) {
			Properties p = new Properties();
			context = new InitialContext(p);
		}
		DataSource ds = (DataSource) context.lookup(dataSourceName);
		Connection conn = ds.getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		return conn;
	}

}
