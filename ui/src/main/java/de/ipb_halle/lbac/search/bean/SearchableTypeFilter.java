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
package de.ipb_halle.lbac.search.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class SearchableTypeFilter {

    List<SearchTarget> types = new ArrayList<>();
    List<SearchTarget> selectedTypes = new ArrayList<>();
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public SearchableTypeFilter() {
        types.add(SearchTarget.MATERIAL);
        types.add(SearchTarget.ITEM);
        types.add(SearchTarget.EXPERIMENT);
        types.add(SearchTarget.DOCUMENT);

        selectedTypes.addAll(types);
    }

    public List<SearchTarget> getTypes() {
        return types;
    }

    public void setTypes(List<SearchTarget> types) {
        this.types = types;
    }

    public List<SearchTarget> getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(List<SearchTarget> selectedTypes) {
        this.selectedTypes = selectedTypes;
    }

    public String getLocalizedType(SearchTarget unlocalizedType) {
        return Messages.getString(
                MESSAGE_BUNDLE,
                "advanced_search_type_" + unlocalizedType.toString(),
                null);
    }

    public boolean shouldSearchForMaterial() {
        return selectedTypes.contains(SearchTarget.MATERIAL);
    }

    public boolean shouldSearchForExperiment() {
        return selectedTypes.contains(SearchTarget.EXPERIMENT);
    }

    public boolean shouldSearchForDocument() {
        return selectedTypes.contains(SearchTarget.DOCUMENT);
    }

    public boolean shouldSearchForItem() {
        return selectedTypes.contains(SearchTarget.ITEM);
    }
}
