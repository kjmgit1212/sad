@echo off
@setlocal
rem /******************************************************************************
rem  *
rem  * Licensed Materials - Property of IBM
rem  *
rem  * (C) COPYRIGHT IBM Corp. 2003, 2012
rem  *
rem  * US Government Users Restricted Rights - Use, duplication or
rem  * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
rem  *
rem  *****************************************************************************/

call setEnv.cmd

rem This example fetches WSSearchDataService WSDL from ITIM server. 
rem Typical WSDL location on ITIM server is
rem http://HOST:PORT/itim/services/WSSearchDataServiceService/WEB-INF/wsdl/WSSearchDataService.wsdl
rem Add host name of WSDL location
set HOST=localhost
rem Add port of WSDL location
set PORT=9080

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.ws.WSSearchDataServiceClient -host=%HOST% -port=%PORT% %*

@endlocal