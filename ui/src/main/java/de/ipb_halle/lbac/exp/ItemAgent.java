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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.search.SearchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bean for interacting with the ui to present and manipulate a experiments
 *
 * @author fbroda
 */
@Dependent
public class ItemAgent implements Serializable {

    private final static long serialVersionUID = 1L;

    @Inject
    protected GlobalAdmissionContext globalAdmissionContext;

    @Inject
    protected UserBean userBean;

    @Inject
    protected ItemService itemService;

    private String itemSearch = "";

    private ItemHolder itemHolder;
    private boolean showMolEditor = false;

    private Integer itemId;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private List<Item> chooseableItems = new ArrayList<>();

    public void actionSetItem(Item item) {
        if (this.itemHolder != null) {
            this.itemHolder.setItem(item);
        } else {
            this.logger.info("actionSetItem(): itemHolder not set");
        }
    }

    public ItemHolder getItemHolder() {
        this.logger.info("getItemHolder() {}", this.itemHolder == null ? "null" : "holder is set");
        return this.itemHolder;
    }

    /**
     * get the list of appropriate items
     *
     * @return
     */
    public List<Item> getItemList() {
        return chooseableItems;
    }

    public void actionTriggerItemSearch() {
        chooseableItems = new ArrayList<>();
        if ((this.itemHolder != null)
                && (this.itemSearch != null)
                && (!this.itemSearch.isEmpty())) {
            try {
                ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(userBean.getCurrentAccount(), 0, 1);
                builder.addLabel(itemSearch);
                SearchResult result = itemService.loadItems(builder.buildSearchRequest());
                chooseableItems = result.getAllFoundObjects(Item.class, result.getNode());
            } catch (NumberFormatException nfe) {
                // ignore and return an empty list
                chooseableItems = new ArrayList<>();
            } catch (Exception e) {
                this.logger.warn("getItemList() caught an exception: ", (Throwable) e);
            }
        }
    }

    public Integer getItemId() {
        return this.itemId;
    }

    public String getItemSearch() {
        return this.itemSearch;
    }

    public void setItemHolder(ItemHolder itemHolder) {
        this.itemHolder = itemHolder;
    }

    public void setItemId(Integer itemId) {
        this.logger.info("setItemId() {}", itemId);
        this.itemId = itemId;
    }

    public void setItemSearch(String itemSearch) {
        this.itemSearch = itemSearch;
    }

    public String getAmount(Item item) {
        if (item.getUnit() == null) {
            return String.format("%f", item.getAmount());
        } else {
            return String.format("%f  %s", item.getAmount(), item.getUnit());
        }
    }

}
