############################################################ {COPYRIGHT-TOP} ###
# IBM Confidential
# OCO Source Materials
# Tivoli Common Reporting Infrastructure
# 5724-Q87
#
# (c) Copyright IBM Corp. 2007, 2008
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
############################################################ {COPYRIGHT-END} ##
####

#------------------------------------------------------------
# BEGIN primary configuration
#------------------------------------------------------------

#------------------------------------------------------------
# aliasUser: a valid userId with which to log in to the ITIM database
# aliasPW: the password for the aliasUser userId
#------------------------------------------------------------
aliasUser = "itimuser"
aliasPW   = "mypassword"

#------------------------------------------------------------
# dsDBVendor: either "db2", "oracle", or "sqlserver"
#------------------------------------------------------------
dsDBVendor = "db2"

#------------------------------------------------------------
# For DB2 and SQL Server databases:
#   dsDBName: the name of the database
#   dsDBServer: the name or IP address of the database server
#   dsDBPort: the database server connection port
#   dsDBType: JDBC type of driver to use when defining the data source: "4" for DB2 and Microsoft SQL Server; "thin" for Oracle
#------------------------------------------------------------
dsDBName = "itimdb"
dsDBServer = "myserver.mydomain.com"
dsDBPort = "60000"
dsDBType = "4"

#------------------------------------------------------------
# For Oracle databases:
#   dsDBURL: the complete JDBC URL of the database
#------------------------------------------------------------
dsDBURL = "jdbc:oracle:thin:@myserver.mydomain.com:1521:itimdb"

#------------------------------------------------------------
# providerCP: the classpath used to define the JDBC provider
#   Example DB2 value of Windows: "c:/Program Files/IBM/SQLLIB/java/db2jcc.jar;c:/Program Files/IBM/SQLLIB/java/db2jcc_license_cu.jar"
#   Example Microsoft SQL Server value on Windows: "C:/Program Files/Microsoft SQL Server 2005 JDBC Driver/sqljdbc_1.1/enu/sqljdbc.jar"
#   Example DB2 value on UNIX: "/opt/IBM/db2/V9.1/java/db2jcc.jar;/opt/IBM/db2/V9.1/java/db2jcc_license_cu.jar"
#   Example Oracle value on UNIX: "/u01/app/oracle/product/10.2.0/Db_1/jdbc/lib/ojdbc14.jar"
#------------------------------------------------------------
providerCP  = "c:/Program Files/IBM/SQLLIB/java/db2jcc.jar;c:/Program Files/IBM/SQLLIB/java/db2jcc_license_cu.jar"

#------------------------------------------------------------
# providerImplClass: the class which implements the JDBC connection pool
#   DB2: "com.ibm.db2.jcc.DB2ConnectionPoolDataSource"
#   Microsoft SQL Server: "com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource"
#   Oracle: oracle.jdbc.pool.OracleConnectionPoolDataSource
#------------------------------------------------------------
providerImplClass = "com.ibm.db2.jcc.DB2ConnectionPoolDataSource"

#------------------------------------------------------------
# dsDBHelper: the class to use as a datasource helper
#   DB2: "com.ibm.websphere.rsadapter.DB2UniversalDataStoreHelper"
#   Microsoft SQL Server: com.ibm.websphere.rsadapter.ConnectJDBCDataStoreHelper
#   Oracle: com.ibm.websphere.rsadapter.Oracle10gDataStoreHelper
#------------------------------------------------------------
dsDBHelper = "com.ibm.websphere.rsadapter.DB2UniversalDataStoreHelper"

#------------------------------------------------------------
# END primary configuration
#------------------------------------------------------------

#------------------------------------------------------------
# BEGIN secondary configuration
#------------------------------------------------------------

#------------------------------------------------------------
# reportDS: the JNDI name for the data source
#------------------------------------------------------------
reportDS = "jdbc/ibm/tivoli/tim"

