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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.lang.Condition;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
public class SearchRequestImpl implements SearchRequest {

    private SearchTarget searchTarget;
    private Condition condition;
    private int firstResultIndex;
    private int maxResults;
    private User user;

    public SearchRequestImpl() {
    }

    public int getFirstResultIndex() {
        return firstResultIndex;
    }

    public void setFirstResultIndex(int firstResultIndex) {
        this.firstResultIndex = firstResultIndex;
    }

    public SearchRequestImpl(User u, Condition condition, int firstResult, int maxResults) {
        this.firstResultIndex = firstResult;
        this.maxResults = maxResults;
        this.condition = condition;
        this.user = u;

    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public int getFirstResult() {
        return firstResultIndex;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public SearchTarget getSearchTarget() {
        return searchTarget;
    }

    @Override
    public void setSearchTarget(SearchTarget searchTarget) {
        this.searchTarget = searchTarget;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void switchToTransferMode() {
        if (getCondition() != null) {
            getCondition().switchToTransferMode();
            if (getCondition().getConditions() != null) {
                for (Condition con : getCondition().getConditions()) {
                    con.switchToTransferMode();
                }
            }
        }
    }

    public void switchToLocalMode() {

        if (getCondition() != null) {
            getCondition().switchToLocalMode();
            if (getCondition().getConditions() != null) {
                for (Condition con : getCondition().getConditions()) {
                    con.switchToLocalMode();
                }
            }
        }

    }

}
