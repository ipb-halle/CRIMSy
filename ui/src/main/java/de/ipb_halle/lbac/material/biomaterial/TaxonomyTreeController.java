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

import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Controller for some functionalities(expand,colapse,...) of the tree for
 * taxonomies
 *
 * @author fmauz
 */
public class TaxonomyTreeController implements Serializable {

    private static final long serialVersionUID = 1L;

    private LoggingProfiler loggingProfiler;
    private Set<Integer> expandedTreeNodes = new HashSet<>();
    private Integer idOfSelectedTaxonomy;
    protected TaxonomyLevelController levelController;
    protected final Logger logger = LogManager.getLogger(this.getClass().getName());
    protected TaxonomyService taxonomyService;
    protected List<Taxonomy> shownTaxonomies = new ArrayList<>();
    protected TreeNode selectedTaxonomy;
    private TreeNode taxonomyTree;

    /**
     * @param lp
     * @param selectedTaxonomy
     * @param taxonomyService
     * @param levelController
     */
    public TaxonomyTreeController(
            LoggingProfiler lp,
            TreeNode selectedTaxonomy,
            TaxonomyService taxonomyService,
            TaxonomyLevelController levelController) {
        this.loggingProfiler = lp;
        this.selectedTaxonomy = selectedTaxonomy;
        this.taxonomyService = taxonomyService;
        this.levelController = levelController;
        reloadTreeNode();
    }

    /**
     * @param lp
     * @param selectedTaxonomy
     * @param taxonomyService
     * @param levelController
     */
    public TaxonomyTreeController(
            LoggingProfiler lp,
            Taxonomy selectedTaxonomy,
            TaxonomyService taxonomyService,
            TaxonomyLevelController levelController) {
        this.loggingProfiler = lp;
        this.idOfSelectedTaxonomy = selectedTaxonomy.getId();
        this.taxonomyService = taxonomyService;
        this.levelController = levelController;
        reloadTreeNode(selectedTaxonomy);
    }

    public void addTaxonomy(Taxonomy taxo) {
        List<Taxonomy> children = taxonomyService.loadDirectChildrenOf(taxo.getId());
        List<Taxonomy> grandChildren = new ArrayList<>();
        for (Taxonomy child : children) {
            grandChildren.addAll(taxonomyService.loadDirectChildrenOf(child.getId()));
        }
        addAbsentTaxos(grandChildren);

    }

