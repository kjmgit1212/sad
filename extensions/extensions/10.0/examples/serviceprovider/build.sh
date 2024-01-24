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
# ANT_HOME_LIB=%VAR_ANT_HOME_LIB%
# Points to the default location of ANT_HOME_LIB in WAS 6.1
ANT_HOME_LIB=$JAVA_HOME/../deploytool/itp/plugins/org.apache.ant_1.6.5/lib

ANT_OPTS="-ms256m -mx512m"


CP=$ANT_HOME_LIB/ant.jar:$ANT_HOME_LIB/ant-launcher.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Ditim.home=$ITIM_HOME $1 $2 $3 $4 $5 $6 $7 $8 $9
