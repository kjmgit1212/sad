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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

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
 * understandable by TIM.
 */
class CompositeSearchResults implements SearchResults {

	private static final String DATASOURCE_ATTR_NAME = "erRDBMSDataSource";

	private static final String ATTR_MAP_PROP_NAME = "erRDBMSAttributeMap";

	private static final char EQUALS = '=';

	private final QueryMetaData[] queryMetaData;

	private final ServiceProviderInformation serviceProviderInfo;

	private Context context;

	private final String dataSourceName;

	private final RequestStatus status = new RequestStatus(
			RequestStatus.SUCCESSFUL);

	private Connection con;

	private Statement statement;

	private ResultSet rs;

	private RDBMSAttributeMap attributeMap;

	private int currentQuery;

	private ResultSetMetaData metaData;

	private final ValueMap valueMap;

	/**
	 * Creates a CompositeSearchResults object.
	 * 
	 * @param queryMetaData
	 *            encapsulates query string, naming attribute, and object class
	 *            for all queries to be executed.
	 * @param searchCriteria
	 *            entities returned should matching this criteria
	 */
	CompositeSearchResults(QueryMetaData[] queryMetaData,
			ServiceProviderInformation serviceProviderInfo) {
		this.queryMetaData = queryMetaData;
		this.serviceProviderInfo = serviceProviderInfo;
		dataSourceName = serviceProviderInfo.getProperties().getProperty(
				DATASOURCE_ATTR_NAME);
		valueMap = new ValueMap(serviceProviderInfo);
	}

	/**
	 * Searches the database and translates the result from JDBC terms to ITIM
	 * native terms.
	 */
	void execute() {
		execute(queryMetaData[0].getRawSQLQuery());
	}

	/**
	 * The status of the search operation. Successful unless were errors
	 * iterating through the rows.
	 * 
	 * @return successful if the searches could be executed successfully and
	 *         there have not yet been any errors iterating through the result
	 *         set
	 */
	public RequestStatus getRequestStatus() {
		return status;
	}

	/**
	 * Returns true if there are more rows in the result set and moves the
	 * cursor foward one row. When the end of the current query is reached,
	 * executes the next query and positions the result set at the first row.
	 * Returns false when there are no more queries to execute.
	 * 
	 * @return true if there are more rows in the result set
	 * @throws RemoteServicesException
	 *             wrapping an SQL Exception
	 */
	public boolean hasNext() throws RemoteServicesException {
		try {
			if (rs.next()) {
				return true;
			}
			if (currentQuery < (queryMetaData.length - 1)) {
				currentQuery++;
				execute(queryMetaData[currentQuery].getRawSQLQuery());
				return rs.next();
			}
			return false;
		} catch (SQLException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
			throw new RemoteServicesException(e.getMessage(), e);
		}
	}

	/**
	 * Translates the next row into a SearchResult object.
	 * 
	 * @return A SearchResult encapsulating the next row
	 * @throws RemoteServicesException
	 *             wrapping an SQL Exception
	 */
	public SearchResult next() throws RemoteServicesException {
		try {
			String namingAttribute = queryMetaData[currentQuery]
					.getNamingAttribute();
			SystemLog.getInstance().logInformation(this,
					"[next] namingAttribute: " + namingAttribute);
			String timNamingAttribute = attributeMap
					.getTIMAttributeName(namingAttribute);
			String dn = timNamingAttribute + EQUALS
					+ rs.getString(namingAttribute);

			SystemLog.getInstance().logInformation(this, "[next] dn: " + dn);
			Collection<String> objectClasses = new ArrayList<String>();
			objectClasses.add(queryMetaData[currentQuery].getObjectClass());
			int columnCount = metaData.getColumnCount();
			AttributeValues attributeValues = new AttributeValues();
			for (int i = 1; i <= columnCount; i++) {
				String remoteName = metaData.getColumnLabel(i);
				String name = (String) attributeMap
						.getTIMAttributeName(remoteName);
				String value = rs.getString(i);
				String timValue = valueMap.getTIMValue(name, value);
				SystemLog.getInstance().logInformation(
						this,
						"[next] " + "name: " + name + ", value: " + value
								+ ", timValue: " + timValue);
				AttributeValue av = new AttributeValue(name, timValue);
				attributeValues.put(av);
			}
			return new SearchResult(dn, objectClasses, attributeValues);
		} catch (SQLException e) {
			status.setStatus(RequestStatus.UNSUCCESSFUL);
			status.setReasonMessage(e.getMessage());
			throw new RemoteServicesException(e.getMessage(), e);
		}
	}

	/**
	 * Closes the connection, statement, and result set.
	 * 
	 * @throws RemoteServicesException
	 *             wrapping an SQL Exception
	 */
	public void close() throws RemoteServicesException {
		close(con, statement, rs);
	}

	// Gets a connection from the application server connection pool.
	private Connection getConnection() throws NamingException, SQLException {
		if (context == null) {
			Properties p = new Properties();
			context = new InitialContext(p);
		}
		DataSource ds = (DataSource) context.lookup(dataSourceName);
		Connection conn = ds.getConnection();
		conn.setReadOnly(true);
		return conn;
	}

	// close RDBMS objects
	private void close(Statement statement, ResultSet rs) {
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

	// Searches the database and translates the result from JDBC terms to ITIM
	private void execute(String rawQueryString) {
		try {
			SQLStatement sQLStatement = new SQLStatement(rawQueryString);
			String sql = sQLStatement.substitute(serviceProviderInfo
					.getProperties(), new AttributeValues());
			SystemLog.getInstance().logInformation(this,
					"[execute] sql: " + sql);
			if (con == null) {
				con = getConnection();
			}
			close(statement, rs); // close previous query, if there was one
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			metaData = rs.getMetaData();
			String attributeMapString = serviceProviderInfo.getProperties()
					.getProperty(ATTR_MAP_PROP_NAME);
			attributeMap = new RDBMSAttributeMap();
			attributeMap.parsePropertiesString(attributeMapString);
		} catch (NamingException e) {
			close(con, statement, rs);
			setStatus(e);
		} catch (SQLException e) {
			close(con, statement, rs);
			setStatus(e);
		} catch (RDBMSSQLException e) {
			close(con, statement, rs);
			setStatus(e);
		}
	}

	// set status and log an exception
	private void setStatus(Exception e) {
		SystemLog.getInstance().logError(this, e.getMessage());
		status.setStatus(RequestStatus.UNSUCCESSFUL);
		status.setReasonMessage(e.getMessage());
	}

}
