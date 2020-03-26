#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#============================================================================
#
# Update all the resource files for i18n
# see remarks in i18n.pl
#
#
p=`dirname $0`
BIN=`realpath $p/i18n.pl`
PKG=de/ipb_halle/lbac/i18n
SRC=`realpath $p/../../ui/src/main/resources/$PKG`

LANGUAGES="de"

sort -f -o $SRC/messages_en.properties $SRC/messages_en.properties

for i in $LANGUAGES ; do
        $BIN $SRC/messages_en.properties $SRC/messages_$i.properties 
done

