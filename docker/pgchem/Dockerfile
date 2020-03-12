#
#  Leibniz Bioactives Cloud
#  Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
#====================================================================
#
# Docker container with PostgreSQL 9.6, OpenBabel 2.4.1 and pgchem_tigress. 
# Please use the following statement to install pgchem_tigress into your 
# database:
#
#   CREATE EXTENSION IF NOT EXISTS "pgchem_tigress";
#
# To build the image run "docker build -t pgchem" in this directory
#
FROM postgres:9.6


#
# install postgresql source repository
#
RUN set -x ; \
  echo "deb-src http://apt.postgresql.org/pub/repos/apt/ jessie-pgdg main $PG_MAJOR" > /etc/apt/sources.list.d/pgdg-src.list; \
  apt-get update

#
# save installation state
# install tools (wget, unzip, cmake, ...) 
# install development tools
# download openbabel, pgchem
# configure, build and install openbabel
# build and install pgchem (incl. libbarsoi.so)
#   --> this includes minor modification to openbabel
# purge unneeded packages / clean up
# silence complaints about missing dictionary.txt 
#   --> empty file, see: https://github.com/sneumann/pgchem/blob/master/setup/tigressdata.zip
#
# line(58) && ln -s .../md5.h fix a build problem in pgchem_tigress against postgresql 9.x
#
RUN set -x \
  && mkdir /opt/pgchem \
  && cd /opt/pgchem \
  && apt-mark showmanual > apt-mark.manual \
  && apt-get install -y --force-yes --no-upgrade postgresql-server-dev-$PG_MAJOR "libpq5=$PG_MAJOR*" "libpq-dev=$PG_MAJOR*" \
  && apt-get build-dep -y --no-upgrade libpq-dev \
  && apt-get install -y --no-install-recommends ca-certificates wget unzip cmake zlib1g-dev apt-utils \
  && rm -rf /var/lib/apt/lists/* \
  && ln -s  /usr/include/postgresql/9.6/server/libpq/md5.h  /usr/include/postgresql/9.6/server/common/md5.h \
  && wget -O /opt/pgchem/openbabel-2-4-1.tar.gz https://github.com/openbabel/openbabel/archive/openbabel-2-4-1.tar.gz \
  && wget -O /opt/pgchem/pgchem.zip https://github.com/ergo70/pgchem_tigress/archive/master.zip \
  && tar -xzf openbabel-2-4-1.tar.gz \
  && cd /opt/pgchem/openbabel-openbabel-2-4-1 \
  && mkdir build \
  && cd build \
  && cmake .. \
  && NUMCPU=`lscpu -e | grep yes | wc -l` \
  && make -j $NUMCPU \
  && make install \
  && cd /opt/pgchem \
  && unzip pgchem.zip \
  && cd /opt/pgchem/pgchem_tigress-master/src \
  && mv openbabel-2.4.1 openbabel-2.4.1-orig \
  && ln -s /opt/pgchem/openbabel-openbabel-2-4-1 openbabel-2.4.1 \
  && mv openbabel-2.4.1/include/openbabel/locale.h openbabel-2.4.1/include/openbabel/_locale.h \
  && cd barsoi \
  && make -f Makefile.linux \
  && cp libbarsoi.so /usr/local/lib \
  && cd .. \
  && export USE_PGXS=1 \
  && make -j $NUMCPU \
  && make install \
  && cd /opt/pgchem \
  && apt-mark showmanual | xargs -l1 apt-mark auto \
  && cat apt-mark.manual | xargs -l1 apt-mark manual \
  && apt-get purge -y --auto-remove \
  && cd /opt \
  && rm -r pgchem \
  && touch /usr/local/share/openbabel/2.4.1/dictionary.txt 

