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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import antlr.Token;
import antlr.TokenStreamException;

import com.ibm.itim.common.AttributeChangeIterator;
import com.ibm.itim.common.AttributeChangeOperation;
import com.ibm.itim.common.AttributeChanges;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.common.AttributeValueIterator;
import com.ibm.itim.common.AttributeValues;
import com.ibm.itim.logging.SystemLog;

import examples.serviceprovider.rdbms.parser.SQLSubstitutionLexer;
import examples.serviceprovider.rdbms.parser.SQLSubstitutionLexerTokenTypes;

/**
 * Encapsulates an SQL statement entered by the service owner. It is parsed to
 * determine the input parameters so at execution time the parameter values can
 * be substituted in. The parameters should be indicated with a '#' character,
 * for example,
 * 
 * <code>INSERT INTO ACCOUNTS (LOGIN) VALUES ('#eruid')</code>
 */
class SQLStatement {

	private static final char COMMA = ',';

	private static final char EQUALS = '=';

	private static final char QUOTE = '\'';

	private static final String ALL_ATTRIBUTES = "#all_attributes";

	private static final String ALL_VALUES = "#all_values";

	private static final String ALL_CHANGES = "#all_changes";

	private final String sql;

	private List<String> timAttributeNames;

	/**
	 * Creates a SQLStatement object
	 * 
	 * @param sql
	 *            Before parsing
	 */
	SQLStatement(String sql) {
		this.sql = sql;
	}

	/**
	 * Substitutes parameters from the service and account objects into the SQL
	 * statement entered by the service owner on the service form.
	 * 
	 * @param serviceInfoProps
	 *            properties from the service info object
	 * @param accountAttributes
	 *            the account attributes
	 * @return An SQL string that may be used in a Statement.execute() call.
	 * @throws RDBMSSQLException
	 *             if the parameters specified in the
	 */
	String substitute(Properties serviceInfoProps,
			AttributeValues accountAttributes) throws RDBMSSQLException {
		RDBMSAttributeMap map = new RDBMSAttributeMap();
		return substitute(serviceInfoProps, accountAttributes, map);
	}

	/**
	 * Substitutes parameters from the service and account objects into the SQL
	 * statement entered by the service owner on the service form.
	 * 
	 * @param serviceInfoProps
	 *            properties from the service info object
	 * @param accountAttributes
	 *            used for #all_attributes and #all_values macros
	 * @param rdbmsAttributeMap
	 *            map of itim attribtues to database field names
	 * @return An SQL string that may be used in a Statement.execute() call.
	 * @throws RDBMSSQLException
	 *             if the parameters specified in the
	 */
	String substitute(Properties serviceInfoProps,
			AttributeValues accountAttributes,
			RDBMSAttributeMap rdbmsAttributeMap) throws RDBMSSQLException {

		SystemLog.getInstance().logInformation(this,
				"[substitute]: " + "sql: " + sql);

		StringBuffer sb = new StringBuffer();
		StringReader reader = new StringReader(sql);

		SQLSubstitutionLexer lexer = new SQLSubstitutionLexer(reader);
		try {
			Token token = lexer.nextToken();
			while (token.getType() != SQLSubstitutionLexerTokenTypes.EOF) {
				if (token.getType() == SQLSubstitutionLexerTokenTypes.IGNORE) {
					sb.append(token.getText());
				} else if (token.getType() == SQLSubstitutionLexerTokenTypes.VAR) {
					if (token.getText().equalsIgnoreCase(ALL_ATTRIBUTES)) {
						String attr_list = getAttributeNames(accountAttributes,
								rdbmsAttributeMap);
						sb.append(attr_list);
					} else if (token.getText().equalsIgnoreCase(ALL_VALUES)) {
						String attr_values = getAttributeValues(accountAttributes);
						sb.append(attr_values);
					} else {
						String name = token.getText().substring(1,
								token.getText().length());
						if (serviceInfoProps.containsKey(name)) {
							sb.append(serviceInfoProps.getProperty(name));
						} else if (accountAttributes.containsKey(name)) {
							sb.append(accountAttributes.get(name).getString());
						} else {
							SystemLog.getInstance().logError(this,
									"Unrecognized attribute: " + name);
							throw new RDBMSSQLException(
									"Unrecognized attribute: " + name);
						}
					}
				}
				token = lexer.nextToken();
			}
		} catch (TokenStreamException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			throw new RDBMSSQLException(e.getMessage(), e);
		}
		return sb.toString();
	}

