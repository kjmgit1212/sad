#!/bin/sh

#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2003, 2009 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************
WAS_HOME=%APP_SRV_HOME%
JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%

CP=$ITIM_HOME/lib/fesi.jar
CP=$ITIM_HOME/data:$CP
CP=$ITIM_HOME/lib/jlog.jar:$CP
CP=$ITIM_HOME/lib/jffdc.jar:$CP
CP=$ITIM_HOME/lib/enroleagent.jar:$CP
CP=$ITIM_HOME/lib/itim_common.jar:$CP
CP=$ITIM_HOME/lib/com.ibm.cv.kmip.ext.jar:$CP
CP=$ITIM_HOME/lib/itim_server_api.jar:$CP
CP=$ITIM_HOME/lib/itim_api.jar:$CP
CP=$ITIM_HOME/lib/itim_server.jar:$CP
CP=$WAS_HOME/lib/j2ee.jar:$CP
CP=$ITIM_HOME/lib/aspectjrt.jar:$CP
CP=$ITIM_HOME/extensions/{RELEASE_VERSION}/lib/examples.jar:$CP

$JAVA_HOME/bin/java -classpath $CP examples.dataservices.GetAllServiceAccounts "$1"
