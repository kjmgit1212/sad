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
package examples.workflow.customApproval.itim50;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ibm.itim.dataservices.model.ModelCommunicationException;
import com.ibm.itim.dataservices.model.ObjectNotFoundException;
import com.ibm.itim.dataservices.model.PartialResultsException;
import com.ibm.itim.dataservices.model.SearchParameters;
import com.ibm.itim.dataservices.model.SearchResults;
import com.ibm.itim.dataservices.model.SearchResultsIterator;
import com.ibm.itim.dataservices.model.domain.DirectorySystemEntity;
import com.ibm.itim.dataservices.model.domain.DirectorySystemSearch;
import com.ibm.itim.dataservices.model.domain.PersonEntity;
import com.ibm.itim.dataservices.model.domain.PersonSearch;
import com.ibm.itim.logging.SystemLog;
import com.ibm.itim.script.ExtensionBean;

public class CustomApprovalBean implements ExtensionBean {
	private static final SystemLog logger = SystemLog.getInstance();

	/**
	 * Constant for the data source name to use.
	 */
	private static final String DATA_SOURCE_NAME = "enroleDataSource";

	/**
	 * The name (cn) of the System Administrator.
	 */
	private static final String SYSTEM_ADMIN_NAME = "System Administrator";

	private Context context;

	/**
	 * Get the DN (as a String) of the approver for the given group name. If no
	 * accounts match the cn found in the database table, then look for the
	 * account with cn=System Administrator. As a last resort, return null.
	 * 
	 * @param groupName
	 *            The name of the group to find the approver for.
	 * @return The DN of the approver as a String.
	 */
	public String findApprover(String groupName) {
		String approverName = null;
		
		if (groupName != null) {
			approverName = getApproverName(groupName);
		}
		
		return getApproverDN(approverName);
	}

	/**
	 * Given the group to be approved find the corresponding approver.
	 * 
	 * @param groupName
	 *            The name of group to find the approver for.
	 * @return The name of the approver for the specified group.
	 */
	private String getApproverName(String groupName) {
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String approverName = null;
		try {
			con = getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery("SELECT APPROVER_NAME FROM APPROVERS "
					+ "WHERE GROUP_NAME = '" + groupName + "'");
			if (rs.next()) {
				approverName = rs.getString(1);
			}
		} catch (SQLException e) {
			logger.logError(this, e.getMessage());
		} catch (NamingException e) {
			logger.logError(this, e.getMessage());
		} finally {
			close(con, statement, rs);
		}
		return approverName;
	}

	/**
	 * Given the common name of the approver, find the distinguished name.
	 * System Administrator is the backup, if cn is null.
	 * 
	 * @param cn
	 *            The common name of the user to look up. If cn is null, then we
	 *            will look up System Administrator.
	 * 
	 * @return The DN of the person to approve the account, or the system
	 *         administrator if cn is null. In the worst case, return null.
	 */
	private String getApproverDN(String cn) {
		if (cn == null) {
			cn = SYSTEM_ADMIN_NAME;
		}

		PersonSearch pSearch = new PersonSearch();
		String filter = "(cn=" + cn + ")";
		DirectorySystemSearch dSearch = new DirectorySystemSearch();
		SearchParameters params = new SearchParameters();
		SearchResults sr = null;

		String approverDN = null;

		try {
			DirectorySystemEntity tenant = dSearch.lookupDefault();
			sr = pSearch.searchByFilter(tenant, filter, params);
			if (sr.size() != 1 && sr.size() != -1) {
				logger.logError(this, "Person with " + "common name " + cn
						+ " could not be uniquely found.  Instead found "
						+ sr.size() + " people.");
			}
			SearchResultsIterator it = sr.iterator();
			PersonEntity personEntity = (PersonEntity) it.next();
			approverDN = personEntity.getDistinguishedName().toString();
		} catch (ModelCommunicationException e) {
			logger.logError(this, e.getMessage());
		} catch (ObjectNotFoundException e) {
			logger.logError(this, e.getMessage());
		} catch (PartialResultsException e) {
			logger.logError(this, e.getMessage());
		} finally {
			sr.close();
		}
		return approverDN;
	}

	/**
	 * Gets a connection from the application server connection pool.
	 * 
	 * @return A Connection object to use from the application server connection
	 *         pool.
	 */
	private Connection getConnection() throws NamingException, SQLException {
		if (context == null) {
			Properties p = new Properties();
			context = new InitialContext(p);
		}

		DataSource ds = (DataSource) context.lookup(DATA_SOURCE_NAME);
		Connection conn = ds.getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		return conn;
	}

	/**
	 * Safely close all of the resources used for the query. Any of the
	 * parameters may be null.
	 * 
	 * @param con
	 *            Connection to close or null.
	 * @param statement
	 *            Statement to close or null.
	 * @param rs
	 *            ResultSet to close or null.
	 */
	private void close(Connection con, Statement statement, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable t) {
			}
		}

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
}
