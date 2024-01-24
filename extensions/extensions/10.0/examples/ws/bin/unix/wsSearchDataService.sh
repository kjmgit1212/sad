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

# This example fetches WSSearchDataService WSDL from ITIM server. 
# Typical WSDL location on ITIM server is
# http://HOST:PORT/itim/services/WSSearchDataServiceService/WEB-INF/wsdl/WSSearchDataService.wsdl
# Add host name of WSDL location
HOST=localhost
# Add port of WSDL location
PORT=9080

$JAVA_HOME/bin/java -classpath $CP examples.ws.WSSearchDataServiceClient -host=$HOST -port=$PORT "$@"