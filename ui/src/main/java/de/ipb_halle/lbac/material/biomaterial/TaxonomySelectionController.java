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

import java.io.Serializable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
public class TaxonomySelectionController implements Serializable {

    protected TaxonomyTreeController treeController;
    protected TreeNode selectedTaxonomy;
    protected Tissue selectedTissue;
    protected List<Tissue> selectableTissues;
    protected TissueService tissueService;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public TaxonomySelectionController(
            TaxonomyService taxonomyService,
            TissueService tissueService) {
        this.tissueService = tissueService;
        treeController = new TaxonomyTreeController(selectedTaxonomy, taxonomyService, new SimpleTaxonomyLevelController(taxonomyService.loadTaxonomyLevel()));
    }

    public TaxonomyTreeController getTreeController() {
        return treeController;
    }

    public void onTaxonomySelect(NodeSelectEvent event) {
        selectedTaxonomy = event.getTreeNode();
        logger.info("Choosing new Taxo");
    }

    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {

        this.selectedTaxonomy = selectedTaxonomy;
    }
    
    public void setSelectedTaxonomy(Taxonomy t){
        treeController.selectTaxonomy(t);
    }

    public String getSelectedTaxonomyName() {
        if (selectedTaxonomy == null) {
            selectableTissues = tissueService.loadTissues();
            return "Please choose a taxonomy";
        } else {
            Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
            selectableTissues = tissueService.loadTissues(t);
            return t.getFirstName();
        }

    }

    public List<Tissue> getSelectableTissues() {
        return selectableTissues;
    }

    public Tissue getSelectedTissue() {
        return selectedTissue;
    }

    public void setSelectedTissue(Tissue selectedTissue) {
        this.selectedTissue = selectedTissue;
    }

}