    /**
     * Creates a blank taxonomy whithout user with a blank name
     *
     * @return
     */
    public Taxonomy createNewTaxonomy() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("", "en", 1));
        return new Taxonomy(0, names, new HazardInformation(), new StorageInformation(), new ArrayList<>(), null, null);
    }

    /**
     * Disables all nodes which has a lower taxonomy level than the given one
     *
     * @param taxoToEdit
     */
    public void disableTreeNodeEntries(Taxonomy taxoToEdit) {
        reloadTreeNode(); //Really neccessary ?
        List<TreeNode> nodes = getAllChildren(taxonomyTree);
        for (TreeNode n : nodes) {
            Taxonomy t = (Taxonomy) n.getData();
            boolean leaf = t.getLevel().getRank() == levelController.getLeastRank();
            boolean targetGotLowerRank = taxoToEdit.getLevel().getRank() > t.getLevel().getRank();
            boolean selectable = !leaf
                    && t.getId() != taxoToEdit.getId()
                    && targetGotLowerRank
                    && isTaxonomyInHierarchy(taxoToEdit.getId(), t);
            n.setSelectable(selectable);
        }
    }

    /**
     * Expand all nodes which are in the expanded list and its parents
     */
    public void expandTree() {
        for (TreeNode tn : getAllChildren(taxonomyTree)) {
            Taxonomy t = (Taxonomy) tn.getData();
            if (expandedTreeNodes.contains(t.getId())) {
                tn.setExpanded(true);
            }
            if (idOfSelectedTaxonomy != null && idOfSelectedTaxonomy == t.getId()) {
                tn.setSelected(true);
            }
        }
    }

    private List<TreeNode> getAllChildren(TreeNode tn) {
        if (tn == null) {
            return new ArrayList<>();
        }
        List<TreeNode> children = new ArrayList<>();
        for (Object n : tn.getChildren()) {
            children.addAll(getAllChildren((TreeNode) n));
            children.add((TreeNode) n);
        }
        return children;
    }

    /**
     *
     * @return
     */
    public TreeNode getTaxonomyTree() {
        return taxonomyTree;
    }

    /**
     * Looks for the taxonomy oin the tree and return it. If there is no
     * taxonomy with that id return null
     *
     * @param id
     * @return
     */
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

    /**
     * sets the selected taxonomy to the first entry
     */
    public void initialise() {
        expandedTreeNodes.clear();
        idOfSelectedTaxonomy = null;
        selectedTaxonomy = null;
        taxonomyTree = null;
        reloadTreeNode();
        setSelectedTaxonomy((TreeNode) taxonomyTree.getChildren().get(0));
    }

    /**
     * Checks if a taxonomy with id is in the hierarchy of the given taxonomy
     *
     * @param id id of taxonomy to look for
     * @param taxo taxonomy which hierarchy should be checked
     * @return
     */
    private boolean isTaxonomyInHierarchy(int id, Taxonomy taxo) {
        for (Taxonomy t : taxo.getTaxHierarchy()) {
            if (t.getId() == id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads all taxonomies from the database and selects a taxonomy if there is
     * one and expands all taxonomies which were expanded before
     */
    public final void reloadTreeNode() {
        try {
            loggingProfiler.profilerStart("TaxonomyBean.treeController.shownTaxonomies");

            shownTaxonomies = loadShownTaxos();
            loggingProfiler.profilerStop("TaxonomyBean.treeController.shownTaxonomies");

            loggingProfiler.profilerStart("TaxonomyBean.treeController.reorganizeTaxonomyTree");

            reorganizeTaxonomyTree();

        } catch (Exception e) {
            logger.error("reloadTreeNode() caught an exception:", (Throwable) e);
        } finally {

            loggingProfiler.profilerStop("TaxonomyBean.treeController.reorganizeTaxonomyTree");
        }
    }

    /**
     * Loads taxonomies from the database and selects a taxonomy if there is one
     * and expands all taxonomies which were expanded before.
     *
     * @param t
     */
    public final void reloadTreeNode(Taxonomy t) {
        try {
            shownTaxonomies = loadShownTaxos();
            addAbsentTaxo(t);
            for (Taxonomy ht : t.getTaxHierarchy()) {
                List<Taxonomy> childrenOfHierEntry = taxonomyService.loadDirectChildrenOf(ht.getId());
                List<Taxonomy> grandChildrenOfHierEntry = new ArrayList<>();
                for (Taxonomy ce : childrenOfHierEntry) {
                    grandChildrenOfHierEntry.addAll(taxonomyService.loadDirectChildrenOf(ce.getId()));
                }
                addAbsentTaxos(childrenOfHierEntry);
                addAbsentTaxos(grandChildrenOfHierEntry);
            }
            addAbsentTaxos(t.getTaxHierarchy());
            reorganizeTaxonomyTree();

        } catch (Exception e) {
            logger.error("reloadTreeNode() caught an exception:", (Throwable) e);
        }
    }

    public void reorganizeTaxonomyTree() {
        loggingProfiler.profilerStart("ReorganizeTaxonomyTree.saveExpanded...");
        saveExpandedAndSelectedTreeNodes();
        loggingProfiler.profilerStop("ReorganizeTaxonomyTree.saveExpanded...");

        loggingProfiler.profilerStart("ReorganizeTaxonomyTree.createNewTaxonomy");
        Taxonomy rootTaxo = createNewTaxonomy();
        loggingProfiler.profilerStop("ReorganizeTaxonomyTree.createNewTaxonomy");

        rootTaxo.setLevel(levelController.getRootLevel());

        taxonomyTree = new DefaultTreeNode(rootTaxo, null);

        loggingProfiler.profilerStart("ReorganizeTaxonomyTree.reorderTaxonomies");
        reorderTaxonomies();
        loggingProfiler.profilerStop("ReorganizeTaxonomyTree.reorderTaxonomies");

        loggingProfiler.profilerStart("ReorganizeTaxonomyTree.shownTaxonomies");
        for (Taxonomy t : shownTaxonomies) {
            TreeNode newNode = null;
            if (!t.getTaxHierarchy().isEmpty()) {
                TreeNode parent = getTreeNodeWithTaxonomy(t.getTaxHierarchy().get(0).getId());
                newNode = new DefaultTreeNode(t, parent);
            } else {
                newNode = new DefaultTreeNode(t, taxonomyTree);
            }
            if (idOfSelectedTaxonomy != null && t.getId() == idOfSelectedTaxonomy) {
                selectedTaxonomy = newNode;
            }
        }
        loggingProfiler.profilerStop("ReorganizeTaxonomyTree.shownTaxonomies");

        loggingProfiler.profilerStart("ReorganizeTaxonomyTree.expandTree");
        expandTree();
        loggingProfiler.profilerStop("ReorganizeTaxonomyTree.expandTree");
    }

    /**
     * Reorders taxonomies by first its level and second by its names
     */
    protected void reorderTaxonomies() {
        Comparator<Taxonomy> rankCom = Comparator.comparing((Taxonomy t) -> t.getLevel().getRank());
        Comparator<Taxonomy> nameCom = Comparator.comparing((Taxonomy t) -> t.getFirstName());
        shownTaxonomies.sort(rankCom.thenComparing(nameCom));
    }

    private List<Taxonomy> loadShownTaxos() {
        loggingProfiler.profilerStart("====loadShownTaxos");

        if (selectedTaxonomy == null) {
            shownTaxonomies = new ArrayList<>();
            shownTaxonomies.add(taxonomyService.loadRootTaxonomy());
        }
        loggingProfiler.profilerStop("====loadShownTaxos");

        loggingProfiler.profilerStart("====loadDirectChildrenOf");

        List<Taxonomy> children = taxonomyService.loadDirectChildrenOf(shownTaxonomies.get(0).getId());

        loggingProfiler.profilerStop("====loadDirectChildrenOf");

        loggingProfiler.profilerStart("====grandChildren");

        List<Taxonomy> grandChildren = new ArrayList<>();
        for (Taxonomy child : children) {
            grandChildren.addAll(taxonomyService.loadDirectChildrenOf(child.getId()));
        }
        loggingProfiler.profilerStop("====grandChildren");

        loggingProfiler.profilerStart("==== addAbsentTaxos(children);");

        addAbsentTaxos(children);
        loggingProfiler.profilerStop("==== addAbsentTaxos(children);");

        loggingProfiler.profilerStart("====addAbsentTaxos(grandChildren);");

        addAbsentTaxos(grandChildren);
        loggingProfiler.profilerStop("====addAbsentTaxos(grandChildren);");

        return shownTaxonomies;
    }

    protected void addAbsentTaxos(List<Taxonomy> taxos) {
        for (Taxonomy t : taxos) {
            addAbsentTaxo(t);
        }
    }

    public void replaceTaxonomy(Taxonomy taxo) {
        for (int i = 0; i < shownTaxonomies.size(); i++) {
            if (shownTaxonomies.get(i).getId() == taxo.getId()) {
                shownTaxonomies.set(i, taxo);
            }
        }
    }

    protected void addAbsentTaxo(Taxonomy taxo) {
        boolean isIn = false;
        for (Taxonomy t : shownTaxonomies) {
            if (t.getId() == taxo.getId()) {
                isIn = true;
            }
        }
        if (!isIn) {
            shownTaxonomies.add(taxo);
        }
    }

    private void saveExpandedAndSelectedTreeNodes() {
        expandedTreeNodes.clear();
        idOfSelectedTaxonomy = null;
        for (TreeNode tn : getAllChildren(taxonomyTree)) {
            Taxonomy t = (Taxonomy) tn.getData();
            if (tn.isExpanded()) {
                expandedTreeNodes.add(t.getId());
            }
            if (tn.isSelected()) {
                idOfSelectedTaxonomy = t.getId();
            }
        }
    }

    public void selectTaxonomy(Taxonomy t) {
        for (TreeNode n : getAllChildren(taxonomyTree)) {
            Taxonomy ta = (Taxonomy) n.getData();
            if (ta.getId() == t.getId()) {
                n.setSelected(true);
                selectedTaxonomy = n;
                idOfSelectedTaxonomy = ta.getId();
                expandTree();
            } else {
                n.setSelected(false);
            }
        }
    }

    public void initSelectionAndExpanseState() {
        if (selectedTaxonomy == null) {
            return;
        }
        idOfSelectedTaxonomy = ((Taxonomy) selectedTaxonomy.getData()).getId();
        expandedTreeNodes = getAllParents(selectedTaxonomy);
        expandTree();
    }

    private Set<Integer> getAllParents(TreeNode selectedTaxonomy) {
        Set<Integer> ids = new HashSet<>();
        while (selectedTaxonomy.getParent() != null) {
            ids.add(((Taxonomy) selectedTaxonomy.getData()).getId());
            selectedTaxonomy = selectedTaxonomy.getParent();
        }
        return ids;
    }

    /**
     *
     * @param selectedTaxonomy
     */
    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
        selectedTaxonomy.setExpanded(true);
        selectedTaxonomy.setSelected(true);
        saveExpandedAndSelectedTreeNodes();
    }

    /**
     *
     * @param taxonomyTree
     */
    public void setTaxonomyTree(TreeNode taxonomyTree) {
        this.taxonomyTree = taxonomyTree;
    }

    public TaxonomyService getTaxonomyService() {
        return taxonomyService;
    }

    public void deactivateTree() {
        for (TreeNode tn : getAllChildren(taxonomyTree)) {
            tn.setSelectable(false);
        }
    }

    public void activateTree() {
        for (TreeNode tn : getAllChildren(taxonomyTree)) {
            tn.setSelectable(true);
        }
    }

}
