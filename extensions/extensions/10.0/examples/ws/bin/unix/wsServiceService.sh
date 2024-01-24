#!/bin/sh

#********************************************************************
#*  Licensed Materials - Property of IBM
#*  
#*  (c) Copyright IBM Corp.  2003, 2012 All Rights Reserved
#*  
#*  US Government Users Restricted Rights - Use, duplication or
#*  disclosure restricted by GSA ADP Schedule Contract with
#*  IBM Corp.
#********************************************************************
. ./setEnv.sh

$JAVA_HOME/bin/java -classpath $CP examples.ws.WSServiceServiceClient "$@"
