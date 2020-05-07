/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.webservice;

import de.ipb_halle.lbac.announcement.membership.MembershipWebService;
import de.ipb_halle.lbac.collections.CollectionWebService;
import de.ipb_halle.lbac.device.job.JobWebService;
import de.ipb_halle.lbac.forum.postings.PostingWebService;
import de.ipb_halle.lbac.forum.topics.TopicsWebService;
import de.ipb_halle.lbac.search.document.SearchWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class ApplicationConfig extends Application {

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(
                CloudNodeWebService.class,
                CollectionWebService.class,
                SearchWebService.class,
                MembershipWebService.class,
                WordCloudWebService.class,
                TopicsWebService.class,
                PostingWebService.class,
                JobWebService.class,
                SimpleRESTPojoExample.class));
    }
}
