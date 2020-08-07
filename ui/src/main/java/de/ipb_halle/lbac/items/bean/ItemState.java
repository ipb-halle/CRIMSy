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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.items.Item;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class ItemState {

    private Date currentHistoryDate = null;

    public ItemState() {

        originalItem = null;
        editedItem = new Item();
    }

    public ItemState(Item original) {
        originalItem = original;
        editedItem = copyItem(original);

    }

    private Item originalItem;
    private Item editedItem;

    private Item copyItem(Item original) {
        Item copiedItem = new Item();
        copiedItem.setAmount(original.getAmount());
        copiedItem.setArticle(original.getArticle());
        copiedItem.setConcentration(original.getConcentration());
        copiedItem.setContainer(original.getContainer());
        copiedItem.setContainerSize(original.getContainerSize());
        copiedItem.setContainerType(original.getContainerType());
        copiedItem.setDescription(original.getDescription());
        copiedItem.setId(original.getId());
        copiedItem.setMaterial(original.getMaterial());
        copiedItem.setOwner(original.getOwner());
        copiedItem.setProject(original.getProject());
        copiedItem.setPurity(original.getPurity());
        copiedItem.setSolvent(original.getSolvent());
        copiedItem.setUnit(original.getUnit());
        copiedItem.setcTime(original.getcTime());
        copiedItem.setHistory(original.getHistory());
        copiedItem.setACList(original.getACList());
        copiedItem.setOwner(original.getOwner());
                
        return copiedItem;
    }

    public Item getOriginalItem() {
        return originalItem;
    }

    public Item getEditedItem() {
        return editedItem;
    }

    public void setEditedItem(Item editedItem) {
        this.editedItem = editedItem;
    }

    public Date getCurrentHistoryDate() {
        return currentHistoryDate;
    }

    public void setCurrentHistoryDate(Date currentHistoryDate) {
        this.currentHistoryDate = currentHistoryDate;
    }

    public Date getPreviousKey(Date d) {
        if (d == null) {
            if(editedItem.getHistory().isEmpty() ){
                return null;
            }else{
                return editedItem.getHistory().lastKey();    
            }
            
        }
        if (editedItem.getHistory().isEmpty() || !editedItem.getHistory().containsKey(d)) {
            return null;
        }
        if (editedItem.getHistory().firstKey().equals(d)) {
            return null;
        }
        ArrayList<Date> dates = new ArrayList<>(editedItem.getHistory().keySet());

        int index = dates.indexOf(d);

        return dates.get(index - 1);
    }
    
      public Date getFollowingKey(Date d) {
        if (d == null||editedItem.getHistory().isEmpty()||d.getTime()==editedItem.getHistory().lastKey().getTime()) {
            return null;
        }
        ArrayList<Date> dates = new ArrayList<>(editedItem.getHistory().keySet());
        int index = dates.indexOf(d);
        return dates.get(index + 1);
    }

}
