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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.postings.PostingWebClient;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class ForumService implements Serializable {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private NodeService nodeService;

    @Inject
    private ACListService acListService;

    @Inject
    private MemberService memberService;

    @Inject
    private PostingWebClient client;

    private Logger logger = LogManager.getLogger(ForumService.class);

    private ACList publicReadEditAcl;

    @PostConstruct
    public void init() {
        publicReadEditAcl = getPublicReadWriteList();
    }

    /**
     *
     * @param u
     * @param cloud
     * @return
     */
    public List<Topic> loadReadableTopics(User u, Cloud cloud) {
        List<Topic> readableTopics = new ArrayList<>();
        try {
            CriteriaBuilder builder = this.em.getCriteriaBuilder();
            CriteriaQuery<TopicEntity> criteriaQuery = builder.createQuery(TopicEntity.class);
            Root<TopicEntity> topicRoot = criteriaQuery.from(TopicEntity.class);
            criteriaQuery.select(topicRoot);

            List<TopicEntity> topics = this.em.createQuery(criteriaQuery).getResultList();

            for (TopicEntity entity : topics) {
                if (cloud.getName().equals(entity.getCloudName())) {
                    Topic topic = new Topic(
                            entity,
                            acListService.loadById(entity.getACList()),
                            memberService.loadUserById(entity.getOwner()),
                            nodeService.getLocalNode(),
                            cloud.getName()
                    );

                    if (acListService.isPermitted(ACPermission.permREAD, topic, u)
                            || topic.getOwner().getId().equals(u.getId())) {
                        topic.setEditable(acListService.isPermitted(ACPermission.permEDIT, topic, u));
                        topic.setPostings(loadPostingsOfTopic(topic));
                        readableTopics.add(topic);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e);
            return new ArrayList<>();
        }
        return readableTopics;
    }

    /**
     * Creates a posting with the given text and saves it to the db linked to
     * the given topic.
     *
     * NOTE: in case of network problems, the user might be not known to the
     * local node. In this case, the method will fail. Saving the user would
     * introduce other problems (overwriting of passwords, ...).
     *
     * @param t
     * @param text
     * @param u
     * @param d
     * @return topic inclusive the new posting
     */
    public Topic addPostingToTopic(Topic t, String text, User u, Date d) {
        try {
            Posting p = new Posting();
            p.setOwner(u);
            p.setTopic(t);
            p.setText(text);
            p.setCreated(d);
            t.getPostings().add(p);
            if (t.getNode().equals(this.nodeService.getLocalNode())) {
                em.merge(p.createEntity());
            } else {
                // any of the clouds is okay
                CloudNode cn = this.cloudNodeService.load(null, t.getNode()).get(0);
                client.announcePostingToRemoteNode(t, u, cn);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return t;
    }

    public List<Topic> getTopicsWithKeyWords(User u, String... keywords) {
        return new ArrayList<>();
    }

    /**
     * Creates a new Topic at the local node and sets the public readable aclist
     * as a default value
     *
     * @param topicName
     * @param category
     * @param owner
     * @param cloudName
     * @return the created and in db saved topic
     *
     */
    public Topic createNewTopic(
            String topicName,
            TopicCategory category,
            User owner,
            String cloudName) {
        Topic topic = new Topic(topicName, category);
        topic.setACList(getPublicReadWriteList());
        topic.setCloudName(cloudName);
        topic.setNode(nodeService.getLocalNode());
        topic.setOwner(owner);

        topic.setId(em.merge(topic.createEntity()).getId());

        return topic;
    }

    private List<Posting> loadPostingsOfTopic(Topic topic) {
        List<Posting> result = new ArrayList<>();

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<PostingEntity> criteriaQuery = builder.createQuery(PostingEntity.class);
        Root<PostingEntity> root = criteriaQuery.from(PostingEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("topic"), topic.getId()));
        for (PostingEntity entity : this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(
                    new Posting(
                            entity,
                            memberService.loadUserById(entity.getOwner()),
                            topic));
        }
        return result;

    }

    /**
     * loads the public aclist with READ and EDIT permissions. If the aclist
     * does not exists a new one will be created
     *
     * @return acl for public group with read and edit permissions
     * @throws Exception
     */
    public ACList getPublicReadWriteList() {
        if (publicReadEditAcl == null) {
            Group g = memberService.loadGroupById(
                    GlobalAdmissionContext.PUBLIC_GROUP_ID
            );
            ACList publicReadWrite = new ACList();
            publicReadWrite.setName("Public Read/Edit ACL");
            publicReadWrite.addACE(
                    g,
                    new ACPermission[]{
                        ACPermission.permREAD,
                        ACPermission.permEDIT}
            );
            publicReadEditAcl = acListService.save(publicReadWrite);
        }
        return publicReadEditAcl;
    }

    /**
     *
     * @param newTopic
     * @param oldTopics
     * @return
     */
    public List<Topic> upsertTopicList(Topic newTopic, List<Topic> oldTopics) {
        boolean isNew = true;
        ListIterator<Topic> li = oldTopics.listIterator();
        while (li.hasNext()) {
            Topic tmp = li.next();
            if (tmp.getId().equals(newTopic.getId())) {
                li.set(newTopic); // replace the last element returned by next()
                isNew = false;
            }
        }
        if (isNew) {
            oldTopics.add(newTopic);
        }

        return oldTopics;
    }
}
