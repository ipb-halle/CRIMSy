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
package de.ipb_halle.lbac.forum.topics;

import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.webservice.WebRequest;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TopicsWebRequest extends WebRequest {

    private List<Topic> topics = new ArrayList<>();
    private Cloud cloud;

    public Cloud getCloud() {
        return cloud;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

}
