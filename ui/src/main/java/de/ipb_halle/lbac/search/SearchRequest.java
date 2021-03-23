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

import de.ipb_halle.lbac.XmlSetWrapper;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.lang.Condition;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public interface SearchRequest {

    public Condition getCondition();

    public int getFirstResult();

    public int getMaxResults();

    public User getUser();

    public void setCondition(Condition c);

    public SearchTarget getSearchTarget();

    public void setSearchTarget(SearchTarget searchTarget);

    public void setUser(User user);

    public SearchRequest addSearchCategory(SearchCategory cat, String... values);

    public HashMap<SearchCategory, XmlSetWrapper> getSearchValues();

    public void setSearchValues(HashMap<SearchCategory, XmlSetWrapper> values);

}