#------------------------------------------------------------
# aliasName: the configuration name to use when creating the JAAS alias
# aliasDesc: a description for the JAAS alias
#------------------------------------------------------------
aliasName = "TIM DB Alias"
aliasDesc = "JAAS Authentication Alias for TIM DB"

#------------------------------------------------------------
# providerName: the configuration name to use when creating the JDBC provider
# providerDesc: a description for the JDBC provider
#------------------------------------------------------------
providerName  = "TIM DB JDBC Provider"
providerDesc  = "JDBC Provider for TIM DB"

#------------------------------------------------------------
# dsName: the configuration name to use when creating the data source
# dsDesc: a description for the data source
#------------------------------------------------------------
dsName   = "TIM DB Data Source"
dsDesc   = "Data Source for TIM DB"

#------------------------------------------------------------
# END secondary configuration
#------------------------------------------------------------

#------------------------------------------------------------
#------------------------------------------------------------

if dsDBVendor not in ["db2", "oracle", "sqlserver"]: raise "Bad dsDBVendor: %s" % dsDBVendor

#-------------------------------------------------------------------------------
# Create JAASAuthData alias
#-------------------------------------------------------------------------------
print "Creating JAASAuthData alias ..... "

sec = AdminConfig.getid("/Cell:/Security:/")

alias_attr_name = ['alias', aliasName]

alias_attr_desc = ['description', aliasDesc]

alias_attr_userid = ['userId', aliasUser]

alias_attr_pw = ['password', aliasPW]

alias_attrs = [alias_attr_name, alias_attr_desc, alias_attr_userid, alias_attr_pw]

authdata = AdminConfig.create('JAASAuthData', sec, alias_attrs)

print "Saving creation of JAASAuthData alias configuration changes made so far..."
AdminConfig.save()
print "   Saved configuration changes so far."

print "JAASAuthData authentication alias has been created successfully."

#-------------------------------------------------------------------------------
# Create the JDBCProvider
#-------------------------------------------------------------------------------
print "Creating JDBCProvider....."

cell = AdminConfig.getid('/Cell:/')

provider_attr_cp = ['classpath', providerCP]

provider_attr_implclass = ['implementationClassName', providerImplClass]

provider_attr_name = ['name', providerName]

provider_attr_desc = ['description', providerDesc]

provider_attrs = [provider_attr_cp, provider_attr_implclass, provider_attr_name, provider_attr_desc]

provider = AdminConfig.create('JDBCProvider', cell, provider_attrs)

print "Saving creation of JDBCProvider configuration changes made so far..."
AdminConfig.save()
print "   Saved all configuration changes."

print "JDBCProvider has been created successfully."

#-------------------------------------------------------------------------------
# Create DataSource
#-------------------------------------------------------------------------------
print "Creating DataSource ..... "

ds_attr_name = ['name', dsName]
ds_attr_desc = ['description', dsDesc]
ds_attrs = [ds_attr_name, ds_attr_desc]
dataSource = AdminConfig.create('DataSource', provider, ds_attrs)

ds_props = AdminConfig.create('J2EEResourcePropertySet', dataSource, [])

dsDBProps = [{'databaseName': dsDBName, 'driverType': dsDBType, 'serverName': dsDBServer, 'portNumber': dsDBPort}, {'URL': dsDBURL}][dsDBVendor=="oracle"]

for dsDBPropName, dsDBPropValue in dsDBProps.items():
	AdminConfig.create('J2EEResourceProperty', ds_props, [['name', dsDBPropName], ['value', dsDBPropValue], ['type', ['java.lang.String', 'java.lang.Integer'][dsDBPropName=="portNumber"]]])

ds_mod_jndi = ['jndiName', reportDS]
ds_mod_alias  = ['authDataAlias', aliasName]
ds_mod_helper = ['datasourceHelperClassname', dsDBHelper]

ds_mod_propr = [ds_mod_jndi, ds_mod_alias, ds_mod_helper]

AdminConfig.modify(dataSource, ds_mod_propr)

print "Saving creation of Datasource configuration changes made so far"
AdminConfig.save()
print "   Saved all configuration changes"

print "Datasource has been created successfully."
