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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bean for interacting with the ui to present and manipulate a experiments
 *
 * @author fbroda
 */
@Dependent
public class MaterialAgent implements Serializable {

    private final static long serialVersionUID = 1L;
    private int MAX_MATERIALS_TO_SEARCH = 5;

    @Inject
    protected GlobalAdmissionContext globalAdmissionContext;

    @Inject
    protected UserBean userBean;

    @Inject
    protected MaterialService materialService;

    private String materialSearch = "";
    private String moleculeSearch = "";

    private MaterialHolder materialHolder;
    private boolean showMolEditor = false;

    private Integer materialId;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public void actionSetMaterial() {
        this.logger.info("actionSetMaterial() materialId = {}", this.materialId);
        if (this.materialHolder != null) {

            // do the actual work
            if (this.materialId != null) {
                this.materialHolder.setMaterial(
                        this.materialService.loadMaterialById(this.materialId));
            }

        } else {
            this.logger.info("actionSetMaterial(): materialHolder not set");
        }
    }

    private SearchRequest createSearchRequest() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(
                this.userBean.getCurrentAccount(),
                0,
                MAX_MATERIALS_TO_SEARCH);
        if (this.materialSearch != null || !this.materialSearch.trim().isEmpty()) {
            builder.addIndexName(this.materialSearch);
        }
        if (this.moleculeSearch != null || !this.moleculeSearch.trim().isEmpty()) {
            builder.addSubMolecule(this.moleculeSearch);
        }
        int size = this.materialHolder.getMaterialTypes().size();
        MaterialType[] types = new MaterialType[size];
        builder.addTypes(this.materialHolder.getMaterialTypes().toArray(types));
        return builder.buildSearchRequest();
    }

    public MaterialHolder getMaterialHolder() {
        this.logger.info("getMaterialHolder() {}", this.materialHolder == null ? "null" : "holder is set");
        return this.materialHolder;
    }

    /**
     * get the list of appropriate materials
     */
    public List<Material> getMaterialList() {
        if (this.materialHolder != null) {
            try {
                SearchResult result = this.materialService.getReadableMaterials(
                        createSearchRequest());
                if (result == null) {
                    this.logger.info("getMaterialList() result is null");
                } else {
                    this.logger.info("getMaterialList() got {} results", result.getAllFoundObjects().size());
                }
                return result.getAllFoundObjects(Material.class, result.getNodes().iterator().next());
            } catch (Exception e) {
                this.logger.warn("getMaterialList() caught an exception: ", (Throwable) e);
            }
        }
        return new ArrayList<Material>();
    }

    public Integer getMaterialId() {
        return this.materialId;
    }

    public String getMaterialSearch() {
        return this.materialSearch;
    }

    public String getMoleculeSearch() {
        this.logger.info("getMoleculeSearch() len={}", this.moleculeSearch.length());
        return this.moleculeSearch;
    }

    public boolean getShowMolEditor() {
        return this.showMolEditor;
    }

    public void setMaterialHolder(MaterialHolder materialHolder) {
        this.materialHolder = materialHolder;
    }

    public void setMaterialId(Integer materialId) {
        this.logger.info("setMaterialId() {}", materialId);
        this.materialId = materialId;
    }

    public void setMaterialSearch(String materialSearch) {
        this.materialSearch = materialSearch;
    }

    public void setMoleculeSearch(String moleculeSearch) {
        this.logger.info("setMoleculeSearch() len={}", moleculeSearch.length());
        this.moleculeSearch = moleculeSearch;
    }

    public void setShowMolEditor(boolean show) {
        this.showMolEditor = show;
    }

}
