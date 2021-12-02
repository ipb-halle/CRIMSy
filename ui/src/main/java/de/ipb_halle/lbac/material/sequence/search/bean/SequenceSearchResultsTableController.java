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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.bean.TableController;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.display.ResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.util.FastaFileFormat;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

/**
 * 
 * @author flange
 */
public class SequenceSearchResultsTableController implements TableController {
    private static final long serialVersionUID = 1L;
    private MessagePresenter messagePresenter;
    private SequenceSearchMaskController searchMaskController;
    private SendFileBean sendFileBean;

    private SortItem sortBy = SortItem.EVALUE;
    private static final SortItem[] sortByItems = SortItem.values();

    private List<FastaResultDisplayWrapper> results = new ArrayList<>();

    public SequenceSearchResultsTableController(SequenceSearchMaskController searchMaskController,
            MessagePresenter messagePresenter) {
        this.searchMaskController = searchMaskController;
        this.messagePresenter = messagePresenter;
    }

    @Override
    public void reloadDataTableItems() {
        //List<SequenceAlignment> searchResults = ...

        ResultDisplayConfig displayConfig = searchMaskController.getSearchMode().getDisplayConfig();

        results = new ArrayList<>();
        for (SequenceAlignment alignment : searchResults) {
            FastaResultDisplayWrapper wrapper = new FastaResultDisplayWrapper(alignment.getFoundSequence(),
                    alignment.getAlignmentInformation()).config(displayConfig);
            results.add(wrapper);
        }

        sortResults();
    }

    private void sortResults() {
        results.sort(sortBy.getComparator());
    }

    @Override
    public void setLastUser(User u) {
        // TODO
    }

    /*
     * Actions
     */
    public void actionDownloadAllResultsAsFasta() throws IOException {
        if (results == null) {
            return;
        }
        Collection<Sequence> sequences = allSequencesFromResults();
        String fastaFile = FastaFileFormat.generateFastaString(sequences);

        if (!fastaFile.isEmpty()) {
            sendFileBean.sendFile(fastaFile.getBytes(), "results.fasta");
        }
    }

    private Collection<Sequence> allSequencesFromResults() {
        /*
         * LinkedHashSet guarantees (1) insertion-order and (2) duplicate removal
         * (because Sequence implements equals() and hashCode() properly).
         */
        Collection<Sequence> sequences = new LinkedHashSet<>();
        results.forEach(result -> sequences.add(result.getSequence()));

        return sequences;
    }

    public void actionDownloadResultAsFasta(FastaResultDisplayWrapper item, int index) throws IOException {
        String fastaFile = FastaFileFormat.generateFastaString(item.getSequence());

        if (!fastaFile.isEmpty()) {
            sendFileBean.sendFile(fastaFile.getBytes(), "result_" + index + ".fasta");
        }
    }

    public void actionOnChangeSortBy() {
        if (results != null) {
            sortResults();
        }
    }

    /*
     * Getters with logics
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