@echo off
rem /******************************************************************************
rem  *
rem  * Licensed Materials - Property of IBM
rem  *
rem  * (C) COPYRIGHT IBM Corp. 2003, 2007
rem  *
rem  * US Government Users Restricted Rights - Use, duplication or
rem  * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
rem  *
rem  *****************************************************************************/

set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%

set CP=..\..\
set CP=%CP%;..\..\lib\examples.jar
set CP=%CP%;%ITIM_HOME%\data
set CP=%CP%;%ITIM_HOME%\lib\enroleagent.jar
set CP=%CP%;%ITIM_HOME%\lib\itim_api.jar
set CP=%CP%;%ITIM_HOME%\lib\itim_common.jar
set CP=%CP%;%ITIM_HOME%\lib\com.ibm.cv.kmip.ext.jar
set CP=%CP%;%ITIM_HOME%\lib\itim_server.jar
set CP=%CP%;%ITIM_HOME%\lib\itim_server_api.jar
set CP=%CP%;%ITIM_HOME%\lib\jlog.jar
set CP=%CP%;%ITIM_HOME%\lib\regexp.jar

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.passwordrules.ExampleRunner %1 %2 %3 %4
