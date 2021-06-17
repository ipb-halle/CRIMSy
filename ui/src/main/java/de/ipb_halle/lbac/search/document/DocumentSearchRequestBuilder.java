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
package de.ipb_halle.lbac.search.document;

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
public class DocumentSearchRequestBuilder extends SearchRequestBuilder {

    private Integer collectionId;
    private Set<String> wordRoots;

    public DocumentSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        target = SearchTarget.DOCUMENT;

    }

    @Override
    protected void addSearchCriteria() {
        addWordRoots();
        addCollectionId();
    }

    private void addWordRoots() {
        if (wordRoots != null) {

            String[] wordRootArray = new String[wordRoots.size()];
            int i = 0;
            for (String s : wordRoots) {
                wordRootArray[i] = s;
                i++;
            }
            this.request.addSearchCategory(SearchCategory.WORDROOT, wordRootArray);
        }
    }

    private void addCollectionId() {
        if (collectionId != null) {
            this.request.addSearchCategory(SearchCategory.COLLECTION, String.valueOf(collectionId));
        }
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public void setWordRoots(Set<String> wordRoots) {
        this.wordRoots = wordRoots;
    }

    public void setWordRoot(String root) {
        this.wordRoots = new HashSet<>();
        wordRoots.add(root);

    }

}
