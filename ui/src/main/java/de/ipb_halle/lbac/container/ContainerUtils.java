/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.container;

import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * 
 * @author flange
 */
public class ContainerUtils {
    private static Logger logger = LogManager.getLogger(ContainerUtils.class.getName());

    private ContainerUtils() {
    }

    /**
     * Removes all containers with a rank greater than 0 in the list of container
     * types, sets the localized name for the remaining containers and sorts the
     * list.
     * 
     * @param containerTypes
     * @param messagePresenter
     */
    public static void filterLocalizeAndSortContainerTypes(List<ContainerType> types,
            MessagePresenter messagePresenter) {
        removeContainerTypesByMaxRank(types, 0);
        types.forEach(type -> localizeContainerType(type, messagePresenter));
        sortContainerTypesByLocalizedName(types);
    }

    /**
     * Removes all elements with a rank greater than maxRank from the containerTypes
     * list.
     * 
     * @param types
     * @param maxRank
     */
    private static void removeContainerTypesByMaxRank(List<ContainerType> types, int maxRank) {
        types.removeIf(type -> type.getRank() > maxRank);
    }

    private static void localizeContainerType(ContainerType type, MessagePresenter messagePresenter) {
        String localizedName = messagePresenter.presentMessage("container_type_" + type.getName());

        if ((localizedName == null) || localizedName.trim().isEmpty()) {
            logger.error("Could not set localized containerTypeName for " + type.getName());

            // fallback to name of the entity
            type.setLocalizedName(type.getName());
        } else {
            type.setLocalizedName(localizedName);
        }
    }

    private static void sortContainerTypesByLocalizedName(List<ContainerType> types) {
        types.sort(Comparator.comparing(ContainerType::getLocalizedName));
    }
}
