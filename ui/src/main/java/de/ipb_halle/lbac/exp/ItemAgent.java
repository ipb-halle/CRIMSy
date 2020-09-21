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
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.service.ItemService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
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

    private final static long   serialVersionUID = 1L;

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

    public void actionSetItem() {
        this.logger.info("actionSetItem() itemId = {}", this.itemId);
        if (this.itemHolder != null) {

            // do the actual work
            if (this.itemId != null) {
                this.itemHolder.setItem(
                        this.itemService.loadItemById(this.itemId));
            }

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
     */
    public List<Item> getItemList() {
        ArrayList<Item> result = new ArrayList<> ();
        if ( this.itemHolder != null ) {
            try {
                Item item = this.itemService.loadItemById(Integer.parseInt(this.itemSearch));
                if (item != null) {
                    result.add(item);
                }
            } catch (Exception e) {
                this.logger.warn("getItemList() caught an exception: ", (Throwable) e);
            }
        } 
        return result;
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

}
