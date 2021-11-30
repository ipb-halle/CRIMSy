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
package de.ipb_halle.lbac.search.mocks;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceSearchService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import java.util.ArrayList;

/**
 *
 * @author fmauz
 */
public class SequenceSearchServiceMock extends SequenceSearchService {

    private static final long serialVersionUID = 1L;

    @Override
    public SearchResult searchSequences(SearchRequest request) {
        SearchResultImpl result = new SearchResultImpl();
        
        Sequence sequence =new Sequence(0, new ArrayList<>(), 1, new HazardInformation(), new StorageInformation(),SequenceData.builder().build());
        
        SequenceAlignment alignent=new SequenceAlignment(sequence, new FastaResult());
        result.addResult(alignent);
        return result;
    }
}
