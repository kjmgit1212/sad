#!/bin/sh
#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2012 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************
APP_SRV_HOME=%APP_SRV_HOME%
JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%

#********************************************************************
#* If the APP_SRV_HOME is set to custom profile location, then the following
#* variable needs to be modified to have the correct WebSphere application 
#* server home directory.
#********************************************************************
APP_SRV_TOP=$APP_SRV_HOME/../..

CP=$ITIM_HOME/lib/itim_ws_model.jar
CP=$ITIM_HOME/lib/itim_ws_client.jar:$CP
CP=$APP_SRV_TOP/runtimes/com.ibm.jaxws.thinclient_7.0.0.jar:$CP
CP=$ITIM_HOME/extensions/{RELEASE_VERSION}/lib/examples.jar:$CP
