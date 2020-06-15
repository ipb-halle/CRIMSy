/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.forum.HTMLInputFilter;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.NameListOperation.Button;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialNameBean implements Serializable {

    protected List<MaterialName> names = new ArrayList<>();
    protected NameListOperation nameListOperation;
    protected List<String> possibleLanguages = new ArrayList<>();
  

    @Inject
    private MaterialBean materialEditBean;

    public List<MaterialName> getNames() {
        return names;
    }

    public void setNames(List<MaterialName> names) {
        this.names = names;
    }

    public void init() {
        names.clear();
        possibleLanguages.clear();
        possibleLanguages.add("de");
        possibleLanguages.add("en");

        nameListOperation = new NameListOperation(possibleLanguages.get(0));
        nameListOperation.addNewEmptyName(names);

    }

    public List<String> getPossibleLanguages() {
        return possibleLanguages;
    }

    public void addNewName() {
        if (materialEditBean.getPermission().isDetailInformationEditable("COMMON_INFORMATION")) {
            nameListOperation.addNewEmptyName(names);
        }
    }

    public void removeName(MaterialName mName) {
        if (materialEditBean.getPermission().isDetailInformationEditable("COMMON_INFORMATION")) {
            nameListOperation.deleteName(mName, names);
        }

    }

    public NameListOperation getNameListOperation() {
        return nameListOperation;
    }

    public void setNameListOperation(NameListOperation nameListOperation) {
        this.nameListOperation = nameListOperation;
    }

    public void swapPosition(MaterialName mn, String action) {
        if (!materialEditBean.getPermission().isDetailInformationEditable("COMMON_INFORMATION")) {
            return;
        }

        Button button = Button.valueOf(action);

        switch (button) {
            case LOWEST:
                nameListOperation.setRankToLowest(mn, names);
                break;
            case LOWER:
                nameListOperation.substractOneRank(mn, names);
                break;
            case HIGHER:
                nameListOperation.addOneRank(mn, names);
                break;
            case HIGHEST:
                nameListOperation.setRankToHighest(mn, names);
                break;

        }
    }

    public boolean isMaterialNameOperationEnabled(
            MaterialName mn,
            String b) {
        if (materialEditBean.getMode() == MaterialBean.Mode.HISTORY) {
            return false;
        }
        return materialEditBean.getPermission().isDetailInformationEditable("COMMON_INFORMATION")
                && nameListOperation.isEnabled(mn, b, names);
    }

    public void reorderNamesByRank() {
        Collections.sort(names, Collections.reverseOrder());
    }
}
