#!/bin/sh
#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2007 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************
# Runs the DSMLv2 JNDI provider test program

# modify these script variables for your environment
LIB=lib
#EXAMPLES=examples.jar
EXAMPLES=/home/weaverma/examples.jar
#JAVA_HOME=%JAVA_HOME%
JAVA_HOME=/opt/IBM/SDP70/runtimes/base_v61/java
JAVA=$JAVA_HOME/bin/java
CERT_HOME=cert

# Library files from ITIM lib directory
DSML2=$LIB/antlr-2.7.2.jar:$LIB/castor-0.9.4.3-xml.jar:$LIB/dsml2.jar:$LIB/regexp.jar
AGENTLIB=$LIB/enroleagent.jar
SSLLIBS=$LIB/sslj.jar:$LIB/jsafe.jar
JNDILIBS=$LIB/ldapjdk.jar
XMLPARSER=$LIB/xerces.jar
LABELS=data
CLASSPATH=.:$DSML2:$AGENTLIB:$SSLLIBS:$JNDILIBS:$XMLPARSER:$LABELS:$EXAMPLES

# system properties for https protocol implementation
# These may be used directly for WAS but need modification for Weblogic
TRUSTSTORE=$CERT_HOME/DummyServerTrustFile.jks
PROPS="-Djavax.net.ssl.trustStore=$TRUSTSTORE -Djavax.net.ssl.trustStorePassword=WebAS"

$JAVA -classpath $CLASSPATH $PROPS examples.serviceprovider.dsml2.DSML2DirContextTest $* 

