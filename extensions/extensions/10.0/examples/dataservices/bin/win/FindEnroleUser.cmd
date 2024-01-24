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
set WAS_HOME=%DEFINED_APP_SRV_HOME%
set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%

set CP=%ITIM_HOME%\lib\fesi.jar
set CP=%CP%;%ITIM_HOME%\data
set CP=%CP%;%ITIM_HOME%\lib\jlog.jar
set CP=%CP%;%ITIM_HOME%\lib\jffdc.jar
set CP=%ITIM_HOME%\lib\enroleagent.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_common.jar;%CP%
set CP=%ITIM_HOME%\lib\com.ibm.cv.kmip.ext.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server.jar;%CP%
set CP=%WAS_HOME%\lib\j2ee.jar;%CP%
set CP=%ITIM_HOME%\lib\aspectjrt.jar;%CP%
set CP=%CP%;%ITIM_HOME%\extensions\{RELEASE_VERSION}\lib\examples.jar

echo on

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.dataservices.FindEnroleUser %1