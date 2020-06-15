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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.biomaterial.TaxonomyBean;
import de.ipb_halle.lbac.material.bean.manipulation.NameListOperation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class TaxonomyNameController {

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    protected TaxonomyBean taxonomyBean;
    private NameListOperation nameListOperation = new NameListOperation(MESSAGE_BUNDLE);
    private List<MaterialName> names;

    public TaxonomyNameController(TaxonomyBean taxonomyBean) {
        this.taxonomyBean = taxonomyBean;
    }

    public void addNewEmptyName(List<MaterialName> taxonomyNames) {
        nameListOperation.addNewEmptyName(taxonomyNames);
    }

    public void removeName(MaterialName mName) {
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE
                || taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
            nameListOperation.deleteName(mName, getTargetTaxonomy().getNames());
        }
    }

    public boolean isEnabled(
            MaterialName mn,
            String b,
            List<MaterialName> materialNames) {
        return nameListOperation.isEnabled(mn, b, materialNames);
    }

    public void swapPosition(MaterialName mn, String action) {
        NameListOperation.Button button = NameListOperation.Button.valueOf(action);
        switch (button) {
            case LOWEST:
                nameListOperation.setRankToLowest(mn, getTargetTaxonomy().getNames());
                break;
            case LOWER:
                nameListOperation.substractOneRank(mn, getTargetTaxonomy().getNames());
                break;
            case HIGHER:
                nameListOperation.addOneRank(mn, getTargetTaxonomy().getNames());
                break;
            case HIGHEST:
                nameListOperation.setRankToHighest(mn, getTargetTaxonomy().getNames());
                break;
        }

    }

    public List<MaterialName> getNames() {
        try {
            if (taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE
                    || taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
                return getTargetTaxonomy().getNames();
            }
            if (taxonomyBean.getSelectedTaxonomy() != null) {
                Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
                return t.getNames();
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.info("Error at fetching names");
            return new ArrayList<>();
        }
    }

    public void setNames(List<MaterialName> names) {
        this.names = names;
    }

    public List<String> getPossibleLanguages() {
        List<String> languages = new ArrayList<>();
        languages.add("de");
        languages.add("la");
        languages.add("en");
        return languages;
    }

    public void addNewName() {
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE
                || taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
            addNewEmptyName(getNames());
        }
    }

    private Taxonomy getTargetTaxonomy() {
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
            return taxonomyBean.getTaxonomyToEdit();
        } else {
            return taxonomyBean.getTaxonomyToCreate();
        }
    }

}
