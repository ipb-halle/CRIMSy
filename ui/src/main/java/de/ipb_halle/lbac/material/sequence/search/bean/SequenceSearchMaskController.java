/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.bean;

import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskController;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.common.bean.MaterialTableController;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.search.SequenceSearchInformation;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.Arrays;

/**
 * JSF controller for the search filters in sequence database searches.
 *
 * @author flange
 */
public class SequenceSearchMaskController extends MaterialSearchMaskController {
    private static final long serialVersionUID = 1L;
    private final SequenceSearchMaskValuesHolder valuesHolder;

    public SequenceSearchMaskController(MaterialOverviewBean overviewBean, MaterialTableController tableController,
            MaterialService materialService, ProjectService projectService, MemberService memberService,
            MessagePresenter messagePresenter) {
        super(overviewBean, tableController, materialService, projectService, memberService,
                Arrays.asList(MaterialType.SEQUENCE));
        valuesHolder = new SequenceSearchMaskValuesHolder(messagePresenter);
    }

    @Override
    public void clearInputFields() {
        super.clearInputFields();
        valuesHolder.setQuery("");
    }

    @Override
    protected MaterialSearchMaskValues getValues() {
        SearchMode searchMode = valuesHolder.getSearchMode();
        MaterialSearchMaskValues values = super.getValues();
        values.type.clear();
        values.type.add(MaterialType.SEQUENCE);
        values.sequenceInfos = new SequenceSearchInformation(searchMode.getQuerySequenceType(),
                searchMode.getLibrarySequenceType(), valuesHolder.getQuery(),
                valuesHolder.getTranslationTable().getId());
        return values;
    }

    /*
     * Actions
     */
    /**
     * Start a sequence database search.
     */
    @Override
    public void actionStartMaterialSearch() {
        tableController.setLastValues(getValues());
        tableController.reloadDataTableItems();
    }

    /**
     * Clear all input fields.
     */
    @Override
    public void actionClearSearchFilter() {
        clearInputFields();
    }

    /*
     * Getters
     */
    public SequenceSearchMaskValuesHolder getValuesHolder() {
        return valuesHolder;
    }
}