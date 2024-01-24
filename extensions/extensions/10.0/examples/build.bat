@echo off
@setlocal

@rem ********************************************************************
@rem *
@rem * Licensed Materials - Property of IBM
@rem *
@rem * Source File Name = build.bat
@rem *
@rem * (C) COPYRIGHT IBM Corp. 1999, 2012 All Rights Reserved
@rem *
@rem * US Government Users Restricted Rights - Use, duplication or
@rem * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
@rem *
@rem ********************************************************************

"%WAS_HOME%"\bin\ws_ant.bat -Dwas.home="%WAS_HOME%" %1 %2 %3 %4 %5 %6 %7 %8 %9

@endlocal
