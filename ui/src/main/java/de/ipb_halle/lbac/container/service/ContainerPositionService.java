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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class ContainerPositionService {

    String SQL_SAVE_ITEM_IN_CONTAINER = "INSERT INTO item_positions ("
            + "itemid,"
            + "containerid,"
            + "itemrow,"
            + "itemcol) "
            + "VALUES("
            + ":itemid,"
            + ":containerid,"
            + ":posY,"
            + ":posX)";

    private final String SQL_DELETE_ITEM_IN_CONTAINER
            = "DELETE FROM item_positions "
            + "WHERE itemid=:itemid";

    private final String SQL_CHECK_ITEM_AT_POSITION
            = "SELECT itemid "
            + "FROM item_positions "
            + "WHERE itemrow=:row "
            + "AND itemcol=:col "
            + "AND containerid=:containerid";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    public boolean moveItemToNewPosition(
            Item item,
            Container newContainer,
            Set<int[]> newPositions,
            User owner,
            Date mdate) {
        //Check if new Positions are blocked
        if (!areContainerSlotsFree(item, newContainer, newPositions)) {
            return false;
        }
        Set<int[]> oldPositions = getItemPositionsInContainer(item);
        if (!areItemPositionsEqual(oldPositions, newPositions)
                || !areContainerEqual(item.getContainer(), newContainer)) {
            archiveChanges(owner, item, item.getContainer(), oldPositions, newContainer, newPositions, mdate
            );
            deleteItemInAllContainer(item.getId());
            saveItemToPositions(newContainer, item, newPositions);
        }
        return true;
    }

    private void saveItemToPositions(
            Container container,
            Item item,
            Set<int[]> positions) {
        if (container != null) {
            //Save new Positions
            for (int[] pos : positions) {
                saveItemInContainer(
                        item.getId(),
                        container.getId(),
                        pos[0],
                        pos[1]);
            }
        }
    }

    public void archiveChanges(User u, Item item, Container oldContainer, Set<int[]> oldPositions, Container containerNew, Set<int[]> positionsNew, Date mdate) {
        List<ItemPositionsHistory> diffs = new ArrayList<>();
        diffs.addAll(getNewDiffs(positionsNew, containerNew, item, mdate, u));
        diffs.addAll(getOldDiffs(oldPositions, oldContainer, item, mdate, u));
        for (ItemPositionsHistory d : diffs) {
            em.merge(d.createEntity());
        }
    }

    private List<ItemPositionsHistory> getNewDiffs(Set<int[]> positionsNew, Container container, Item item, Date mDate, User u) {
        List<ItemPositionsHistory> diffs = new ArrayList<>();
        if (container == null) {
            return diffs;
        }
        for (int[] n : positionsNew) {
            ItemPositionsHistory h = new ItemPositionsHistory();
            h.setColNew(n[0])
                    .setRowNew(n[1])
                    .setContainerId(container.getId())
                    .setItemId(item.getId())
                    .setUser(u)
                    .setmDate(mDate);
            diffs.add(h);
        }
        return diffs;
    }

    private List<ItemPositionsHistory> getOldDiffs(Set<int[]> positions, Container container, Item item, Date mDate, User u) {
        List<ItemPositionsHistory> diffs = new ArrayList<>();
        if (container == null) {
            return diffs;
        }
        for (int[] n : positions) {
            ItemPositionsHistory h = new ItemPositionsHistory();
            h.setColOld(n[0])
                    .setRowOld(n[1])
                    .setContainerId(container.getId())
                    .setItemId(item.getId())
                    .setUser(u)
                    .setmDate(mDate);
            diffs.add(h);
        }
        return diffs;
    }

    public boolean areContainerSlotsFree(Item i, Container c, Set<int[]> positions) {
        if (c == null) {
            return true;
        }
        for (int[] pos : positions) {
            Integer itemAtPlace = getItemIdAtPosition(c.getId(), pos[0], pos[1]);
            if (itemAtPlace != null && itemAtPlace != i.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an item is at the place where the new item should be placed
     * into and saves it to there if none item is blocking it.
     *
     * @param itemid
     * @param containerid
     * @param posX
     * @param posY
     * @return
     */
    public boolean saveItemInContainer(int itemid, int containerid, int posX, int posY) {
        Integer itemAtPlace = getItemIdAtPosition(containerid, posX, posY);
        if (itemAtPlace == null) {
            this.em.createNativeQuery(SQL_SAVE_ITEM_IN_CONTAINER)
                    .setParameter("itemid", itemid)
                    .setParameter("containerid", containerid)
                    .setParameter("posX", posX)
                    .setParameter("posY", posY)
                    .executeUpdate();
            return true;
        }
        return itemAtPlace == itemid;
    }

    public void deleteItemInAllContainer(int itemid) {
        this.em.createNativeQuery(SQL_DELETE_ITEM_IN_CONTAINER)
                .setParameter("itemid", itemid)
                .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public Integer getItemIdAtPosition(int containerId, int x, int y) {
        List<Integer> results = this.em.createNativeQuery(SQL_CHECK_ITEM_AT_POSITION)
                .setParameter("containerid", containerId)
                .setParameter("col", x)
                .setParameter("row", y)
                .getResultList();
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    private Set<int[]> getItemPositionsInContainer(Item item) {
        Set<int[]> positions = new HashSet<>();
        if (item.getContainer() != null) {
            positions = item.getContainer().getPositionsOfItem(item.getId());
        }
        return positions;
    }

    private boolean areItemPositionsEqual(Set<int[]> oldPositions, Set<int[]> newPositions) {
        if (oldPositions.size() != newPositions.size()) {
            return false;
        }
        int hits = 0;
        for (int[] p1 : oldPositions) {
            for (int[] p2 : newPositions) {
                if (p1[0] == p2[0] && p1[1] == p2[1]) {
                    hits++;
                }
            }
        }
        return hits == oldPositions.size();
    }

    private boolean areContainerEqual(Container oldContainer, Container newContainer) {
        Integer idOld = null;
        if (oldContainer != null) {
            idOld = oldContainer.getId();
        }
        Integer idNew = null;
        if (newContainer != null) {
            idNew = newContainer.getId();
        }
        return Objects.equals(idOld, idNew);
    }
}
