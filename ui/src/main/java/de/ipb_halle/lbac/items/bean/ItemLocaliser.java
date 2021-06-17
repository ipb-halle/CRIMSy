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

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.items.Item;
import java.io.Serializable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class ItemLocaliser implements Serializable {

    private final static long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public List<Item> localiseContainerNamesOf(List<Item> items) {
        try {
            for (Item i : items) {
                localiseBoundContainerOf(i);
                localiseNestedContainersOf(i);
            }
        } catch (Exception e) {
            logger.error("Clould not localise containernames", e);
        }
        return items;
    }

    private void localiseBoundContainerOf(Item i) {
        if (i.getContainerType() != null) {
            i.getContainerType().setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + i.getContainerType().getName(), null));
        }
    }

    private void localiseNestedContainersOf(Item i) {
        if (i.getContainer() != null) {
            i.getContainer().getType().setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + i.getContainer().getType().getName(), null));
            for (Container c : i.getContainer().getContainerHierarchy()) {
                localiseContainerTypeOf(c);
            }
        }
    }
    private void localiseContainerTypeOf(Container c) {
        c.getType().setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + c.getType().getName(), null));
    }
}
