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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class IndexHistoryController implements HistoryController<MaterialIndexDifference> {

    private MaterialBean materialBean;

    public IndexHistoryController(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    @Override
    public void applyPositiveDifference(MaterialIndexDifference indexDiff) {
        for (int i = 0; i < indexDiff.getEntries(); i++) {
            if (indexDiff.getTypeId().get(i) == 1) {
                int currentSize = materialBean.getMaterialNameBean().getNames().size();
                for (int j = currentSize - 1; j >= 0; j--) {
                    MaterialName mn = materialBean.getMaterialNameBean().getNames().get(j);
                    String newLan = indexDiff.getLanguageNew().get(i);
                    String newValue = indexDiff.getValuesNew().get(i);
                    Integer newRank = indexDiff.getRankNew().get(i);
                    boolean rankEqual = mn.getRank().equals(indexDiff.getRankOld().get(i));
                    boolean valueEqual = mn.getValue().equals(indexDiff.getValuesOld().get(i));
                    boolean lanEqual = mn.getLanguage().equals(indexDiff.getLanguageOld().get(i));
                    if (rankEqual && valueEqual && lanEqual) {
                        if (newLan == null) {
                            materialBean.getMaterialNameBean().getNames().remove(j);
                        } else {
                            mn.setLanguage(newLan);
                            mn.setValue(newValue);
                            mn.setRank(newRank);
                        }
                    }
                    if (indexDiff.getRankOld().get(i) == null && indexDiff.getValuesOld().get(i) == null && indexDiff.getLanguageOld().get(i) == null) {
                        materialBean.getMaterialNameBean().getNames().add(new MaterialName(newValue, newLan, newRank));
                        break;
                    }
                }
            } else {
                if (indexDiff.getValuesNew().get(i) == null) {
                    IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), materialBean.getMaterialIndexBean().getIndices());
                    if (index != null) {
                        materialBean.getMaterialIndexBean().getIndices().remove(index);
                    }
                } else if (indexDiff.getValuesOld().get(i) == null) {
                    materialBean.getMaterialIndexBean().getIndices().add(
                            new IndexEntry(
                                    indexDiff.getTypeId().get(i),
                                    indexDiff.getValuesNew().get(i),
                                    null));
                } else {
                    IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), materialBean.getMaterialIndexBean().getIndices());
                    if (index != null) {
                        index.setValue(indexDiff.getValuesNew().get(i));
                    }
                }
            }
        }
        materialBean.getMaterialNameBean().reorderNamesByRank();
    }

    @Override
    public void applyNegativeDifference(MaterialIndexDifference indexDiff) {
        for (int i = 0; i < indexDiff.getEntries(); i++) {
            if (indexDiff.getTypeId().get(i) == 1) {

                for (int j = materialBean.getMaterialNameBean().getNames().size() - 1; j >= 0; j--) {
                    MaterialName mn = materialBean.getMaterialNameBean().getNames().get(j);
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
                            materialBean.getMaterialNameBean().getNames().remove(j);
                        } else {
                            mn.setLanguage(oldLan);
                            mn.setValue(oldValue);
                            mn.setRank(oldRank);
                        }
                    }
                    if (newLan == null && newValue == null && newRank == null) {
                        materialBean.getMaterialNameBean().getNames().add(new MaterialName(oldValue, oldLan, oldRank));
                        break;
                    }
                }
            } else {
                if (indexDiff.getValuesOld().get(i) == null) {
                    IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), materialBean.getMaterialIndexBean().getIndices());
                    if (index != null) {
                        materialBean.getMaterialIndexBean().getIndices().remove(index);
                    }
                } else if (indexDiff.getValuesNew().get(i) == null) {
                    materialBean.getMaterialIndexBean().getIndices().add(
                            new IndexEntry(
                                    indexDiff.getTypeId().get(i),
                                    indexDiff.getValuesOld().get(i),
                                    null));
                } else {
                    IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), materialBean.getMaterialIndexBean().getIndices());
                    if (index != null) {
                        index.setValue(indexDiff.getValuesOld().get(i));
                    }
                }
            }
        }
        materialBean.getMaterialNameBean().reorderNamesByRank();
    }

    protected IndexEntry getIndexByTypeId(int typeId, List<IndexEntry> indices) {
        if (indices == null) {
            return null;
        }
        for (IndexEntry ie : indices) {
            if (ie.getTypeId() == typeId) {
                return ie;
            }
        }
        return null;
    }

}
