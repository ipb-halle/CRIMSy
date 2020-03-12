#!/bin/bash
#
#  LBAC Solr Container
#
#  Leibniz Bioactives Cloud
#  Copyright 2017, 2018, 2019 Leibniz-Institut f. Pflanzenbiochemie
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
#
#
#====================================================================
#

function setupPublic {
        sleep 30
        /opt/solr/bin/solr create_core -c public -d /data/solr/configsets/lbac
}

if test "!" -d /data/solr ; then
        echo "Directory /data/solr not found!"
        exit 1
fi

if test "!" -f /data/solr/solr.xml ; then
        echo "Installing new solr.xml"
        cp /opt/solr/server/solr/solr.xml /data/solr
fi

if test "!" -d /data/solr/configsets ; then
        echo "configsets not found; create default lbac conf ..."
        mkdir -p /data/solr/configsets/lbac/
        cp -r /install/configsets/lbac/conf /data/solr/configsets/lbac
fi

if test -d /install/configsets/lbac ; then
        echo "update managed solr schema configuration for template"
        cp -uv /install/configsets/lbac/* /data/solr/configsets/lbac/conf/
        echo "update managed solr schema configuration for all collections ..."
        find /data -name managed-schema -type f -exec cp -uv /install/configsets/lbac/conf/managed-schema {} \;
        echo "please reIndex all collections!"
fi

if test "!" -d /data/solr/public ; then
        echo "Public collection not found; scheduling create_core"
        setupPublic &
fi

/opt/solr/bin/solr -f -s /data/solr 

