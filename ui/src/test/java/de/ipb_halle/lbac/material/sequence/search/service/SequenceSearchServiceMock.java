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
package de.ipb_halle.lbac.material.sequence.search.service;

import java.util.function.Function;

import jakarta.ejb.Singleton;

import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;

/**
 * @author flange
 */
@Singleton
public class SequenceSearchServiceMock extends SequenceSearchService {
    private static final long serialVersionUID = 1L;

    private Function<SearchRequest, SearchResult> behaviour;

    public void setBehaviour(Function<SearchRequest, SearchResult> behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public SearchResult searchSequences(SearchRequest request) {
        return behaviour.apply(request);
    }
}
