#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Update uncompressed version of reports from report file
#
# Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie 
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
p=`dirname $0`
LBAC_REPO=`realpath $p/../..`
RPT=`realpath $1`
DIR=`basename $RPT .prpt`

rm -rf $LBAC_REPO/util/reports/$DIR
mkdir -p $LBAC_REPO/util/reports/$DIR
pushd $LBAC_REPO/util/reports/$DIR > /dev/null
jar xf $RPT
popd >/dev/null

