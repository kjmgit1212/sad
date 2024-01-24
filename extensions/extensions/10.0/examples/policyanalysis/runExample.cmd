@echo off
rem /******************************************************************************
rem  *
rem  * Licensed Materials - Property of IBM
rem  *
rem  * (C) COPYRIGHT IBM Corp. 2003, 2009
rem  *
rem  * US Government Users Restricted Rights - Use, duplication or
rem  * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
rem  *
rem  *****************************************************************************/


set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%
set WAS_HOME=%DEFINED_APP_SRV_HOME%
set JDBC_DRIVER_JARS=%VAR_DB2_JDBC_DRIVER_PATH%

set CP=%ITIM_HOME%\data;
set CP=%ITIM_HOME%\lib\enroleagent.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_common.jar;%CP%
set CP=%ITIM_HOME%\lib\com.ibm.cv.kmip.ext.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server.jar;%CP%
set CP=%ITIM_HOME%\lib\ibmjs.jar;%CP%
set CP=%ITIM_HOME%\lib\bsf23.jar;%CP%
set CP=%ITIM_HOME%\lib\jlog.jar;%CP%
set CP=%ITIM_HOME%\lib\jffdc.jar;%CP%
set CP=%ITIM_HOME%\extensions\{RELEASE_VERSION}\lib\examples.jar;%CP%
set CP=%IWAS_HOME%\lib\j2ee.jar;%CP%
set CP=%ITIM_HOME%\lib\aspectjrt.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.runtime_6.1.0.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.emf_2.1.0.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.wccm_6.1.0.jar;%CP%
set CP=%WAS_HOME%/../../runtimes/com.ibm.ws.webservices.thinclient_6.1.0.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.runtime.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.emf.jar;%CP%
set CP=%WAS_HOME%/../../plugins/com.ibm.ws.wccm.jar;%CP%
set CP=%WAS_HOME%/../../runtimes/com.ibm.ws.webservices.thinclient_7.0.0.jar;%CP%
set CP=%WAS_HOME%/../../runtimes/com.ibm.ws.admin.client_7.0.0.jar;%CP%
set CP=%JDBC_DRIVER_JARS%;%CP%

set CP=..\..\..;%CP%
set CP=.;%CP%

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.policyanalysis.ProvisioningPolicyAnalysisExample %1 %2 %3 %4
