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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.service.MemberService;
import java.io.Serializable;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.TreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;

/**
 * Bean for interacting with the ui to present and manipulate a single material
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class TaxonomyBean implements Serializable {

    public enum Mode {
        CREATE, SHOW, EDIT, HISTORY
    }

    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private User currentUser;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;

    @Inject
    private MemberService memberService;

    private TreeNode selectedTaxonomy;

    private Taxonomy taxonomyToCreate;
    private Taxonomy taxonomyToEdit;
    private Taxonomy taxonomyBeforeEdit;

    private Mode mode;

    private Taxonomy parentOfNewTaxo;

    protected TaxonomyNameController nameController;
    protected TaxonomyRenderController renderController;
    protected TaxonomyValidityController validityController;
    protected TaxonomyTreeController treeController;
    protected TaxonomyLevelController levelController;
    protected TaxonomyHistoryController historyController;

    @PostConstruct
    public void init() {
        nameController = new TaxonomyNameController(this);
        levelController = new TaxonomyLevelController(this);
        levelController.setLevels(this.taxonomyService.loadTaxonomyLevel());
        validityController = new TaxonomyValidityController(this);
        historyController = new TaxonomyHistoryController(this, nameController, taxonomyService, memberService);
        renderController = new TaxonomyRenderController(this, nameController, levelController,memberService);
        treeController = new TaxonomyTreeController(selectedTaxonomy, taxonomyService, levelController);
    }

    public void actionClickFirstButton() {
        if (mode == Mode.CREATE || mode == Mode.EDIT) {
            mode = Mode.SHOW;
            treeController.reloadTreeNode();
        } else if (mode == Mode.SHOW) {
            try {
                mode = Mode.EDIT;
                taxonomyBeforeEdit = (Taxonomy) selectedTaxonomy.getData();
                taxonomyToEdit = taxonomyBeforeEdit.copyMaterial();
                treeController.disableTreeNodeEntries(taxonomyBeforeEdit);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        treeController.expandTree();
    }

    public void actionClickSecondButton() {
        try {
            if (mode == Mode.SHOW) {
                mode = Mode.CREATE;
                taxonomyToCreate = treeController.createNewTaxonomy();
                return;
            }
            if (mode == Mode.CREATE) {
                if (validityController.checkInputValidity()) {
                    saveNewTaxonomy();
                    mode = Mode.SHOW;
                }
            }
            if (mode == Mode.EDIT) {
                taxonomyToEdit.setLevel(levelController.getSelectedLevel());
                materialService.saveEditedMaterial(taxonomyToEdit, taxonomyBeforeEdit, null, currentUser.getId());
                taxonomyBeforeEdit = null;
                taxonomyToEdit = null;
                mode = Mode.SHOW;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        treeController.reloadTreeNode();
    }

    public Taxonomy getTaxonomyToCreate() {
        return taxonomyToCreate;
    }

    public void setTaxonomyToCreate(Taxonomy taxonomyToCreate) {
        this.taxonomyToCreate = taxonomyToCreate;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        mode = Mode.SHOW;
        currentUser = evt.getCurrentAccount();
        treeController.reloadTreeNode();
        levelController.setLevels(taxonomyService.loadTaxonomyLevel());
        levelController.setSelectedLevel(levelController.getLevels().get(0));
        treeController.initialise();
        selectedTaxonomy = treeController.getTaxonomyTree().getChildren().get(0);

        initHistoryDate();
    }

    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
    }

    public void onTaxonomySelect(NodeSelectEvent event) {
        if (mode == Mode.EDIT) {
            Taxonomy t = (Taxonomy) event.getTreeNode().getData();
            taxonomyToEdit.getTaxHierachy().clear();
            taxonomyToEdit.getTaxHierachy().add(t);
            taxonomyToEdit.getTaxHierachy().addAll(t.getTaxHierachy());
        } else {
            selectedTaxonomy = event.getTreeNode();
            parentOfNewTaxo = (Taxonomy) selectedTaxonomy.getData();
            initHistoryDate();

        }
    }

    public void initHistoryDate() {
        Taxonomy t = (Taxonomy) selectedTaxonomy.getData();
        if (t.getHistory().getChanges().isEmpty()) {
            historyController.setDateOfShownHistory(null);
        } else {
            historyController.setDateOfShownHistory(
                    t
                            .getHistory()
                            .getChanges()
                            .lastKey());
        }
    }

    private void saveNewTaxonomy() {

        taxonomyToCreate.setLevel(levelController.getSelectedLevel());
        if (selectedTaxonomy != null) {
            Taxonomy parent = (Taxonomy) selectedTaxonomy.getData();
            taxonomyToCreate.getTaxHierachy().add(parent);
            taxonomyToCreate.getTaxHierachy().addAll(parent.getTaxHierachy());
        }
        materialService.saveMaterialToDB(taxonomyToCreate, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>());

    }

    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public Taxonomy getTaxonomyToEdit() {
        return taxonomyToEdit;
    }

    public void setTaxonomyToEdit(Taxonomy taxonomyToEdit) {
        this.taxonomyToEdit = taxonomyToEdit;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Taxonomy getParentOfNewTaxo() {
        return parentOfNewTaxo;
    }

    public TaxonomyNameController getNameController() {
        return nameController;
    }

    public void setNameController(TaxonomyNameController nameController) {
        this.nameController = nameController;
    }

    public Taxonomy getTaxonomyBeforeEdit() {
        return taxonomyBeforeEdit;
    }

    public void setTaxonomyBeforeEdit(Taxonomy taxonomyBeforeEdit) {
        this.taxonomyBeforeEdit = taxonomyBeforeEdit;
    }

    public TaxonomyRenderController getRenderController() {
        return renderController;
    }

    public void setRenderController(TaxonomyRenderController renderController) {
        this.renderController = renderController;
    }

    public TaxonomyValidityController getValidityController() {
        return validityController;
    }

    public void setValidityController(TaxonomyValidityController validityController) {
        this.validityController = validityController;
    }

    public TaxonomyTreeController getTreeController() {
        return treeController;
    }

    public void setTreeController(TaxonomyTreeController treeController) {
        this.treeController = treeController;
    }

    public TaxonomyLevelController getLevelController() {
        return levelController;
    }

    public void setLevelController(TaxonomyLevelController levelController) {
        this.levelController = levelController;
    }

    public void onTaxonomyExpand(NodeExpandEvent event) {
        if (selectedTaxonomy == null) {
            treeController.setSelectedTaxonomy(event.getTreeNode());
        }
        event.getTreeNode().setExpanded(true);
    }

    public void onTaxonomyCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }

    public TaxonomyHistoryController getHistoryController() {
        return historyController;
    }

    public void setHistoryController(TaxonomyHistoryController historyController) {
        this.historyController = historyController;
    }

}
