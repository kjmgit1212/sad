#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2003, 2009 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************


JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%
WAS_HOME=%APP_SRV_HOME%
JDBC_DRIVER_JARS=%VAR_DB2_JDBC_DRIVER_PATH%

CP=$ITIM_HOME/data:
CP=$ITIM_HOME/lib/enroleagent.jar:$CP
CP=$ITIM_HOME/lib/itim_common.jar:$CP
CP=$ITIM_HOME/lib/com.ibm.cv.kmip.ext.jar:$CP
CP=$ITIM_HOME/lib/itim_server_api.jar:$CP
CP=$ITIM_HOME/lib/itim_api.jar:$CP
CP=$ITIM_HOME/lib/itim_server.jar:$CP
CP=$ITIM_HOME/lib/jlog.jar:$CP
CP=$ITIM_HOME/lib/jffdc.jar:$CP
CP=$ITIM_HOME/lib/ibmjs.jar:$CP
CP=$ITIM_HOME/lib/bsf23.jar:$CP
CP=$ITIM_HOME/extensions/{RELEASE_VERSION}/lib/examples.jar:$CP
CP=$WAS_HOME/lib/j2ee.jar:$CP
CP=$ITIM_HOME/lib/aspectjrt.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.runtime_6.1.0.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.emf_2.1.0.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.wccm_6.1.0.jar:$CP
CP=$WAS_HOME/../../runtimes/com.ibm.ws.webservices.thinclient_6.1.0.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.runtime.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.emf.jar:$CP
CP=$WAS_HOME/../../plugins/com.ibm.ws.wccm.jar:$CP
CP=$WAS_HOME/../../runtimes/com.ibm.ws.webservices.thinclient_7.0.0.jar:$CP
CP=$WAS_HOME/../../runtimes/com.ibm.ws.admin.client_7.0.0.jar:$CP
CP=$JDBC_DRIVER_JARS:$CP

CP=../../..:$CP
CP=.:$CP
export CP

$JAVA_HOME/bin/java -classpath $CP examples.policyanalysis.ProvisioningPolicyAnalysisExample $1 $2 $3 $4
