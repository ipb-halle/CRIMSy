#!/bin/bash
#
# Purge logfiles older than 7 days to 
# please the DSGVO
#
TOMCAT_HOME=/usr/local/tomee

find $TOMCAT_HOME/logs -type f -mtime +7 -exec rm -f {} \;

