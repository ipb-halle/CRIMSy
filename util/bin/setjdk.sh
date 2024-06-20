#!/usr/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Integration Test Setup Script
#
# Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie 
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#==========================================================
#
# Select the JDK version 
# ----------------------
# Unfortunately JDK installation paths are not standardized 
# among different Linux distributions
#

case "$1" in
    jdk8|JDK8)
            for JAVA_HOME in /usr/lib/jvm/temurin-8-jdk-amd64 \
                    /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0 \
                    ;do
                if [ -d $JAVA_HOME ] ; then
                    export JAVA_HOME
                    alias java=$JAVA_HOME/bin/java
                    return
                fi
            done
            ;;
    jdk17|JDK17)
            for JAVA_HOME in /usr/lib/jvm/java-17-openjdk-amd64 \
                    /usr/lib64/jvm/java-17-openjdk-17 \
                    ; do
                if [ -d $JAVA_HOME ] ; then
                    export JAVA_HOME
                    alias java=$JAVA_HOME/bin/java
                    return
                fi
            done
            ;;
esac
