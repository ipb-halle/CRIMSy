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

import de.ipb_halle.lbac.util.RichTextConverter;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.service.CloudService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Manages the actions from the socialForum.xhtml
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ForumBean implements Serializable {

    private String postingText;
    private ForumSearchState searchState = new ForumSearchState();
    private Topic activeTopic;
    private Logger logger = LogManager.getLogger(ForumBean.class);
    private String fullTextSearch = "";
    protected RichTextConverter vFilter;
    protected final String TOPIC_HEADER_PRESTRING = "Topic: ";

    @Inject
    private CloudService cloudService;

    @Inject
    private ForumOrchestrator remoteTopicOrchestrator;

    @Inject
    private ForumService forumService;

    private String cloudOfTopic;
    private User currentUser;
    private Set<String> availableClouds;

    private String newTopicName;

    @PostConstruct
    public void init() {
        vFilter = new RichTextConverter();
    }

    /**
     *
     *
     *
     * @param evt the LoginEvent scheduled by UserBean
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        availableClouds = new HashSet<>();
        for (Cloud c : cloudService.load()) {
            availableClouds.add(c.getName());
        }

        cloudOfTopic = availableClouds.iterator().next();
        refreshForumState();
    }

    /**
     * Triggers a new search for local and remote topics with their postings
     */
    public void refreshForumState() {
        Integer idOfActiveTopic = null;
        if (activeTopic != null) {
            idOfActiveTopic = activeTopic.getId();
        }

        fetchLocalTopics();

        remoteTopicOrchestrator.startRemoteSearch(
                searchState,
                currentUser);

        restoreActiveTopicFromId(idOfActiveTopic);
    }

    /**
     * Adds a new Posting to the active topic
     */
    public void addPostingToActiveTopic() {
        if (activeTopic == null || postingText.trim().isEmpty()) {
            return;
        }

        Date creationDate = new Date();

        try {
            forumService.addPostingToTopic(
                    activeTopic,
                    vFilter.filter(postingText),
                    currentUser, creationDate);
        } catch (Exception e) {
            logger.error("addPostingToActiveTopic() caught an exception:", (Throwable) e);
        }
        postingText = "";
    }

    /**
     * Creates a new topic on the local node
     */
    public void createNewTopic() {
        try {
            forumService.createNewTopic(
                    newTopicName.trim(),
                    TopicCategory.OTHER,
                    currentUser,
                    cloudOfTopic);
        } catch (Exception e) {
            logger.error("createNewTopic() caught an exception:", (Throwable) e);
        }
        newTopicName = "";
        refreshForumState();
    }

    public void chooseActiveTopic(Topic t) {
        this.activeTopic = t;
    }

    /**
     * Returns all readable topics for the view. If the fulltext search is
     * active, topics which postings does not contain the keyword will be
     * excluded
     *
     * @return
     */
    public List<Topic> getAllReadableTopics() {
        if (fullTextSearch.trim().isEmpty()) {
            Collections.sort(searchState.getReadableTopics());
            return searchState.getReadableTopics();
        } else {
            List<Topic> topicsWithSearchTerm = new ArrayList<>();
            for (Topic t : searchState.getReadableTopics()) {
                for (Posting p : t.getPostings()) {
                    if (p.getText().toLowerCase().contains(fullTextSearch.toLowerCase())) {
                        if (!topicsWithSearchTerm.contains(t)) {
                            topicsWithSearchTerm.add(t);
                        }
                    }
                }
            }
            if (!topicsWithSearchTerm.contains(activeTopic)) {
                activeTopic = null;
            }
            Collections.sort(topicsWithSearchTerm);
            return topicsWithSearchTerm;
        }
    }

    /**
     * Returns all Postings of the active Topic. The postings are sorted with
     * earliest creationdate at first position, latest posting at last position.
     *
     * @return all postings from active topic. Empty list if there is no active
     * topic.
     */
    public List<Posting> getPostsOfActiveTopic() {

        if (activeTopic == null || activeTopic.getPostings().isEmpty()) {
            return new ArrayList<>();
        }
        Collections.sort(activeTopic.getPostings());
        return activeTopic.getPostings();
    }

    /**
     * Returns the permission to create a posting in a topic. Public users are
     * never allowed to post.
     *
     * @return
     */
    public boolean isPostingAllowed() {
        if (currentUser == null || activeTopic == null) {
            return false;
        }
        if (currentUser.isPublicAccount()) {
            return false;
        }
        return activeTopic.isEditable();
    }

    /**
     * Returns the permission to create a new topic. Public users are never
     * allowed.
     *
     * @return
     */
    public boolean isCreationOfNewTopicAllowed() {
        return !(currentUser == null
                || currentUser.isPublicAccount());
    }

    /**
     * Returns the header for the Topic with a predefined startstring
     *
     * @return
     */
    public String getTopicName() {
        if (activeTopic == null) {
            return "";
        } else {
            return TOPIC_HEADER_PRESTRING + activeTopic.getName();
        }
    }

    public Set<String> getAvailableClouds() {
        return availableClouds;
    }

    private void restoreActiveTopicFromId(Integer id) {
        if (id != null) {
            for (Topic t : searchState.getReadableTopics()) {
                if (Objects.equals(t.getId(), id)) {
                    activeTopic = t;
                }
            }
        }
    }

    private void fetchLocalTopics() {
        // Local search
        List<Topic> foundTopics = new ArrayList<>();
        List<Cloud> clouds = cloudService.load();
        for (Cloud c : clouds) {
            foundTopics.addAll(
                    forumService.loadReadableTopics(currentUser, c));
        }
        for (Topic t : foundTopics) {
            searchState.setReadableTopics(
                    forumService.upsertTopicList(t, searchState.getReadableTopics())
            );
        }
    }

    // Boilerplate Getter and Setter
    public String getPostingText() {
        return postingText;
    }

    public void setPostingText(String postingText) {
        this.postingText = postingText;
    }

    public Topic getActiveTopic() {
        return activeTopic;
    }

    public void setActiveTopic(Topic activeTopic) {
        this.activeTopic = activeTopic;
    }

    public void setAvailableClouds(Set<String> availableClouds) {
        this.availableClouds = availableClouds;
    }

    public String getNewTopicName() {
        return newTopicName;
    }

    public void setNewTopicName(String newTopicName) {
        this.newTopicName = newTopicName;
    }

    public void triggerNewSearch() {
        refreshForumState();
    }

    public String getFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(String fullTextSearch) {
        this.fullTextSearch = fullTextSearch;
    }

    public String getCloudOfTopic() {
        return cloudOfTopic;
    }

    public void setCloudOfTopic(String cloudOfTopic) {
        this.cloudOfTopic = cloudOfTopic;
    }

}
