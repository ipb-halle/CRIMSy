/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.sequence.Sequence;

import de.ipb_halle.lbac.material.common.bean.MaterialTableController;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.search.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.display.ResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.material.sequence.util.FastaFileFormat;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

/**
 * JSF controller for the sequence search's alignment results.
 *
 * @author flange
 */
public class SequenceSearchResultsTableController extends MaterialTableController {
    private static final long serialVersionUID = 1L;

    private SequenceSearchService sequenceSearchService;
    private SendFileBean sendFileBean;
    private MessagePresenter messagePresenter;
    private SequenceSearchMaskController searchMaskController;

    private SortItem sortBy = SortItem.EVALUE;
    private static final SortItem[] sortByItems = SortItem.values();
    private User lastUser;

    private List<FastaResultDisplayWrapper> results = new ArrayList<>();

    public SequenceSearchResultsTableController(MaterialService materialService,
            SequenceSearchService sequenceSearchService, SendFileBean sendFileBean, MessagePresenter messagePresenter) {
        super(materialService);
        this.sequenceSearchService = sequenceSearchService;
        this.sendFileBean = sendFileBean;
        this.messagePresenter = messagePresenter;
    }

    public void setSearchMaskController(SequenceSearchMaskController searchMaskController) {
        this.searchMaskController = searchMaskController;
    }

    /**
     * Reload the results list by performing a sequence database search and sort
     * this list.
     */
    @Override
    public void reloadDataTableItems() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(lastUser, 0,
                searchMaskController.getMaxResults());
        builder.addMaterialType(MaterialType.SEQUENCE);
        builder.setSearchValues(lastValues);

        SearchResult searchResultFromService = sequenceSearchService.searchSequences(builder.build());

        boolean hasErrors = !searchResultFromService.getErrorMessages().isEmpty();
        if (hasErrors) {
            messagePresenter.error("sequenceSearch_error");
            return;
        }

        List<SequenceAlignment> searchResults = searchResultFromService
                .getAllFoundObjectsAsSearchable(SequenceAlignment.class);

        ResultDisplayConfig displayConfig = searchMaskController.getSearchMode().getDisplayConfig();

        results = new ArrayList<>();
        for (SequenceAlignment alignment : searchResults) {
            FastaResultDisplayWrapper wrapper = new FastaResultDisplayWrapper(alignment.getFoundSequence(),
                    alignment.getAlignmentInformation()).config(displayConfig);
            results.add(wrapper);
        }

        sortResults();

        if (results.isEmpty()) {
            messagePresenter.info("sequenceSearch_noResults");
        }
    }

    private void sortResults() {
        results.sort(sortBy.getComparator());
    }

    @Override
    public void setLastUser(User u) {
        lastUser = u;
    }

    /*
     * Actions
     */
    /**
     * Offer a download of all distinct result sequences in a single fasta file.
     *
     * @throws IOException
     */
    public void actionDownloadAllResultsAsFasta() throws IOException {
        Collection<Sequence> sequences = allDistinctSequencesFromResults();
        String fastaFile = FastaFileFormat.generateFastaString(sequences);

        if (!fastaFile.isEmpty()) {
            sendFileBean.sendFile(fastaFile.getBytes(), "results.fasta");
        }
    }

    private Collection<Sequence> allDistinctSequencesFromResults() {
        // set with distinct objects
        TreeSet<FastaResultDisplayWrapper> set = new TreeSet<>(
                Comparator.comparing(result -> result.getSequence().getId()));
        set.addAll(results);

        // sorted list
        List<FastaResultDisplayWrapper> distinctResults = new ArrayList<>(set);
        distinctResults.sort(sortBy.getComparator());

        // sorted list of distinct sequences
        List<Sequence> sequences = new ArrayList<>(distinctResults.size());
        distinctResults.forEach(result -> sequences.add(result.getSequence()));

        return sequences;
    }

    /**
     * Offer a download of the given result as fasta file.
     *
     * @throws IOException
     */
    public void actionDownloadResultAsFasta(FastaResultDisplayWrapper item, int index) throws IOException {
        if (item == null) {
            return;
        }
        String fastaFile = FastaFileFormat.generateFastaString(item.getSequence());

        if (!fastaFile.isEmpty()) {
            sendFileBean.sendFile(fastaFile.getBytes(), "result_" + index + ".fasta");
        }
    }

    /**
     * Sort the result list.
     */
    public void actionOnChangeSortBy() {
        sortResults();
    }

    /*
     * Getters with logic
     */
    /**
     * @param item
     * @return i18nized name for the given {@link SortItem}
     */
    public String getLocalizedSortByLabel(SortItem item) {
        return messagePresenter.presentMessage("sequenceSearch_sortItem_" + item.toString());
    }

    /*
     * Getters/setters for fields
     */
    public SortItem getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortItem sortBy) {
        this.sortBy = sortBy;
    }

    public SortItem[] getSortByItems() {
        return sortByItems;
    }

    public List<FastaResultDisplayWrapper> getResults() {
        return results;
    }
}