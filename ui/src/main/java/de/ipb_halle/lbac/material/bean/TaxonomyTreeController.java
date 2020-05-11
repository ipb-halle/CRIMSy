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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.material.service.TaxonomyService;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
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

    protected TaxonomyBean taxonomyBean;
    private List<Taxonomy> shownTaxonomies;
    protected TaxonomyService taxonomyService;
    protected TaxonomyLevelController levelController;
    private TreeNode taxonomyTree;

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    public TaxonomyTreeController(
            TaxonomyBean taxonomyBean,
            TaxonomyService taxonomyService,
            TaxonomyLevelController levelController) {
        this.taxonomyBean = taxonomyBean;
        this.taxonomyService = taxonomyService;
        this.levelController = levelController;
    }

    public void reloadTreeNode(TreeNode selectedNode) {
        try {
            Map<String, Object> cmap = new HashMap<>();
            shownTaxonomies = taxonomyService.loadTaxonomy(cmap, true);
            Taxonomy rootTaxo = taxonomyBean.createNewTaxonomy();
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
        if (taxonomyBean.getSelectedTaxonomy() == null) {
            return;
        }
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        expandNode(taxonomyBean.getSelectedTaxonomy());
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

    public void disableTreeNodeEntries(int id) {
        List<TreeNode> nodes = getAllChildren(taxonomyTree);
        for (TreeNode n : nodes) {
            Taxonomy t = (Taxonomy) n.getData();
            boolean leaf = t.getLevel().getRank() == levelController.getLeastRank();
            n.setSelectable(!leaf && t.getId() != id);
        }
    }

}
