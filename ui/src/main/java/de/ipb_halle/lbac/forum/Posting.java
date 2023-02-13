/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.forum;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.Obfuscatable;
import de.ipb_halle.lbac.admission.User;
import java.io.Serializable;
import java.util.Date;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author fmauz
 */
public class Posting implements Serializable, Comparable<Posting>, DTO,Obfuscatable {

    private final static long serialVersionUID = 1L;

    private Integer id;

    private Topic topic;

    private String text;

    private User owner;

    private Date created;

    public Posting() {
    
    }

    /**
     * Constructor
     * @param entity
     * @param owner
     * @param t 
     */
    public Posting(PostingEntity entity, User owner, Topic t) {
        this.id = entity.getId();
        this.created = entity.getCreated();
        this.text = entity.getText();
        this.owner = owner;
        this.topic = t;
    }
    
     @Override
    public PostingEntity createEntity() {
        return new PostingEntity()
                .setCreated(created)
                .setId(id)
                .setOwner(owner.getId())
                .setText(text)
                .setTopic(topic.getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Topic getTopic() {
        return topic;
    }

    @XmlTransient
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getUserTag() {
        return owner.getName() + "@" + owner.getNode().getInstitution();
    }

    @Override
    public int compareTo(Posting o) {
        if (o.getCreated().getTime() > this.created.getTime()) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public void obfuscate() {
       owner.obfuscate();
       
    }

   

}
