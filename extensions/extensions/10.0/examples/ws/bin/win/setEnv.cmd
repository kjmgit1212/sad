@echo off
rem /******************************************************************************
rem  *
rem  * Licensed Materials - Property of IBM
rem  *
rem  * (C) COPYRIGHT IBM Corp. 2012
rem  *
rem  * US Government Users Restricted Rights - Use, duplication or
rem  * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
rem  *
rem  *****************************************************************************/
set APP_SRV_HOME=%DEFINED_APP_SRV_HOME%
set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%

rem /********************************************************
rem * If the APP_SRV_HOME is set to custom profile location, then the following
rem * variable needs to be modified to have the correct WebSphere application 
rem * server home directory.
rem /********************************************************
set APP_SRV_TOP=%APP_SRV_HOME%\..\..

set CP=%ITIM_HOME%\lib\itim_ws_client.jar;
set CP=%ITIM_HOME%\lib\itim_ws_model.jar;%CP%
set CP=%APP_SRV_TOP%\runtimes\com.ibm.jaxws.thinclient_7.0.0.jar;%CP%
set CP=%CP%;%ITIM_HOME%\extensions\{RELEASE_VERSION}\lib\examples.jar

