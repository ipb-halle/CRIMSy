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

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.items.Item;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SearchResultImplTest {

    private UUID idOfNode1 = UUID.fromString("422352f8-024c-11eb-adc1-0242ac120002");
    private UUID idOfNode2 = UUID.fromString("5426ee2e-024c-11eb-adc1-0242ac120002");

    @Test
    public void getAllFoundObjectsTest() {
        SearchResultImpl searchResult = createResult();
        List<NetObject> netObjects = searchResult.getAllFoundObjects();
        Assert.assertEquals(6, netObjects.size());
    }

    @Test
    public void getAllFoundObjectsOfClassTest() {
        SearchResultImpl searchResult = createResult();
        List<NetObject> netObjects = searchResult.getAllFoundObjects(Item.class);
        Assert.assertEquals(4, netObjects.size());
        netObjects = searchResult.getAllFoundObjects(Container.class);
        Assert.assertEquals(2, netObjects.size());
    }

    @Test
    public void getAllFoundObjectsOfNode() {
        SearchResultImpl searchResult = createResult();
        List<Searchable> netObjects = searchResult.getAllFoundObjects(createNode(idOfNode1));
        Assert.assertEquals(3, netObjects.size());
    }

    @Test
    public void getAllFoundObjectsOfNodeAndClass() {
        SearchResultImpl searchResult = createResult();
        List<Item> netObjects = searchResult.getAllFoundObjects(Item.class, createNode(idOfNode1));
        Assert.assertEquals(2, netObjects.size());

    }

    private SearchResultImpl createResult() {
        SearchResultImpl searchResult = new SearchResultImpl();
        searchResult.addResults(
                createNode(idOfNode1),
                Arrays.asList(
                        createItem(1),
                        createItem(2),
                        createContainer(1)));
        searchResult.addResults(
                createNode(idOfNode2),
                Arrays.asList(
                        createItem(3),
                        createItem(4),
                        createContainer(2)));
        return searchResult;
    }

    private Node createNode(UUID id) {
        Node n = new Node();
        n.setId(id);
        return n;
    }

    private Searchable createItem(int id) {
        Item i = new Item();
        i.setId(id);
        return i;
    }

    private Searchable createContainer(int id) {
        Container c = new Container();
        c.setId(id);
        return c;
    }
}
