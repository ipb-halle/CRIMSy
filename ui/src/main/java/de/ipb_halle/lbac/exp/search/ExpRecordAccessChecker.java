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
package de.ipb_halle.lbac.exp.search;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayRecord;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class ExpRecordAccessChecker {

    private ExpRecordService recordService;
    private ACListService aclistService;

    public ExpRecordAccessChecker(ExpRecordService recordService, ACListService aclistService) {
        this.recordService = recordService;
        this.aclistService = aclistService;
    }

    public boolean checkExpRecords(
            int experimentid,
            List<Object> searchString,
            User user) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("EXPERIMENT_ID", experimentid);
        List<ExpRecord> records = recordService.load(cmap);
        if (records.isEmpty() || searchString.isEmpty()) {
            return true;
        }
        boolean shouldExpBeShown = false;
        for (ExpRecord rec : records) {
            if (rec.getType() == ExpRecordType.ASSAY) {
                shouldExpBeShown = checkAssay((Assay) rec, shouldExpBeShown, user, searchString);
            }
            if (rec.getType() == ExpRecordType.TEXT) {
                shouldExpBeShown = checkText((Text) rec, shouldExpBeShown, searchString);
            }
        }
        return shouldExpBeShown;
    }

    private boolean checkText(Text text, boolean shouldExpBeShown, List<Object> searchString) {
        if (textContainsSearchTerm(text.getText(), searchString)) {
            shouldExpBeShown = true;
        }
        return shouldExpBeShown;
    }

    private boolean checkAssay(Assay assay, boolean shouldExpBeShown, User user, List<Object> searchString) {
        if (checkMaterial(assay.getTarget(), user, searchString)) {
            shouldExpBeShown = true;
        }
        for (AssayRecord assayRec : assay.getRecords()) {
            if (checkMaterial(assayRec.getMaterial(), user, searchString)) {
                shouldExpBeShown = true;
            }
            if (checkItem(assayRec.getItem(), user, searchString)) {
                shouldExpBeShown = true;
            }
        }
        return shouldExpBeShown;
    }

    public boolean textContainsSearchTerm(String text, List<Object> searchString) {
        for (Object nameObj : searchString) {
            String searchTerm = (String) nameObj;
            if (text.toLowerCase().contains(searchTerm.toLowerCase().replace("%", ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMaterial(Material mat, User user, List<Object> searchString) {
        boolean hit = false;
        if (!aclistService.isPermitted(ACPermission.permREAD, mat.getACList(), user)) {
            return false;
        }

        for (Object nameObj : searchString) {
            String name = (String) nameObj;
            for (MaterialName matName : mat.getNames()) {
                if (matName.getValue().toLowerCase().contains(name.toLowerCase().replace("%", ""))) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    private boolean checkItem(Item item, User user, List<Object> searchString) {
        if (item == null) {
            return false;
        }
        boolean hit = false;
        if (!aclistService.isPermitted(ACPermission.permREAD, item.getACList(), user)) {
            return false;
        }
        for (Object nameObj : searchString) {
            String name = (String) nameObj;

            if (item.getDescription().toLowerCase().contains(name.toLowerCase().replace("%", ""))
                    || item.getNameToDisplay().toLowerCase().contains(name.toLowerCase().replace("%", ""))) {
                hit = true;
            }

        }
        return hit;
    }
}
