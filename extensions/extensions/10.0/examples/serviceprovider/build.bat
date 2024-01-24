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
@rem set ANT_HOME_LIB=%VAR_ANT_LIB_HOME%
@rem Points to the default location of ANT_HOME_LIB in WAS 6.1
set ANT_HOME_LIB=%JAVA_HOME%\..\deploytool\itp\plugins\org.apache.ant_1.6.5\lib

set ANT_OPTS=-ms256m -mx512m

"%JAVA_HOME%\bin\java" %ANT_OPTS% -classpath "%ANT_HOME_LIB%/ant.jar;%ANT_HOME_LIB%/ant-launcher.jar;%JAVA_HOME%/lib/tools.jar;." org.apache.tools.ant.Main -Ditim.home="%ITIM_HOME%" %1 %2 %3 %4 %5 %6 %7 %8 %9

@endlocal