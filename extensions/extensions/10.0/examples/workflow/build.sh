#!/bin/sh

#/******************************************************************************
# *
# * Licensed Materials - Property of IBM
# *
# * Source File Name = build.sh
# *
# * (C) COPYRIGHT IBM Corp. 1999, 2007 All Rights Reserved
# *
# * US Government Users Restricted Rights - Use, duplication or
# * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
# *
# *****************************************************************************/


JAVA_HOME=%JAVA_HOME%
ITIM_HOME=%ITIM_HOME%
APP_SERVER_DEPLOY_DIR=%VAR_APP_SERVER_DEPLOY_DIR%
HTML_ROOT=%VAR_HTML_ROOT%
JDBC_DRIVER_JAR=%VAR_DB2_JDBC_DRIVER_PATH%
ANT_HOME_LIB=%VAR_ANT_LIB_HOME%

COMPILER=modern
EJBCCOMPILER=javac
DEBUG=on
OPTIMIZE=off
DEPRECATION=on
VERBOSE=off
FAILONERROR=yes
ANT_OPTS="-ms256m -mx512m"

$JAVA_HOME/bin/java $ANT_OPTS -classpath $ANT_HOME_LIB/ant.jar:$ANT_HOME_LIB/ant-launcher.jar:$JAVA_HOME/lib/tools.jar:$JDBC_DRIVER_JAR:. org.apache.tools.ant.Main -Ditim.home=$ITIM_HOME -Dapp.deploy=$APP_SERVER_DEPLOY_DIR -Dhtml.root=$HTML_ROOT $1 $2 $3 $4 $5 $6 $7 $8 $9
