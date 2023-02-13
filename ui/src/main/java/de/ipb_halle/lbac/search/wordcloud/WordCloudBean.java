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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.file.TermFrequency;
import de.ipb_halle.lbac.file.TermFrequencyList;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchState;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.CloudNodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.primefaces.model.tagcloud.TagCloudModel;

/**
 * Controller for the GUI. Handles all actions which can be done by the user.
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class WordCloudBean implements Serializable {

    private final static Long serialVersionUID = 1L;

    private Set<String> tagList = new HashSet<>();
    private String maxTerms = "300";
    private Logger logger = LogManager.getLogger(WordCloudBean.class);
    private TagCloudModel model = new DefaultTagCloudModel();
    private boolean wordCloudVisible = false;
    private WordTermCategoriser categoriser;
    private String searchTermInput = "";
    private WordTermOrganizer organizer = new WordTermOrganizer();
    private WordTermListMerger listMerger = new WordTermListMerger();
    private DocumentSearchState docSeachState;
    private List<Document> startingDocuments = new ArrayList<>();

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private DocumentSearchService searchService;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private TermVectorEntityService termVectorService;

    @Inject
    private WordCloudWebClient tempClient;

    @Inject
    private UserBean userBean;

    /**
     * Starts the word cloud functionality
     * <ul>
     * <li> clear all old oinformation from last searches</li>
     * <li> convert the text from inputfield to tags</li>
     * <li> get local docs</li>
     * <li> get remote docs</li>
     * <li> removes not matching docs. This can occure because the doc search is
     * solr based, the filtering java based</li>
     * <li> calculate the wordterms</li>
     * <li>create the wordcloud</li>
     * </ul>
     */
    public void startSearch() {
        startingDocuments.clear();
        tagList.clear();
        docSeachState = new DocumentSearchState();
        wordCloudVisible = true;
        model.clear();
        if (searchTermInput.trim().isEmpty()) {
            return;
        }
        try {
            convertInputStringToTags();

            getLocalDocsWithTermVector();

            getRemoteDocsWithTermVector();

            removeDocsWithoutTags();

            // Gets all Words from each docs and merges them. For every word
            // a calculation is done how many documents this word contain
            List<WordTerm> wordTerms = initialiseWordTerms();
            categoriser = new WordTermCategoriserComplex(docSeachState.getFoundDocuments().size());
            categoriser.categorise(wordTerms);

            startingDocuments = copyDocList(docSeachState.getFoundDocuments());

            createWordCloud(wordTerms);

        } catch (Exception e) {
            logger.error("startSearch() caught an exception:", (Throwable) e);
        }
    }

    /**
     * Updates the word cloud if a tag was clicked in the cloud
     */
    private void updateExistingCloud(String newSearchTag) {
        try {
            docSeachState.setFoundDocuments(copyDocList(startingDocuments));

            for (Document d : docSeachState.getFoundDocuments()) {
                String tag = d.getTermFreqList().getStemmedWordFor(newSearchTag);
                if (tag != null) {
                    tagList.add(tag);
                    break;
                }
            }
            removeDocsWithoutTags();

            List<WordTerm> remainingTerms = filterWordTerms(
                    docSeachState.getFoundDocuments(), tagList);

            for (WordTerm wt : remainingTerms) {
                wt.setDocsWithTerm(
                        getDocAmountWithAdditionalTag(wt.getTerm(),
                                docSeachState.getFoundDocuments()));
            }

            categoriser = new WordTermCategoriserComplex(docSeachState.getFoundDocuments().size());
            categoriser.categorise(remainingTerms);

            createWordCloud(remainingTerms);
        } catch (Exception e) {
            logger.error("updateExistingCloud() caught an exception:", (Throwable) e);
        }
    }

    /**
     * Removes a tag from tag list. If the tag is part of the user defined input
     * text it is removed from there if all tags are removed the state is
     * cleared
     *
     * @param tag
     */
    public void removeTag(String tag) {
        tagList.remove(tag);
        searchTermInput = searchTermInput.replace(tag, "").trim();
        if (tagList.isEmpty()) {
            clearCloudState();

        } else {
            updateExistingCloud(null);
        }
    }

    /**
     * Add a new tag to the taglist, recalculate the new documents and words and
     * create a new tagcloud
     *
     * @param event
     */
    public void onSelect(SelectEvent event) {
        TagCloudItem item = (TagCloudItem) event.getObject();
        String represantation = item.getLabel();

        updateExistingCloud(represantation);
    }

    /**
     * This method observes login events and removes all information/states from
     * previous searching
     */
    public void clearCloudState(@Observes LoginEvent evt) {
        clearCloudState();
    }

    /**
     * Removes all information/states from previous searching
     */
    public void clearCloudState() {
        startingDocuments.clear();
        model.clear();
        tagList.clear();
        if (docSeachState != null) {
            docSeachState.clearState();
        }
        wordCloudVisible = false;
        searchTermInput = "";
    }

    /**
     * Removes all wordTerms from the list for words which do not appear in any
     * document
     *
     * @param docs List of docs
     * @param tagList words to check
     * @return list with words. Every word appears at least once in the list of
     * docs
     */
    private List<WordTerm> filterWordTerms(List<Document> docs, Set<String> tagList) {
        List<WordTerm> remainingTerms = listMerger.mergeTerms(new ArrayList<>(), docs);

        for (int i = remainingTerms.size() - 1; i >= 0; i--) {
            if (tagList.contains(remainingTerms.get(i).getTerm())) {
                remainingTerms.remove(i);
            }
        }
        return remainingTerms;
    }

    /**
     * Checks the amount of documents if the tag would be part of the taglist
     *
     * @param tag
     * @param docs
     * @return
     * @throws Exception
     */
    private int getDocAmountWithAdditionalTag(
            String tag,
            List<Document> docs) throws Exception {
        int i = 0;
        for (Document d : docs) {
            if (d.getTermFreqList().containsWord(tag)) {
                i++;
            }
        }
        return i;
    }

    /**
     * loads all documents with the tags and adds the limited term vecor to them
     *
     * @throws Exception
     */
    private void getLocalDocsWithTermVector() throws Exception {
        searchService.actionStartDocumentSearch(
                docSeachState,
                collectionBean.getCollectionSearchState().getCollections(),
                searchService.getTagStringForSeachRequest(tagList),
                Integer.MAX_VALUE, 0,
                searchService.getUriOfPublicCollection());

        for (Document d : docSeachState.getFoundDocuments()) {
            d.setTermFreqList(
                    new TermFrequencyList(
                            termVectorService.getTermVector(
                                    Arrays.asList(d.getId()),
                                    Integer.parseInt(maxTerms)
                            )
                    ));

            for (TermFrequency tf : d.getTermFreqList().getTermFreq()) {
                d.getTermFreqList().getUnstemmedWords().addAll(termVectorService.loadUnstemmedWordsOfDocument(d.getId(), tf.getTerm()));
            }
        }
    }

    /**
     * loads all documents from all readable remote collections with limited
     * termvector
     */
    private void getRemoteDocsWithTermVector() {
        for (Collection c : collectionBean.getCollectionSearchState().getCollections()) {
            if (c.getNode().getLocal() == false) {

                // any of the clouds is okay
                List<CloudNode> cnl = this.cloudNodeService.load(null, c.getNode());
                if ((cnl == null) || (cnl.size() == 0)) {
                    continue;
                }
                WordCloudWebRequest req = tempClient.getWordCloudResponse(
                        userBean.getCurrentAccount(),
                        cnl.get(0),
                        tagList,
                        Stream.of(c.getId()).collect(Collectors.toSet())
                );
                docSeachState.getFoundDocuments().addAll(req.getDocumentsWithTerms());
            }
        }
    }

    private List<Document> copyDocList(List<Document> docsToCopy) {
        ArrayList<Document> docs = new ArrayList<>();
        for (Document d : docsToCopy) {
            docs.add(d);
        }
        return docs;
    }

    private List<WordTerm> initialiseWordTerms() throws Exception {
        List<WordTerm> wordTerms = filterWordTerms(docSeachState.getFoundDocuments(), tagList);

        for (WordTerm t : wordTerms) {
            t.setDocsWithTerm(
                    getDocAmountWithAdditionalTag(
                            t.getTerm(),
                            docSeachState.getFoundDocuments()
                    ));
        }
        return wordTerms;
    }

    private void createWordCloud(List<WordTerm> wordTerms) {
        wordTerms = organizer.orderTerms(wordTerms, Integer.parseInt(maxTerms));
        model = new DefaultTagCloudModel();
        for (WordTerm wt : wordTerms) {
            model.addTag(new DefaultTagCloudItem(wt.getWordRepresantation(), wt.getCategory().getNumValue()));
        }
    }

    private void removeDocsWithoutTags() {

        for (String s : tagList) {
            for (int i = docSeachState.getFoundDocuments().size() - 1; i >= 0; i--) {
                if (docSeachState.getFoundDocuments().get(i).getTermFreqList().getFreqOfVagueWord(s) == 0) {
                    docSeachState.getFoundDocuments().remove(i);
                }
            }
        }
    }

    public void toggleWordCloudVisibility() {
        wordCloudVisible = !wordCloudVisible;
    }

    public TagCloudModel getModel() {
        return model;
    }

    public String getMaxTerms() {
        return maxTerms;
    }

    public void setMaxTerms(String maxTerms) {
        this.maxTerms = maxTerms;
    }

    public boolean isWordCloudVisible() {
        return wordCloudVisible;
    }

    public void setWordCloudVisible(boolean wordCloudVisible) {
        this.wordCloudVisible = wordCloudVisible;
    }

    public String getSearchTermInput() {
        return searchTermInput;
    }

    public void setSearchTermInput(String searchTermInput) {
        this.searchTermInput = searchTermInput;
    }

    public DocumentSearchState getDocSeachState() {
        return docSeachState;
    }

    public void setDocSeachState(DocumentSearchState docSeachState) {
        this.docSeachState = docSeachState;
    }

    public String getTagsAsStringforBadges() {
        String tagListForBadges = "";
        if (tagList.isEmpty()) {
            return tagListForBadges;
        }
        for (String s : tagList) {
            tagListForBadges += s + ",";
        }
        return tagListForBadges.substring(0, tagListForBadges.length() - 1);

    }

    private void convertInputStringToTags() {
        searchTermInput = searchTermInput.replace(",", " ");
        for (String s : searchTermInput.trim().split(" ")) {
            if (!s.isEmpty()) {
                tagList.add(s.trim().toLowerCase());
            }
        }
    }

    public Set<String> getTagList() {
        return tagList;
    }

    public void setTagList(Set<String> tagList) {
        this.tagList = tagList;
    }

    public List<String> getTagsAsList() {
        return new ArrayList<>(tagList);
    }  
}
