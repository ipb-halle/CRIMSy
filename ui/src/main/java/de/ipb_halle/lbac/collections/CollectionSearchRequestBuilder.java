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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class CollectionSearchRequestBuilder extends SearchRequestBuilder {

    private final Set<String> collectionIds = new HashSet<>();

    public CollectionSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        this.target = SearchTarget.MATERIAL;
    }

    @Override
    protected void addSearchCriteria() {
        addIdsToRequest();

    }

    private void addIdsToRequest() {
        if (!collectionIds.isEmpty()) {
            request.addSearchCategory(
                    SearchCategory.COLLECTION,
                    collectionIds.stream().toArray(String[]::new));
        }
    }

    public void addId(int id) {
        this.collectionIds.add(String.valueOf(id));
    }

}
