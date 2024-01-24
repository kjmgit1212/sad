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

/**
 * Encapsulates meta data about the SQL query and the rows in the associated
 * data set. This meta data is used to translate the rows into search result
 * entries that can be processed by TIM as either accounts or supporting data.
 */
class QueryMetaData {

	// SQL query before substitution of variables
	private final String rawSQLQuery;

	private final String namingAttribute;

	private final String objectClass;

	QueryMetaData(String rawSQLQuery, String namingAttribute, String objectClass) {
		this.rawSQLQuery = rawSQLQuery;
		this.namingAttribute = namingAttribute;
		this.objectClass = objectClass;
	}

	String getNamingAttribute() {
		return namingAttribute;
	}

	String getObjectClass() {
		return objectClass;
	}

	String getRawSQLQuery() {
		return rawSQLQuery;
	}
}
