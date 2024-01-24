@echo off
@setlocal

@rem ********************************************************************
@rem *
@rem * Licensed Materials - Property of IBM
@rem *
@rem * Source File Name = build.bat
@rem *
@rem * (C) COPYRIGHT IBM Corp. 1999, 2007 All Rights Reserved
@rem *
@rem * US Government Users Restricted Rights - Use, duplication or
@rem * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
@rem *
@rem ********************************************************************

set JAVA_HOME=%DEFINED_JAVA_HOME%
set ITIM_HOME=%DEFINED_ITIM_HOME%
set APP_SERVER_DEPLOY_DIR=%VAR_APP_SERVER_DEPLOY_DIR%
set HTML_ROOT=%VAR_HTML_ROOT%
set JDBC_DRIVER_JAR=%VAR_DB2_JDBC_DRIVER_PATH%
set ANT_HOME_LIB=%VAR_ANT_LIB_HOME%

set COMPILER=modern
set EJBCCOMPILER=javac
set DEBUG=on
set OPTIMIZE=off
set DEPRECATION=on
set VERBOSE=off
set FAILONERROR=yes
set ANT_OPTS=-ms256m -mx512m

"%JAVA_HOME%\bin\java" %ANT_OPTS% -classpath "%ANT_HOME_LIB%/ant.jar;%ANT_HOME_LIB%/ant-launcher.jar;%JAVA_HOME%/lib/tools.jar;%JDBC_DRIVER_JAR%;." org.apache.tools.ant.Main -Ditim.home="%ITIM_HOME%" -Dapp.deploy="%APP_SERVER_DEPLOY_DIR%" -Dhtml.root="%HTML_ROOT%" %1 %2 %3 %4 %5 %6 %7 %8 %9

@endlocal

