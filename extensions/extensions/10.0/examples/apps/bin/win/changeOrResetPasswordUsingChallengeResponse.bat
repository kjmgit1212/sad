@echo off

@rem ********************************************************************
@rem *
@rem * Licensed Materials - Property of IBM
@rem *
@rem * Source File Name = changePerson.bat
@rem *
@rem * (C) COPYRIGHT IBM Corp. 2003, 2009 All Rights Reserved
@rem *
@rem * US Government Users Restricted Rights - Use, duplication or
@rem * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
@rem *
@rem ********************************************************************

call setEnv.bat

set OPTS=-ms256m -mx512m

"%JAVA_HOME%/bin/java" %OPTS% %SYSTEM_PROPERTIES% -cp "%CP%" -Djava.security.auth.login.config=%LOGIN_CONFIG% -Dapps.context.factory=%PLATFORM_CONTEXT_FACTORY% -Ditim.user=%ITIM_USER% -Ditim.pswd=%ITIM_PSWD% examples.api.ChangeOrResetPasswordUsingChallengeResponse %*

@endlocal