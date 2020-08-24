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
package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.items.entity.ItemPositionsHistoryEntity;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class ItemPositionsHistory implements DTO {

    private Integer colNew;
    private Integer colOld;
    private int containerId;
    private int itemId;
    private Date mDate;
    private Integer rowNew;
    private Integer rowOld;
    private User user;
    private Integer id;

    public ItemPositionsHistory() {

    }

    public ItemPositionsHistory(ItemPositionsHistoryEntity entity, User user) {
        this.colNew = entity.getCol_new();
        this.colOld = entity.getCol_old();
        this.containerId = entity.getContainerid();
        this.itemId = entity.getItemid();
        this.mDate = entity.getMdate();
        this.rowNew = entity.getRow_new();
        this.rowOld = entity.getRow_old();
        this.user = user;
        this.id = entity.getId();

    }

    @Override
    public ItemPositionsHistoryEntity createEntity() {
        ItemPositionsHistoryEntity entity = new ItemPositionsHistoryEntity();
        entity.setId(id);
        entity.setActorid(user.getId());
        entity.setContainerid(containerId);
        entity.setItemid(itemId);
        entity.setCol_new(colNew);
        entity.setCol_old(colOld);
        entity.setRow_new(rowNew);
        entity.setRow_old(rowOld);
        entity.setMdate(mDate);

        return entity;
    }

    public Integer getColNew() {
        return colNew;
    }

    public Integer getColOld() {
        return colOld;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getItemId() {
        return itemId;
    }

    public Date getmDate() {
        return mDate;
    }

    public Integer getRowNew() {
        return rowNew;
    }

    public Integer getRowOld() {
        return rowOld;
    }

    public User getUser() {
        return user;
    }

    public ItemPositionsHistory setColNew(Integer colNew) {
        this.colNew = colNew;
        return this;
    }

    public ItemPositionsHistory setColOld(Integer colOld) {
        this.colOld = colOld;
        return this;
    }

    public ItemPositionsHistory setContainerId(int containerId) {
        this.containerId = containerId;
        return this;
    }

    public ItemPositionsHistory setItemId(int itemId) {
        this.itemId = itemId;
        return this;
    }

    public ItemPositionsHistory setmDate(Date mDate) {
        this.mDate = mDate;
        return this;
    }

    public ItemPositionsHistory setRowNew(Integer rowNew) {
        this.rowNew = rowNew;
        return this;
    }

    public ItemPositionsHistory setRowOld(Integer rowOld) {
        this.rowOld = rowOld;
        return this;
    }

    public ItemPositionsHistory setUser(User user) {
        this.user = user;
        return this;
    }

}
