@echo off

@rem ********************************************************************
@rem *
@rem * Licensed Materials - Property of IBM
@rem *
@rem * Source File Name = setEnv.bat
@rem *
@rem * (C) COPYRIGHT IBM Corp. 2003, 2009 All Rights Reserved
@rem *
@rem * US Government Users Restricted Rights - Use, duplication or
@rem * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
@rem *
@rem ********************************************************************

set APP_SRV_HOME=%DEFINED_APP_SRV_HOME%
set APP_SRV_TYPE=%DEFINED_APP_SRV_TYPE%
set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%

set ITIM_USER="itim manager"
set ITIM_PSWD=secret

set CP=%JAVA_HOME%/jre/lib/ext/jaas.jar
set CP=%CP%;../../../../lib/examples.jar

:wasSetup
  call "%APP_SRV_HOME%\bin\setupCmdLine.bat"
  set APP_SRV_TOP=%APP_SRV_HOME%/../..
  set SYSTEM_PROPERTIES="-Xbootclasspath/p:%WAS_BOOTCLASSPATH%" -Djava.ext.dirs="%JAVA_HOME%\lib\ext;%JAVA_HOME%\jre\lib\ext;%WAS_EXT_DIRS%;%WAS_HOME%\plugins;%WAS_HOME%\lib\WMQ\java\lib" -Djava.naming.provider.url=%APP_SRV_URL% -Djava.naming.factory.initial=com.ibm.websphere.naming.WsnInitialContextFactory -Dserver.root="%WAS_HOME%" -Ditim.home="%ITIM_HOME%" "%CLIENTSAS%" "%CLIENTSSL%" %USER_INSTALL_PROP% -Djava.util.logging.manager=com.ibm.ws.bootstrap.WsLogManager -Djava.util.logging.configureByServer=true 
  set LOGIN_CONFIG=../jaas_login_was.conf
  set PLATFORM_CONTEXT_FACTORY=com.ibm.itim.apps.impl.websphere.WebSpherePlatformContextFactory
  set CP=%CP%;%ITIM_HOME%/lib/api_ejb.jar
  set CP=%CP%;%ITIM_HOME%/lib/itim_api.jar
  set CP=%CP%;%ITIM_HOME%/lib/itim_server_api.jar
  set CP=%CP%;%ITIM_HOME%/lib/jlog.jar
  set CP=%CP%;%ITIM_HOME%/lib/com.ibm.cv.kmip.ext.jar
  set CP=%CP%;%ITIM_HOME%/data
  set CP=%CP%;%APP_SRV_TOP%/plugins/com.ibm.ws.runtime_6.1.0.jar
  set CP=%CP%;%APP_SRV_TOP%/plugins/com.ibm.ws.emf_2.1.0.jar
  set CP=%CP%;%APP_SRV_TOP%/plugins/com.ibm.ws.wccm_6.1.0.jar
  set CP=%CP%;%APP_SRV_TOP%/plugins/com.ibm.ws.ejbportable_6.1.0.jar
  set CP=%CP%;%APP_SRV_TOP%/runtimes/com.ibm.ws.webservices.thinclient_6.1.0.jar

goto end

:end