	/**
	 * Substitutes parameters from the service and account objects into the SQL
	 * statement entered by the service owner on the service form.
	 * 
	 * @param serviceInfoProps
	 *            properties from the service info object
	 * @param accountAttributes
	 *            the account attributes
	 * @param rdbmsAttributeMap
	 *            map of itim attribtues to database field names
	 * @param attributeChanges
	 *            used for #all_attributes and #all_values macros
	 * @return An SQL string that may be used in a Statement.execute() call.
	 * @throws RDBMSSQLException
	 *             if the parameters specified in the
	 */
	String substitute(Properties serviceInfoProps,
			AttributeValues accountAttributes,
			RDBMSAttributeMap rdbmsAttributeMap,
			AttributeChanges attributeChanges) throws RDBMSSQLException {

		SystemLog.getInstance().logInformation(this,
				"[substitute]: " + "sql: " + sql);

		StringBuffer sb = new StringBuffer();
		StringReader reader = new StringReader(sql);

		SQLSubstitutionLexer lexer = new SQLSubstitutionLexer(reader);
		try {
			Token token = lexer.nextToken();
			while (token.getType() != SQLSubstitutionLexerTokenTypes.EOF) {
				if (token.getType() == SQLSubstitutionLexerTokenTypes.IGNORE) {
					sb.append(token.getText());
				} else if (token.getType() == SQLSubstitutionLexerTokenTypes.VAR) {
					if (token.getText().equalsIgnoreCase(ALL_CHANGES)) {
						String attr_list = getAttributeChanges(
								attributeChanges, rdbmsAttributeMap);
						sb.append(attr_list);
					} else {
						String name = token.getText().substring(1,
								token.getText().length());
						if (serviceInfoProps.containsKey(name)) {
							sb.append(serviceInfoProps.getProperty(name));
						} else if (accountAttributes.containsKey(name)) {
							sb.append(accountAttributes.get(name).getString());
						} else {
							SystemLog.getInstance().logError(this,
									"Unrecognized attribute: " + name);
							throw new RDBMSSQLException(
									"Unrecognized attribute: " + name);
						}
					}
				}
				token = lexer.nextToken();
			}
		} catch (TokenStreamException e) {
			SystemLog.getInstance().logError(this, e.getMessage());
			throw new RDBMSSQLException(e.getMessage(), e);
		}
		return sb.toString();
	}

	/**
	 * Gives a list of all remote attribute names in the
	 * 
	 * @param accountAttributes
	 *            the account attributes to give the name for
	 * @param rdbmsAttributeMap
	 *            map of itim attribtues to database field names
	 * @return string for insertion into a
	 */
	private String getAttributeNames(AttributeValues accountAttributes,
			RDBMSAttributeMap rdbmsAttributeMap) {
		StringBuffer sb = new StringBuffer();
		List timNames = getITIMAttributeNames(accountAttributes);
		Iterator it = timNames.iterator();
		while (it.hasNext()) {
			String timName = (String) it.next();
			String name = rdbmsAttributeMap.getRemoteAttributeName(timName);
			sb.append(name);
			if (it.hasNext()) {
				sb.append(COMMA);
			}
		}
		return sb.toString();
	}

	/**
	 * Gives a list of all remote attribute names in the
	 * 
	 * @param accountAttributes
	 *            the account attributes to give the values for
	 * @return string for insertion into a
	 */
	private String getAttributeValues(AttributeValues accountAttributes) {
		StringBuffer sb = new StringBuffer();
		List timNames = getITIMAttributeNames(accountAttributes);
		Iterator it = timNames.iterator();
		while (it.hasNext()) {
			String timName = (String) it.next();
			String value = accountAttributes.get(timName).getString();
			sb.append(QUOTE);
			sb.append(value);
			sb.append(QUOTE);
			if (it.hasNext()) {
				sb.append(COMMA);
			}
		}
		return sb.toString();
	}

	/**
	 * Gives a list of all remote attribute names in the
	 * 
	 * @param attributeChanges
	 *            the account attributes to give the values for
	 * @return string for insertion into a
	 */
	private String getAttributeChanges(AttributeChanges attributeChanges,
			RDBMSAttributeMap rdbmsAttributeMap) {
		StringBuffer sb = new StringBuffer();
		AttributeChangeIterator it = attributeChanges.iterator();
		HashMap<String, String> changes = new HashMap<String, String>();
		while (it.hasNext()) {
			AttributeChangeOperation operation = it.next();
			Collection<AttributeValue> changeData = operation.getChangeData();

			Iterator<AttributeValue> changeDataIt = changeData.iterator();
			while (changeDataIt.hasNext()) {
				AttributeValue attribute = changeDataIt.next();
				String timName = attribute.getName();
				String remoteName = rdbmsAttributeMap
						.getRemoteAttributeName(timName);
				String value = "";
				if (attribute.getValues().size() > 0) {
					value = attribute.getString();
				}
				if (changes.containsKey("D:" + remoteName)) {
					changes.remove("D:" + remoteName);
					changes.put(remoteName, value);
				} else {
					if (operation.getModificationAction() == AttributeChangeOperation.REMOVE_ACTION) {
						changes.put("D:" + remoteName, value);
					} else {
						changes.put(remoteName, value);
					}
				}
				SystemLog.getInstance().logInformation(
						this,
						"[getAttributeChanges]: " + "timName: " + timName
								+ ", remoteName: " + remoteName + ", value: "
								+ value);
			}
		}
		Iterator<String> keys = changes.keySet().iterator();
		while (keys.hasNext()) {
			String remoteName = keys.next();
			String value = changes.get(remoteName);
			value = remoteName.startsWith("D:") ? "" : value;
			remoteName = remoteName.startsWith("D:") ? remoteName.replaceFirst(
					"D:", "") : remoteName;
			sb.append(remoteName);
			sb.append(EQUALS);
			sb.append(QUOTE);
			sb.append(value);
			sb.append(QUOTE);
			if (keys.hasNext()) {
				sb.append(COMMA);
			}
		}
		return sb.toString();
	}

	// Stores the names of the attributes in an ordered list
	private List getITIMAttributeNames(AttributeValues accountAttributes) {
		if (timAttributeNames != null) {
			return timAttributeNames;
		}
		timAttributeNames = new ArrayList<String>();
		AttributeValueIterator it = accountAttributes.iterator();
		while (it.hasNext()) {
			AttributeValue attribute = it.next();
			timAttributeNames.add(attribute.getName());
		}
		return timAttributeNames;
	}

}
