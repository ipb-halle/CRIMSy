#!/bin/bash
#
# Leibniz Bioactives Cloud Source Code Statistics
# Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie 
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
TOTALFILES=0
TOTALLINES=0
echo "      Type     Files  Lines"
echo "----------------------------"
for i in "java" "xhtml" "m4" "xml" "sh" "sql" "txt" "properties" "js" "css"; do
	numfiles=`find . -type f -name "*.$i" -not -path "*/target/*" | wc -l`
	numlines=`find . -type f -name "*.$i" -not -path "*/target/*" -exec cat {} \; | wc -l`
	TOTALFILES=$(($TOTALFILES + numfiles))
	TOTALLINES=$(($TOTALLINES + numlines))
	printf "% 12s:  % 4d  % 6d\n" $i $numfiles $numlines 
done 
echo "----------------------------"
printf "% 12s:  % 4d  % 6d \n" "TOTAL" $TOTALFILES $TOTALLINES 
echo "============================"

