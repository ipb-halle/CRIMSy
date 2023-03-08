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

import de.ipb_halle.lbac.material.MessagePresenter;
import java.io.Serializable;
/**
 *
 * @author fmauz
 */
public class TaxonomyValidityController implements Serializable {

    protected MessagePresenter messagePresenter;

    protected TaxonomyBean taxonomyBean;

    public TaxonomyValidityController(TaxonomyBean taxonomyBean, MessagePresenter presenter) {
        this.taxonomyBean = taxonomyBean;
        this.messagePresenter = presenter;
    }

    public boolean checkInputValidity() {
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE) {
            if (taxonomyBean.getTaxonomyToCreate() != null) {
                if (taxonomyBean.getSelectedTaxonomy() == null) {
                    messagePresenter.error("taxonomy_no_valide_parent");
                    return false;
                }

                if (!isNameSet()) {
                    messagePresenter.error("taxonomy_no_valide_input");
                    return false;
                } else {
                    messagePresenter.info("taxonomy_saved");
                    return true;
                }
            }

        }
        return false;
    }

    private boolean isNameSet() {
        return taxonomyBean.getTaxonomyToCreate().getNames().size() > 0
                && !taxonomyBean.getTaxonomyToCreate().getNames()
                        .get(0)
                        .getValue()
                        .trim()
                        .equals("");
    }
}
