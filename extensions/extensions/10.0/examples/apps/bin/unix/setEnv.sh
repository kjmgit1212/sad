#!/bin/sh

#/******************************************************************************
# *
# * Licensed Materials - Property of IBM
# *
# * Source File Name = setEnv.sh
# *
# * (C) COPYRIGHT IBM Corp. 2003, 2009 All Rights Reserved
# *
# * US Government Users Restricted Rights - Use, duplication or
# * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
# *
# *****************************************************************************/

# Set Env Vars...                                                        
APP_SRV_HOME=%APP_SRV_HOME%
APP_SRV_TYPE=%APP_SRV_TYPE%
JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%

ITIM_USER="itim manager"
ITIM_PSWD=secret

CP=$JAVA_HOME/jre/lib/ext/jaas.jar

CP=$CP:../../../../lib/examples.jar

case $APP_SRV_TYPE in
	WAS)
		APP_SRV_TOP=$APP_SRV_HOME/../..
		CURRENT_DIR=`pwd`
		cd $APP_SRV_HOME/bin
		. ./setupCmdLine.sh
		cd $CURRENT_DIR
		unset CURRENT_DIR
		
		SYSTEM_PROPERTIES="-Xbootclasspath/p:$WAS_BOOTCLASSPATH -Djava.ext.dirs=$JAVA_HOME/lib/ext:$JAVA_HOME/jre/lib/ext:$WAS_EXT_DIRS:$WAS_HOME/plugins:$WAS_HOME/lib/WMQ/java/lib -Djava.naming.provider.url=$APP_SRV_URL -Djava.naming.factory.initial=com.ibm.websphere.naming.WsnInitialContextFactory -Dserver.root=$WAS_HOME -Ditim.home=$ITIM_HOME $CLIENTSAS $CLIENTSSL $USER_INSTALL_PROP -Djava.util.logging.manager=com.ibm.ws.bootstrap.WsLogManager -Djava.util.logging.configureByServer=true"
		LOGIN_CONFIG=../jaas_login_was.conf
  		PLATFORM_CONTEXT_FACTORY=com.ibm.itim.apps.impl.websphere.WebSpherePlatformContextFactory
		CP=$CP:$ITIM_HOME/lib/api_ejb.jar
		CP=$CP:$ITIM_HOME/lib/itim_api.jar
		CP=$CP:$ITIM_HOME/lib/itim_server_api.jar
		CP=$CP:$ITIM_HOME/lib/jlog.jar
		CP=$CP:$ITIM_HOME/lib/com.ibm.cv.kmip.ext.jar
		CP=$CP:$ITIM_HOME/data
		
		CP=$CP:$APP_SRV_TOP/plugins/com.ibm.ws.runtime_6.1.0.jar
		CP=$CP:$APP_SRV_TOP/plugins/com.ibm.ws.emf_2.1.0.jar
		CP=$CP:$APP_SRV_TOP/plugins/com.ibm.ws.wccm_6.1.0.jar
		CP=$CP:$APP_SRV_TOP/plugins/com.ibm.ws.ejbportable_6.1.0.jar
		
		CP=$CP:$APP_SRV_TOP/runtimes/com.ibm.ws.webservices.thinclient_6.1.0.jar
	;;
esac

export APP_SRV_HOME JAVA_HOME ITIM_HOME LOGIN_CONFIG CP
