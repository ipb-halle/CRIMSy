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
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
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
            TissueService tissueService,
            Taxonomy taxonomyOfMaterial) {
        this.tissueService = tissueService;
        treeController = new TaxonomyTreeController(
                selectedTaxonomy,
                taxonomyService,
                new SimpleTaxonomyLevelController(taxonomyService.loadTaxonomyLevel()));

        treeController.selectTaxonomy(taxonomyOfMaterial);
        treeController.initSelectionAndExpanseState();

    }

    public TaxonomyTreeController getTreeController() {
        return treeController;
    }

    public void onTaxonomySelect(NodeSelectEvent event) {
        selectedTaxonomy = event.getTreeNode();
    }
    /**
     * Keeps the loaded taxonomies of the children in memory
     *
     * @param event
     */
    public void onTaxonomyCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }

    /**
     * loads the children of the now shown taxonomy from the database
     *
     * @param event
     */
    public void onTaxonomyExpand(NodeExpandEvent event) {
        if (selectedTaxonomy == null) {
            treeController.setSelectedTaxonomy(event.getTreeNode());
        }
        event.getTreeNode().setExpanded(true);
        treeController.addTaxonomy((Taxonomy) event.getTreeNode().getData());
        treeController.reorganizeTaxonomyTree();
    }

    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
    }

    public void setSelectedTaxonomy(Taxonomy t) {
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

    public void setSelectedTaxonomyById(int id) {
        setSelectedTaxonomy(treeController.getTaxonomyService().loadTaxonomyById(id));
    }
    
    public void deactivateTree(){
        treeController.deactivateTree();
    }
     public void activateTree(){
        treeController.activateTree();
    }

}
