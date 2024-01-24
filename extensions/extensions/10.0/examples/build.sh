#!/bin/sh

#/******************************************************************************
# *
# * Licensed Materials - Property of IBM
# *
# * Source File Name = build.sh
# *
# * (C) COPYRIGHT IBM Corp. 1999, 2012 All Rights Reserved
# *
# * US Government Users Restricted Rights - Use, duplication or
# * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
# *
# *****************************************************************************/

export WAS_HOME

"$WAS_HOME"/bin/ws_ant.sh -Dwas.home="$WAS_HOME" $1 $2 $3 $4 $5 $6 $7 $8 $9
