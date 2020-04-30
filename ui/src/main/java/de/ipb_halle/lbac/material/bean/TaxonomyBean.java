/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.TaxonomyService;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import de.ipb_halle.lbac.material.subtype.taxonomy.TaxonomyLevel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.TreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;

/**
 * Bean for interacting with the ui to present and manipulate a single material
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class TaxonomyBean implements Serializable {

    protected enum Mode {
        CREATE, SHOW, EDIT, HISTORY
    }

    private List<Taxonomy> shownTaxonomies;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private User currentUser;

    private List<TaxonomyLevel> levels;
    private TaxonomyLevel selectedLevel;
    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;

    private List<MaterialName> names = new ArrayList<>();

    private TreeNode taxonomyTree;

    private TreeNode selectedTaxonomy;

    private Taxonomy taxonomyToCreate;

    private Mode mode;

    private Taxonomy parentOfNewTaxo;
    private Taxonomy taxonomyToEdit;

    @PostConstruct
    public void init() {

    }

    public void actionApplyButtonClick() {
        if (mode == Mode.SHOW) {
            mode = Mode.CREATE;
            taxonomyToCreate = createNewTaxonomy();
            return;
        }
        if (mode == Mode.CREATE) {
            boolean success = saveNewTaxonomy();
            if (success) {
                mode = Mode.SHOW;
            }
        }
        reloadTreeNode();
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        mode = Mode.SHOW;
        currentUser = evt.getCurrentAccount();
        selectedTaxonomy = null;

        levels = taxonomyService.loadTaxonomyLevel();
        selectedLevel = levels.get(0);
        reloadTreeNode();
    }

    public void reloadTreeNode() {
        try {
            Map<String, Object> cmap = new HashMap<>();
            //cmap.put("level", 1);
            shownTaxonomies = taxonomyService.loadTaxonomy(cmap, true);
            Taxonomy rootTaxo = createNewTaxonomy();
            rootTaxo.setLevel(levels.get(0));
            taxonomyTree = new DefaultTreeNode(rootTaxo, null);
            for (Taxonomy t : shownTaxonomies) {
                if (!t.getTaxHierachy().isEmpty()) {
                    TreeNode parent = getTreeNodeWithTaxonomy(t.getTaxHierachy().get(0).getId());
                    new DefaultTreeNode(t, parent);
                } else {
                    new DefaultTreeNode(t, taxonomyTree);
                }

            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public String getCurrentAction() {
        if (mode == Mode.SHOW || mode == Mode.HISTORY) {
            if (selectedTaxonomy != null) {
                Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
                return "Detail information for " + t.getFirstName();
            }
        }
        if (mode == Mode.CREATE) {
            return "Creating a new taxonomy entry";
        }
        if (mode == Mode.EDIT) {
            return "Editing taxonomy " + taxonomyToEdit.getFirstName();
        }
        return "";

    }

    public boolean isNamesVisible() {

        if (mode == Mode.CREATE) {
            return true;
        }
        if (selectedTaxonomy == null) {
            return false;
        }
        return true;
    }

    public boolean isNameEditable() {
        boolean isEditable = false;
        if (mode == Mode.EDIT) {
            isEditable = true;
        }
        if (mode == Mode.CREATE) {
            isEditable = true;
        }
        return isEditable;
    }

    public boolean isHistoryVisible() {
        boolean isVisible = false;

        if (mode == Mode.SHOW && selectedTaxonomy != null) {
            isVisible = true;
        }
        if (mode == Mode.HISTORY) {
            isVisible = true;
        }
        return isVisible;
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

    public List<String> getSimilarTaxonomy() {
        return new ArrayList<>();
    }

    public void actionChangeParent(Taxonomy taxo, Taxonomy newParent) {

    }

    public TreeNode getTaxonomyTree() {
        return taxonomyTree;
    }

    public void setTaxonomyTree(TreeNode taxonomyTree) {
        this.taxonomyTree = taxonomyTree;
    }

    public List<MaterialName> getNames() {
        if (mode == Mode.CREATE) {
            return taxonomyToCreate.getNames();
        }
        if (selectedTaxonomy != null) {
            Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
            return t.getNames();
        } else {
            return new ArrayList<>();
        }
    }

    public void setNames(List<MaterialName> names) {
        this.names = names;
    }

    public List<String> getPossibleLanguages() {
        List<String> names = new ArrayList<>();
        names.add("de");
        names.add("la");
        names.add("en");
        return names;
    }

    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
    }

    public String getApplyButtonText() {
        if (mode == Mode.CREATE) {
            return "Save";
        }
        return "Create New Taxonomy";
    }

    public boolean isApplyButtonDisabled() {
        if (mode == Mode.HISTORY) {
            return true;
        } else {
            return false;
        }
    }

    public void onTaxonomySelect(NodeSelectEvent event) {
        selectedTaxonomy = event.getTreeNode();
        parentOfNewTaxo = (Taxonomy) selectedTaxonomy.getData();

    }

    private Taxonomy createNewTaxonomy() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("", "en", 1));
        return new Taxonomy(0, names, new HazardInformation(), new StorageClassInformation(), new ArrayList<>());
    }

    private boolean saveNewTaxonomy() {
        boolean success = true;
        taxonomyToCreate.setLevel(selectedLevel);
        if (selectedTaxonomy != null) {
            Taxonomy parent = (Taxonomy) selectedTaxonomy.getData();
            taxonomyToCreate.getTaxHierachy().add(parent);
            taxonomyToCreate.getTaxHierachy().addAll(parent.getTaxHierachy());
        }
        materialService.saveMaterialToDB(taxonomyToCreate, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>());

        return success;
    }

    public TaxonomyLevel getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(TaxonomyLevel selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public List<TaxonomyLevel> getLevels() {

        if (selectedTaxonomy != null) {
            List<TaxonomyLevel> valideLevels = new ArrayList<>();
            return valideLevels;
        } else {
            return levels;
        }
    }

    public void setLevels(List<TaxonomyLevel> levels) {
        this.levels = levels;
    }

    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public String getCategoryOfChoosenTaxo() {
        if (selectedTaxonomy != null) {
            Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
            return t.getLevel().getId() + " - " + t.getLevel().getName();
        } else {
            return "";
        }
    }

    public boolean isCategoryVisible() {
        boolean isVisible = false;
        if (mode == Mode.SHOW && selectedTaxonomy != null) {
            isVisible = true;
        }
        return isVisible;
    }

    public boolean isCategorySelectionVisible() {
        boolean isVisible = false;
        if (mode == Mode.EDIT) {
            isVisible = true;
        }
        return isVisible;
    }

    public String getLabelForParentTaxonomy() {
        if (parentOfNewTaxo == null) {
            return "no parent choosen";
        } else {
            return parentOfNewTaxo.getNames().get(0).getValue();
        }
    }

}
