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
package de.ipb_halle.lbac.search.mocks;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.SearchWebClient;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class SearchWebClientMock extends SearchWebClient {

    private Node node;
    private int responseTimeInMs;
    private boolean repsonseDelivered;

    public SearchWebClientMock(Node node, int responseTimeInMs) {
        this.node = node;
        this.responseTimeInMs = responseTimeInMs;
    }

    @Override
    public SearchResult getRemoteSearchResult(
            CloudNode cn,
            User user,
            List<SearchRequest> requests) {
        SearchResult result = new SearchResultImpl(cn.getNode());
        Item item = new Item();
        item.setId(1);
        repsonseDelivered = false;
        result.addResults(Arrays.asList(item));
        try {
            Thread.sleep(responseTimeInMs);
        } catch (Exception e) {
        }
        repsonseDelivered = true;
        return result;
    }

    public boolean isRepsonseDelivered() {
        return repsonseDelivered;
    }

}
