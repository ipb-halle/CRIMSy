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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides methods to apply one ore more differences bewetween two
 * states of a material.
 *
 * @author fmauz
 */
public class HistoryOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected MaterialBean materialBean;

    /**
     * Initialises the functionality by neccessary services and the history
     * (collection of all differences at some timepoints) in the
     * materialEditState variable.
     *
     * @param materialBean
     */
    public HistoryOperation(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    /**
     * Based on the current timepoint applies all differences of the next
     * timepoint and set t as the current timepoint.
     *
     */
    public void applyNextPositiveDifference() {
        materialBean.getMaterialEditState().changeVersionDateToNext(materialBean.getMaterialEditState().getCurrentVersiondate());

        for (MaterialDifference difference : materialBean.getMaterialEditState().getMaterialBeforeEdit().getHistory().getChanges().get(materialBean.getMaterialEditState().getCurrentVersiondate())) {
            difference.createHistoryController(materialBean).applyPositiveDifference(difference);
        }

    }

    /**
     * Based on the current timepoint applies all differences(negative) of the
     * last timepoint and set t as the current timepoint.
     *
     */
    public void applyNextNegativeDifference() {
        for (MaterialDifference difference : materialBean.getMaterialEditState().getMaterialBeforeEdit().getHistory().getChanges().get(materialBean.getMaterialEditState().getCurrentVersiondate())) {
            difference.createHistoryController(materialBean).applyNegativeDifference(difference);
        }
        materialBean.getMaterialEditState().changeVersionDateToPrevious(materialBean.getMaterialEditState().getCurrentVersiondate());
    }

    /**
     * @return true if the currently shown material state is the original
     * version
     */
    public boolean isOriginalMaterial() {
        return materialBean.getMaterialEditState().getCurrentVersiondate() == null;
    }

}
