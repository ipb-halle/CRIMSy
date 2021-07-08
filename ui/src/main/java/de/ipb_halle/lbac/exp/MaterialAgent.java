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
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.apache.commons.lang.exception.ExceptionUtils;

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
    private final int MAX_MATERIALS_TO_SEARCH = 5;
    
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
    
    private List<Material> choosableMaterials = new ArrayList<>();
    
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    
    public void actionSetMaterial(Material m) {
        this.materialHolder.setMaterial(m);
    }
    
    private SearchRequest createSearchRequest() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(
                this.userBean.getCurrentAccount(),
                0,
                MAX_MATERIALS_TO_SEARCH);
        //TO DO: set deactivated

        // builder.s(false);
        if (this.materialSearch != null && !this.materialSearch.trim().isEmpty()) {
            builder.setMaterialName(this.materialSearch);
        }
        
        if (isMoleculeSearch()) {
            builder.setStructure(this.moleculeSearch);
        }
        for (MaterialType t : this.materialHolder.getMaterialTypes()) {
            builder.addMaterialType(t);
        }
        return builder.build();
    }
    
    private boolean shouldSearchBeDone() {
        boolean nameSet = this.materialSearch != null && !this.materialSearch.trim().isEmpty();
        boolean moleculeSet = isMoleculeSearch();
        boolean targetSet = this.materialHolder != null && this.materialHolder.getMaterialTypes().size() > 0;
        return targetSet && (nameSet || moleculeSet);
    }
    
    public MaterialHolder getMaterialHolder() {
        return this.materialHolder;
    }

    /**
     * get the list of appropriate materials
     *
     * @return
     */
    public List<Material> getMaterialList() {
        return choosableMaterials;
    }
    
    public void actionTriggerMaterialSearch() {
        if (!shouldSearchBeDone()) {
            choosableMaterials = new ArrayList<>();
        }
        if (this.materialHolder != null) {
            try {
                SearchResult result = this.materialService.loadReadableMaterials(
                        createSearchRequest());
                choosableMaterials = extractMaterialsFromResult(result);
            } catch (Exception e) {
                this.logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }
    
    private List<Material> extractMaterialsFromResult(SearchResult result) {
        
        Node n = result.getNode();
        List<Material> foundMaterials = new ArrayList<>();
        for (MaterialType t : getMaterialHolder().getMaterialTypes()) {
            foundMaterials.addAll(result.getAllFoundObjects(t.getClassOfDto(), n));
        }
        return foundMaterials;
    }
    
    public String getMaterialSearch() {
        return this.materialSearch;
    }
    
    public String getMoleculeSearch() {
        return this.moleculeSearch;
    }
    
    public boolean getShowMolEditor() {
        return this.showMolEditor;
    }
    
    public void setMaterialHolder(MaterialHolder materialHolder) {
        this.materialHolder = materialHolder;
    }
    
    public void setMaterialSearch(String materialSearch) {
        this.materialSearch = materialSearch;
    }
    
    public void setMoleculeSearch(String moleculeSearch) {
        this.moleculeSearch = moleculeSearch;
    }
    
    public void setShowMolEditor(boolean show) {
        this.showMolEditor = show;
    }
    
    private boolean isMoleculeSearch() {
        if (this.moleculeSearch != null && !this.moleculeSearch.trim().isEmpty()) {
            Molecule mol = new Molecule(moleculeSearch, 0);
            return !mol.isEmptyMolecule();
        }
        return false;
    }
    
    public void clearAgent() {
        this.materialSearch = "";
        this.choosableMaterials.clear();
        this.moleculeSearch = "";
    }
    
}
