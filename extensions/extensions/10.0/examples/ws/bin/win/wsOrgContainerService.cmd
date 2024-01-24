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

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.ws.WSOrganizationalContainerServiceClient %*

@endlocal