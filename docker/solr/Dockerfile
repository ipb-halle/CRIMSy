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
FROM solr:6

#
WORKDIR /install

COPY ./solr/setup.sh /install/
COPY ./solr/configsets /install/configsets
# root permission needed
USER root
RUN chmod +x /install/setup.sh \
 && mkdir /home/solr \
 && chown solr /home/solr

USER solr:solr
CMD ["/bin/bash","-c","/install/setup.sh"]
