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
package de.ipb_halle.lbac.material.mocks;

import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyBean;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyHistoryController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyLevelController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNameController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyRenderController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyTreeController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyValidityController;

/**
 *
 * @author fmauz
 */
public class TaxonomyBeanMock extends TaxonomyBean {

    
    private MessagePresenter messagePresenter;

    public TaxonomyBeanMock(MessagePresenter presenter) {
        messagePresenter = presenter;
    }

    public void init(
            MemberService memberService,
            TaxonomyService taxonomieService) {
        nameController = new TaxonomyNameController(this);
        levelController = new TaxonomyLevelController(this);
        levelController.setLevels(taxonomieService.loadTaxonomyLevel());
        validityController = new TaxonomyValidityController(this, messagePresenter);
        historyController = new TaxonomyHistoryController(this, nameController, taxonomieService, memberService);
        renderController = new TaxonomyRenderController(this, nameController, levelController, memberService, messagePresenter);
        treeController = new TaxonomyTreeController(selectedTaxonomy, taxonomieService, levelController);
    }

}
