#!/bin/bash
#
# CRIMSY Agency
# Job Management Script Template
#
#  Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie 
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
#
# Note:
# -----
# This template illustrates, how the CRIMSy Agency could be used 
# to process different types of jobs (e.g. PRINT jobs, COMPUTE jobs, 
# etc.). The script will receive the job type as first argument,
# the queue as second argument and input data on STDIN. Results 
# are expected via STDOUT. 
#
# Administrators are expected to modify and extend this script to 
# their needs. The script is not intended for manual calling although
# it is possible, e.g. for debugging purposes.
#
#====================================================================
#
function compute {
    echo "Scheduling compute job for queue $1"
    cat - | md5sum
}

function print {
    lp -d $1
}
#
#====================================================================
#
    case $1 in

        PRINT)
            print $2
            ;;

        COMPUTE)
            compute $2
            ;;
    esac
