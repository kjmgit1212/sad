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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.remoteservices.exception.RemoteServicesException;
import com.ibm.itim.remoteservices.provider.RequestStatus;
import com.ibm.itim.remoteservices.provider.SearchResult;
import com.ibm.itim.remoteservices.provider.SearchResults;
import com.ibm.itim.remoteservices.provider.ServiceProviderInformation;

/**
 * Performs SQL search execution for multiple queries and construction of a
 * composite result set. Adapts the results returned from the database to a form
 * understandable by TIM. Encapsulates search execution in a transaction.
 */
class RDBMSSearchObject {

	private static final String DATASOURCE_ATTR_NAME = "erRDBMSDataSource";

	private static final String ATTR_MAP_PROP_NAME = "erRDBMSAttributeMap";

	private static final char EQUALS = '=';

	// User transaction JNDI name. It varies with different application server
	// vendors
	private static String USER_TX_JNDI_NAME = "jta/usertransaction";

	private final QueryMetaData[] queryMetaData;

	private final ServiceProviderInformation serviceProviderInfo;

	private final ValueMap valueMap;

	private final Collection<SearchResult> results = new Vector<SearchResult>();

	/**
	 * Creates a RDBMSSearchResults object but does not execute the search.
	 * 
	 * @param queryMetaData
	 *            encapsulates query string, naming attribute, and object class
	 *            for all queries to be executed.
	 * @param searchCriteria
	 *            entities returned should matching this criteria
	 */
	RDBMSSearchObject(QueryMetaData[] queryMetaData,
			ServiceProviderInformation serviceProviderInfo) {
		this.queryMetaData = queryMetaData;
		this.serviceProviderInfo = serviceProviderInfo;
		valueMap = new ValueMap(serviceProviderInfo);
	}

	/**
	 * Searches the database and translates the result from JDBC terms to ITIM
	 * native terms.
	 */
	SearchResults execute() {
		RequestStatus status = null;
		for (int i = 0; i < queryMetaData.length; i++) {
			status = execute(queryMetaData[i]);
			if (!status.succeeded()) {
				break;
			}
		}
		return new RDBMSSearchResults(status, results.iterator());
	}

	// close RDBMS objects
	private void close(Connection con, Statement statement, ResultSet rs) {
		if (con != null) {
			try {
				con.close();
			} catch (Throwable t) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (Throwable t) {
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable t) {
			}
		}
	}

	// commits the database transaction
	private RequestStatus commitTransaction(UserTransaction transaction) {
		RequestStatus status = null;
		try {
			transaction.commit();
		} catch (SecurityException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		} catch (IllegalStateException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		} catch (RollbackException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		} catch (HeuristicMixedException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		} catch (HeuristicRollbackException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		} catch (SystemException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
		}
		return status;
	}

	// Gets a connection from the application server connection pool.
	private Connection getConnection() throws NamingException, SQLException {
		Properties p = new Properties();
		Context context = new InitialContext(p);
		String dataSourceName = serviceProviderInfo.getProperties()
				.getProperty(DATASOURCE_ATTR_NAME);
		DataSource ds = (DataSource) context.lookup(dataSourceName);
		Connection conn = ds.getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		return conn;
	}

	private static UserTransaction getUserTransaction() throws NamingException {
		Properties p = new Properties();
		Context context = new InitialContext(p);
		return (UserTransaction) context.lookup(USER_TX_JNDI_NAME);
	}

