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

import de.ipb_halle.lbac.search.RemoteTransformer;
import de.ipb_halle.lbac.search.Searchable;

/**
 *
 * @author fmauz
 */
public class RemoteItemTransformer implements RemoteTransformer {

    private Item item;
    private RemoteItem remoteItem;

    public RemoteItemTransformer(Item item) {
        this.item = item;
        this.remoteItem = new RemoteItem();
        remoteItem.setId(item.getId());
        remoteItem.setAmount(item.getAmount());
        remoteItem.setDescription(item.getDescription());
        remoteItem.setMaterialName(item.getMaterial().getFirstName());
        if (item.getProject() != null) {
            remoteItem.setProjectName(item.getProject().getDescription());
        }
        remoteItem.setUnit(item.getUnit());
    }

    @Override
    public Searchable transformToRemote() {
        return remoteItem;
    }

}
