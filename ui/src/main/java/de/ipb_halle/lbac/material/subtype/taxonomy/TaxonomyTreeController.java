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

import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.service.TaxonomyService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
public class TaxonomyTreeController {

    protected TreeNode selectedTaxonomy;
    private List<Taxonomy> shownTaxonomies;
    protected TaxonomyService taxonomyService;
    protected TaxonomyLevelController levelController;
    private TreeNode taxonomyTree;

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    public TaxonomyTreeController(
            TreeNode selectedTaxonomy,
            TaxonomyService taxonomyService,
            TaxonomyLevelController levelController) {
        this.selectedTaxonomy = selectedTaxonomy;
        this.taxonomyService = taxonomyService;
        this.levelController = levelController;
        reloadTreeNode(null);
    }

    public void reloadTreeNode(TreeNode selectedNode) {
        try {
            Map<String, Object> cmap = new HashMap<>();
            shownTaxonomies = taxonomyService.loadTaxonomy(cmap, true);
            Taxonomy rootTaxo = createNewTaxonomy();
            rootTaxo.setLevel(levelController.getLevels().get(0));
            taxonomyTree = new DefaultTreeNode(rootTaxo, null);
            for (Taxonomy t : shownTaxonomies) {
                if (!t.getTaxHierachy().isEmpty()) {
                    TreeNode parent = getTreeNodeWithTaxonomy(t.getTaxHierachy().get(0).getId());
                    new DefaultTreeNode(t, parent);
                } else {
                    new DefaultTreeNode(t, taxonomyTree);
                }
            }
            expandTree();
        } catch (Exception e) {
            logger.error(e);
        }

    }

    private TreeNode getTreeNodeWithTaxonomy(int id) {
        List<TreeNode> nodes = getAllChildren(taxonomyTree);
        nodes.add(taxonomyTree);
        for (TreeNode n : nodes) {
            Taxonomy t = (Taxonomy) n.getData();
            if (t.getId() == id) {
                return n;
            }
        }
        return null;
    }
    
    public void selectTaxonomy(Taxonomy t){
        for(TreeNode n:getAllChildren(taxonomyTree)){
            Taxonomy ta=(Taxonomy)n.getData();
            if(ta.getId()==t.getId()){
                n.setSelected(true);
                expandTree();
            }
        }
    }

    private List<TreeNode> getAllChildren(TreeNode tn) {
        List<TreeNode> children = new ArrayList<>();
        for (TreeNode n : tn.getChildren()) {
            children.addAll(getAllChildren(n));
            children.add(n);
        }
        return children;
    }

    public TreeNode getTaxonomyTree() {
        return taxonomyTree;
    }

    public void setTaxonomyTree(TreeNode taxonomyTree) {
        this.taxonomyTree = taxonomyTree;
    }

    public void expandTree() {
        if (selectedTaxonomy == null) {
            return;
        }
        Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
        expandNode(selectedTaxonomy);
        getTreeNodeWithTaxonomy(t.getId()).setSelected(true);
    }

    private void expandNode(TreeNode n) {
        Taxonomy t = (Taxonomy) n.getData();
        n = getTreeNodeWithTaxonomy(t.getId());
        n.setExpanded(true);
        if (n.getParent() != null) {
            expandNode(n.getParent());
        }
    }

    public void disableTreeNodeEntries(Taxonomy taxoToEdit) {
        List<TreeNode> nodes = getAllChildren(taxonomyTree);
        for (TreeNode n : nodes) {
            Taxonomy t = (Taxonomy) n.getData();

            boolean leaf = t.getLevel().getRank() == levelController.getLeastRank();
            boolean targetGotLowerRank = taxoToEdit.getLevel().getRank() >= t.getLevel().getRank();

            n.setSelectable(!leaf
                    && t.getId() != taxoToEdit.getId()
                    && targetGotLowerRank
                    && isTaxonomyInHierarchy(taxoToEdit.getId(), t));
        }
    }

    private boolean isTaxonomyInHierarchy(int id, Taxonomy taxo) {
        for (Taxonomy t : taxo.getTaxHierachy()) {
            if (t.getId() == id) {
                return false;
            }
        }
        return true;
    }

    public Taxonomy createNewTaxonomy() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("", "en", 1));
        return new Taxonomy(0, names, new HazardInformation(), new StorageClassInformation(), new ArrayList<>());
    }

}
