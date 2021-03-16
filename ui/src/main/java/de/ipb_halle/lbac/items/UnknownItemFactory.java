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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.unknown.UnknownMaterial;
import java.util.TreeMap;

/**
 *
 * @author fmauz
 */
public class UnknownItemFactory {

    public static Item getInstance(User user, ACList publicAcList) {
        Item item = new Item();
        item.setMaterial(UnknownMaterial.createNewInstance(publicAcList));
        item.setOwner(user);
        item.setHistory(new TreeMap<>());
        item.setLabel("no label");
        item.setId(-1);
        return item;
    }
}
