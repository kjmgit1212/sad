#!/bin/sh

#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2003, 2007 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************

JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%

CP=.:../../
CP=$CP:../../lib/examples.jar
CP=$CP:$ITIM_HOME/data
CP=$CP:$ITIM_HOME/lib/enroleagent.jar
CP=$CP:$ITIM_HOME/lib/itim_api.jar
CP=$CP:$ITIM_HOME/lib/itim_common.jar
CP=$CP:$ITIM_HOME/lib/com.ibm.cv.kmip.ext.jar
CP=$CP:$ITIM_HOME/lib/itim_server.jar
CP=$CP:$ITIM_HOME/lib/itim_server_api.jar
CP=$CP:$ITIM_HOME/lib/jlog.jar
CP=$CP:$ITIM_HOME/lib/regexp.jar

$JAVA_HOME/bin/java -classpath $CP examples.passwordrules.ExampleRunner $1 $2 $3 $4
