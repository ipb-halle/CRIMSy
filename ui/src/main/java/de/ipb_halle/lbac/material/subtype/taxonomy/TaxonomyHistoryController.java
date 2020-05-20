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
package de.ipb_halle.lbac.material.subtype.taxonomy;

import de.ipb_halle.lbac.material.bean.TaxonomyBean;
import de.ipb_halle.lbac.material.bean.TaxonomyBean.Mode;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.difference.MaterialDifference;
import de.ipb_halle.lbac.material.difference.MaterialIndexDifference;
import de.ipb_halle.lbac.material.difference.TaxonomyDifference;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class TaxonomyHistoryController {

    private SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Date dateOfShownHistory = null;
    private TaxonomyBean taxonomyBean;
    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    private TaxonomyNameController nameController;

    public TaxonomyHistoryController(TaxonomyBean taxonomyBean, TaxonomyNameController nameController) {
        this.taxonomyBean = taxonomyBean;
        this.nameController = nameController;
    }

    public Date getDateOfShownHistory() {
        return dateOfShownHistory;
    }

    public void setDateOfShownHistory(Date dateOfShownHistory) {
        this.dateOfShownHistory = dateOfShownHistory;
    }

    public String getHistoryText() {
        if (dateOfShownHistory == null) {
            return "Current Version";
        } else {
            Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            TaxonomyDifference diffTaxonomy = t.getHistory().getDifferenceOfTypeAtDate(TaxonomyDifference.class, dateOfShownHistory);
            MaterialIndexDifference diffNames = t.getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, dateOfShownHistory);
            String back = getVersionBasicLabel(diffTaxonomy, diffNames);
            if (diffTaxonomy != null) {
                back = "Taxonomy Values edited<br>";
            }
            if (diffNames != null) {
                back += "Names edited";
            }
            return back;

        }
    }

    public void actionSwitchToEarlierVersion() {
        taxonomyBean.setMode(Mode.HISTORY);
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        dateOfShownHistory = t.getHistory().getFollowingKey(dateOfShownHistory);
        applyNegativeDiff();

    }

    public void actionSwitchToLaterVersion() {
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        applyPositiveDiff();
        dateOfShownHistory = t.getHistory().getPreviousKey(dateOfShownHistory);
        if (dateOfShownHistory == null) {
            taxonomyBean.setMode(Mode.SHOW);
        }
    }

    private String getVersionBasicLabel(TaxonomyDifference diffTaxonomy, MaterialIndexDifference diffNames) {
        MaterialDifference diff = diffTaxonomy != null ? diffTaxonomy : diffNames;
        return SDF.format(diff.getModificationDate()) + "<br> by user: " + diff.getUserId().toString() + "<br><br>";
    }

    private void applyPositiveDiff() {
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        MaterialIndexDifference indexDiff = t.getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, dateOfShownHistory);
        if (indexDiff != null) {
            for (int i = 0; i < indexDiff.getEntries(); i++) {
                if (indexDiff.getTypeId().get(i) == 1) {
                    for (int j = nameController.getNames().size() - 1; j >= 0; j--) {
                        MaterialName mn = nameController.getNames().get(j);
                        String newLan = indexDiff.getLanguageNew().get(i);
                        String newValue = indexDiff.getValuesNew().get(i);
                        Integer newRank = indexDiff.getRankNew().get(i);
                        boolean rankEqual = mn.getRank().equals(indexDiff.getRankOld().get(i));
                        boolean valueEqual = mn.getValue().equals(indexDiff.getValuesOld().get(i));
                        boolean lanEqual = mn.getLanguage().equals(indexDiff.getLanguageOld().get(i));
                        if (rankEqual && valueEqual && lanEqual) {
                            if (newLan == null) {
                                nameController.getNames().remove(j);
                            } else {
                                mn.setLanguage(newLan);
                                mn.setValue(newValue);
                                mn.setRank(newRank);
                            }
                        }
                        if (indexDiff.getRankOld().get(i) == null && indexDiff.getValuesOld().get(i) == null && indexDiff.getLanguageOld().get(i) == null) {
                            nameController.getNames().add(new MaterialName(newValue, newLan, newRank));
                        }
                    }
                }
            }
        }
    }

    private void applyNegativeDiff() {
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        MaterialIndexDifference indexDiff = t.getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, dateOfShownHistory);
        if (indexDiff != null) {
            int originalNameSize = nameController.getNames().size();
            for (int i = 0; i < indexDiff.getEntries(); i++) {

                for (int j = originalNameSize - 1; j >= 0; j--) {
                    MaterialName mn = nameController.getNames().get(j);
                    String oldLan = indexDiff.getLanguageOld().get(i);
                    String oldValue = indexDiff.getValuesOld().get(i);
                    Integer oldRank = indexDiff.getRankOld().get(i);
                    String newLan = indexDiff.getLanguageNew().get(i);
                    String newValue = indexDiff.getValuesNew().get(i);
                    Integer newRank = indexDiff.getRankNew().get(i);
                    boolean rankEqual = mn.getRank().equals(newRank);
                    boolean valueEqual = mn.getValue().equals(newValue);
                    boolean lanEqual = mn.getLanguage().equals(newLan);
                    if (rankEqual && valueEqual && lanEqual) {
                        if (oldLan == null) {
                            nameController.getNames().remove(j);
                        } else {
                            mn.setLanguage(oldLan);
                            mn.setValue(oldValue);
                            mn.setRank(oldRank);
                        }
                    }
                    if (newLan == null && newValue == null && newRank == null) {
                        nameController.getNames().add(new MaterialName(oldValue, oldLan, oldRank));
                        break;
                    }
                }
            }
        }
    }
}
