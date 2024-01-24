@echo off

@rem #********************************************************************
@rem #*  Licensed Materials - Property of IBM
@rem #*  
@rem #*  (c) Copyright IBM Corp.  2003, 2009 All Rights Reserved
@rem #*  
@rem #*  US Government Users Restricted Rights - Use, duplication or
@rem #*  disclosure restricted by GSA ADP Schedule Contract with
@rem #*  IBM Corp.
@rem #********************************************************************
set WAS_HOME=%DEFINED_APP_SRV_HOME%
set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%

set CP=%ITIM_HOME%\data
set CP=%ITIM_HOME%\lib\jlog.jar;%CP%
set CP=%ITIM_HOME%\lib\jffdc.jar;%CP%
set CP=%ITIM_HOME%\lib\enroleagent.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_common.jar;%CP%
set CP=%ITIM_HOME%\lib\com.ibm.cv.kmip.ext.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_api.jar;%CP%
set CP=%ITIM_HOME%\lib\itim_server.jar;%CP%
set CP=%WAS_HOME%\lib\j2ee.jar;%CP%
set CP=%ITIM_HOME%\lib\aspectjrt.jar;%CP%
set CP=%ITIM_HOME%\extensions\{RELEASE_VERSION}\lib\examples.jar;%CP%

"%JAVA_HOME%\bin\java" -classpath "%CP%" examples.dataservices.BusinessUnitLoader
