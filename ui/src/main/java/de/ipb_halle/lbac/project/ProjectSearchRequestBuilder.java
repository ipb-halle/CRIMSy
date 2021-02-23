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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchRequestImpl;
import de.ipb_halle.lbac.search.SearchTarget;

/**
 *
 * @author fmauz
 */
public class ProjectSearchRequestBuilder extends SearchRequestBuilder {

    private String projectName;
    private SearchTarget target;

    public ProjectSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        target = SearchTarget.PROJECT;
    }

    public void setProjectName(String name) {
        projectName = name;
    }

    @Override
    public SearchRequest build() {
        SearchRequest request = new SearchRequestImpl(user, firstResult, maxResults);
        request.setSearchTarget(target);
        setProjectName(request);
        
        return request;
    }

    private void setProjectName(SearchRequest request) {
        if (projectName != null && !projectName.isEmpty()) {
            request.addSearchCategory(SearchCategory.PROJECT, projectName);
        }
    }

}
