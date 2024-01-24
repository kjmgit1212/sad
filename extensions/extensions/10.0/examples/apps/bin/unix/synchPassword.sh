#!/bin/sh

#/******************************************************************************
# *
# * Licensed Materials - Property of IBM
# *
# * Source File Name = synchPassword.sh
# *
# * (C) COPYRIGHT IBM Corp. 2003, 2009 All Rights Reserved
# *
# * US Government Users Restricted Rights - Use, duplication or
# * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
# *
# *****************************************************************************/

# Export child proccess vars w/o forking...
. ./setEnv.sh

OPTS="-ms256m -mx512m"
export OPTS

$JAVA_HOME/bin/java $OPTS $SYSTEM_PROPERTIES -cp $CP -Djava.security.auth.login.config=$LOGIN_CONFIG -Dapps.context.factory=$PLATFORM_CONTEXT_FACTORY -D"itim.user=$ITIM_USER" -Ditim.pswd=$ITIM_PSWD examples.api.SynchPassword "$@"
