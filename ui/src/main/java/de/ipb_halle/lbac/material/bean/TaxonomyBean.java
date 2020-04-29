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
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.TreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;

/**
 * Bean for interacting with the ui to present and manipulate a single material
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class TaxonomyBean implements Serializable {

    private enum Mode {
        CREATE, SHOW, EDIT, HISTORY
    }

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
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        mode = Mode.SHOW;
        currentUser = evt.getCurrentAccount();
        selectedTaxonomy = null;

        levels = taxonomyService.loadTaxonomyLevel();
        selectedLevel = levels.get(0);
        
        
//        List<MaterialName> names = new ArrayList<>();
//        names.add(new MaterialName("Life", "en", 1));
//
//        Taxonomy root = new Taxonomy(0, names, 0, new HazardInformation(), new StorageClassInformation(), new ArrayList<>());
//        root.setLevel(levels.get(0));
//
//        List<MaterialName> names_t1 = new ArrayList<>();
//        names_t1.add(new MaterialName("Bakterien", "en", 1));
//        Taxonomy t1 = new Taxonomy(0, names_t1, 0, new HazardInformation(), new StorageClassInformation(), new ArrayList<>());
//        t1.setLevel(levels.get(1));
//
//        List<MaterialName> names_t2 = new ArrayList<>();
//        names_t2.add(new MaterialName("Tiere", "en", 1));
//
//        Taxonomy t2 = new Taxonomy(0, names_t2, 0, new HazardInformation(), new StorageClassInformation(), new ArrayList<>());
//        t2.setLevel(levels.get(1));
//        try {
//            taxonomyTree = new DefaultTreeNode(root, null);
//            logger.info(root.getFirstName());
//            new DefaultTreeNode(t1, taxonomyTree);
//            logger.info(t1.getFirstName());
//            new DefaultTreeNode(t2, taxonomyTree);
//            logger.info(t2.getFirstName());
//        } catch (Exception e) {
//            logger.info("Crash!!");
//        }

//          taxonomyTree = new DefaultTreeNode("hh", null);
//        TreeNode male = new DefaultTreeNode("male", taxonomyTree);
//        TreeNode female = new DefaultTreeNode("female", taxonomyTree);
//        TreeNode t1 = new DefaultTreeNode("woman1", female);
//        TreeNode t2 = new DefaultTreeNode("woman2", female);
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
        return levels;
    }

    public void setLevels(List<TaxonomyLevel> levels) {
        this.levels = levels;
    }

    public String getLabelForParentTaxonomy() {
        if (parentOfNewTaxo == null) {
            return "no parent choosen";
        } else {
            return parentOfNewTaxo.getNames().get(0).getValue();
        }
    }

}
