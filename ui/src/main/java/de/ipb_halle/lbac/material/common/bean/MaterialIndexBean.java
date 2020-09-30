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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.service.IndexService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialIndexBean implements Serializable {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private final int INDEX_TYPE_NAME = 1;

    @Inject
    private IndexService indexService;

    @Inject
    private MaterialBean materialEditBean;

    protected List<IndexEntry> indices = new ArrayList<>();
    protected Map<String, Integer> indexCategoriesReversed = new HashMap<>();
    protected Map<Integer, String> indexCategories = new HashMap<>();
    protected String indexCatergory;
    protected String indexValue;

    public void init() {
        indices.clear();
        indexCategories = indexService.loadIndexTypes();
        indexCategories.remove(1);
        for (Integer i : indexCategories.keySet()) {
            if (i > INDEX_TYPE_NAME) {
                indexCategoriesReversed.put(indexCategories.get(i), i);
            }
        }

        indexCatergory = indexCategoriesReversed.keySet().iterator().next();
        indexValue = "";
    }

    public List<String> getIndexCategories() {
        List<String> values = new ArrayList<>();

        for (String s : indexCategories.values()) {
            boolean alreadyIn = false;
            for (IndexEntry ie : indices) {
                if (indexCategories.get(ie.getTypeId()).equals(s)) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                values.add(s);
            }
        }
        return values;
    }

    public String getIndexCatergory() {
        return indexCatergory;
    }

    public void setIndexCatergory(String indexCatergory) {
        this.indexCatergory = indexCatergory;
    }

    public String getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(String indexValue) {
        this.indexValue = indexValue;
    }

    public List<IndexEntry> getIndices() {
        return indices;
    }

    public void setIndices(List<IndexEntry> indices) {
        this.indices = indices;
    }

    public void addNewIndex() {
        boolean alreadyIn = false;

        for (int i = 0; i < indices.size(); i++) {
            IndexEntry ie = indices.get(i);
            if (indexCategories.get(ie.getTypeId()).equals(indexCatergory)) {
                alreadyIn = true;
                ie.setValue(indexValue);
            }
        }
        if (!alreadyIn) {

            IndexEntry ie = new IndexEntry(indexCategoriesReversed.get(indexCatergory), indexValue, null);
            indices.add(ie);
        }
        indexValue = "";
        if (!getIndexCategories().isEmpty()) {
            indexCatergory = getIndexCategories().get(0);

        } else {
            indexCatergory = "";
        }
    }

    public void removeIndex(IndexEntry ie) {
        indices.remove(ie);
        if (!getIndexCategories().isEmpty()) {
            indexCatergory = getIndexCategories().get(0);

        } else {
            indexCatergory = "";
        }
    }

    public String getIndexName(IndexEntry ie) {
        return indexCategories.get(ie.getTypeId());
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    public boolean isIndexEditDisabled() {
        if (materialEditBean.getMode() == MaterialBean.Mode.HISTORY) {
            return true;
        }
        if (materialEditBean.getMode() == MaterialBean.Mode.CREATE) {
            return false;
        }
        return !materialEditBean.hasDetailRight(ACPermission.permEDIT, MaterialDetailType.INDEX);

    }

    public void setMaterialEditBean(MaterialBean materialEditBean) {
        this.materialEditBean = materialEditBean;
    }

}