	// Searches the database and translates the result from JDBC terms to ITIM
	private RequestStatus execute(QueryMetaData queryMetaData) {
		String rawQueryString = queryMetaData.getRawSQLQuery();
		SystemLog.getInstance().logInformation(this,
				"[execute] " + "rawQueryString: " + rawQueryString);
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		RequestStatus status = null;
		UserTransaction transaction = null;
		try {
			transaction = getUserTransaction();
			transaction.begin();
			SQLStatement sQLStatement = new SQLStatement(rawQueryString);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), new AttributeValues());
			SystemLog.getInstance().logInformation(this,
					"[execute] sql: " + sql);
			con = getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			String attributeMapString = serviceProviderInfo.getProperties()
					.getProperty(ATTR_MAP_PROP_NAME);
			RDBMSAttributeMap attributeMap = new RDBMSAttributeMap();
			attributeMap.parsePropertiesString(attributeMapString);
			while (rs.next()) {
				String namingAttribute = queryMetaData.getNamingAttribute();
				SystemLog.getInstance().logInformation(this,
						"[execute] namingAttribute: " + namingAttribute);
				String timNamingAttribute = attributeMap
						.getTIMAttributeName(namingAttribute);
				String dn = timNamingAttribute + EQUALS
						+ rs.getString(namingAttribute);

				SystemLog.getInstance().logInformation(this,
						"[execute] dn: " + dn);
				Collection<String> objectClasses = new ArrayList<String>();
				objectClasses.add(queryMetaData.getObjectClass());
				int columnCount = metaData.getColumnCount();
				AttributeValues attrValues = new AttributeValues();
				for (int i = 1; i <= columnCount; i++) {
					String remoteName = metaData.getColumnLabel(i);
					String name = (String) attributeMap
							.getTIMAttributeName(remoteName);
					String value = rs.getString(i);
					String timValue = valueMap.getTIMValue(name, value);
					SystemLog.getInstance().logInformation(
							this,
							"[execute] " + "name: " + name + ", value: "
									+ value + ", timValue: " + timValue);
					AttributeValue av = new AttributeValue(name, timValue);
					attrValues.put(av);
				}
				results.add(new SearchResult(dn, objectClasses, attrValues));
			}
			commitTransaction(transaction);
			status = new RequestStatus(RequestStatus.SUCCESSFUL);
		} catch (NamingException e) {
			status = setStatus(e);
			rollbackTransaction(transaction);
		} catch (SQLException e) {
			status = setStatus(e);
			rollbackTransaction(transaction);
		} catch (RDBMSSQLException e) {
			status = setStatus(e);
			rollbackTransaction(transaction);
		} catch (NotSupportedException e) {
			status = setStatus(e);
		} catch (SystemException e) {
			status = setStatus(e);
		} finally {
			close(con, statement, rs);
		}
		return status;
	}

	// commits the database transaction
	private void rollbackTransaction(UserTransaction transaction) {
		try {
			if (transaction != null) {
				transaction.rollback();
			}
		} catch (SecurityException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		} catch (IllegalStateException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		} catch (SystemException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
		}
	}

	// set status and log an exception
	private RequestStatus setStatus(Exception e) {
		SystemLog.getInstance().logError(this, e.getMessage());
		RequestStatus status = new RequestStatus(RequestStatus.UNSUCCESSFUL);
		status.setReasonMessage(e.getMessage());
		return status;
	}

	/**
	 * Encapsulates results stored in a collection to a form readable by TIM.
	 */
	private static class RDBMSSearchResults implements SearchResults {

		private final Iterator it;

		private final RequestStatus status;

		/**
		 * Creates a RDBMSSearchResults object.
		 * 
		 * @param status
		 *            the status of the search
		 * @param it
		 *            to iterate through the results
		 */
		RDBMSSearchResults(RequestStatus status, Iterator it) {
			this.status = status;
			this.it = it;
		}

		/**
		 * The status of the search operation.
		 * 
		 * @return successful if the searches could be executed successfully and
		 *         there were no errors iterating through the result set
		 */
		public RequestStatus getRequestStatus() {
			return status;
		}

		/**
		 * Returns true if there are more rows in the result set.
		 * 
		 * @return true if there are more rows in the result set
		 * @throws RemoteServicesException
		 *             wrapping an SQL Exception
		 */
		public boolean hasNext() throws RemoteServicesException {
			return (status.succeeded() && (it != null) && it.hasNext());
		}

		/**
		 * Translates the next row into a SearchResult object.
		 * 
		 * @return A SearchResult encapsulating the next row
		 */
		public SearchResult next() {
			return (SearchResult) it.next();
		}

		/**
		 * Result set is closed immediately after the search is executed and
		 * results are collected so this method is redundant.
		 */
		public void close() {
		}
	}

}
