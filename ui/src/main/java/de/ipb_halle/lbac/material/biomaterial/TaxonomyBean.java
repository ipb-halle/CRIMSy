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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.admission.MemberService;
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
 * Bean for interacting with the ui to present and manipulate a the taxonomy
 * tree
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class TaxonomyBean implements Serializable {

    public enum Mode {
        CREATE, SHOW, EDIT, HISTORY
    }
    @Inject
    private MaterialService materialService;
    @Inject
    private MemberService memberService;
    @Inject
    private TaxonomyService taxonomyService;

    private User currentUser;
    protected TaxonomyHistoryController historyController;
    protected TaxonomyLevelController levelController;
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    protected TaxonomyNameController nameController;
    private Mode mode;
    private Taxonomy parentOfNewTaxo;
    private TreeNode selectedTaxonomy;
    protected TaxonomyRenderController renderController;
    private Taxonomy taxonomyBeforeEdit;
    private Taxonomy taxonomyToCreate;
    private Taxonomy taxonomyToEdit;
    protected TaxonomyTreeController treeController;
    protected TaxonomyValidityController validityController;

    @PostConstruct
    public void init() {
        nameController = new TaxonomyNameController(this);
        levelController = new TaxonomyLevelController(this);
        levelController.setLevels(this.taxonomyService.loadTaxonomyLevel());
        validityController = new TaxonomyValidityController(this);
        historyController = new TaxonomyHistoryController(this, nameController, taxonomyService, memberService);
        renderController = new TaxonomyRenderController(this, nameController, levelController, memberService);
        treeController = new TaxonomyTreeController(selectedTaxonomy, taxonomyService, levelController);
    }

    /**
     * Initialises all data if a new user is logged in
     *
     * @param evt
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        mode = Mode.SHOW;
        currentUser = evt.getCurrentAccount();
        treeController.reloadTreeNode();
        levelController.setLevels(taxonomyService.loadTaxonomyLevel());
        levelController.setSelectedLevel(levelController.getRootLevel());
        treeController.initialise();
        selectedTaxonomy = treeController.getTaxonomyTree().getChildren().get(0);
        initHistoryDate();
    }

    /**
     * Triggered by an ui element to either cancel a create or edit action or to
     * start an edit action with a choosen taxonomy
     */
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
                levelController.setSelectedLevel(taxonomyToEdit.getLevel());
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     * Triggered by an ui element to either switch to edit or create mode or to
     * save a new or edited taxonomy
     */
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
                    treeController.reloadTreeNode();
                    mode = Mode.SHOW;
                }
            }
            if (mode == Mode.EDIT) {
                taxonomyToEdit.setLevel(levelController.getSelectedLevel());
                materialService.saveEditedMaterial(taxonomyToEdit, taxonomyBeforeEdit, null, currentUser.getId());
                treeController.reloadTreeNode();
                levelController.setSelectedLevel(taxonomyToEdit.getLevel());
                selectedTaxonomy = treeController.selectedTaxonomy;
                taxonomyBeforeEdit = null;
                taxonomyToEdit = null;
                mode = Mode.SHOW;
            }
        } catch (Exception e) {
            logger.error(e);
            taxonomyBeforeEdit = null;
            taxonomyToEdit = null;
            mode = Mode.SHOW;
        }
        initHistoryDate();
    }

    /**
     *
     * @return
     */
    public TaxonomyHistoryController getHistoryController() {
        return historyController;
    }

    /**
     *
     * @return
     */
    public TaxonomyLevelController getLevelController() {
        return levelController;
    }

    /**
     *
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     *
     * @return
     */
    public TaxonomyNameController getNameController() {
        return nameController;
    }

    /**
     *
     * @return
     */
    public Taxonomy getParentOfNewTaxo() {
        return parentOfNewTaxo;
    }

    /**
     *
     * @return
     */
    public TaxonomyRenderController getRenderController() {
        return renderController;
    }

    /**
     *
     * @return
     */
    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    /**
     *
     * @return
     */
    public Taxonomy getTaxonomyBeforeEdit() {
        return taxonomyBeforeEdit;
    }

    /**
     *
     * @return
     */
    public Taxonomy getTaxonomyToCreate() {
        return taxonomyToCreate;
    }

    /**
     *
     * @return
     */
    public Taxonomy getTaxonomyToEdit() {
        return taxonomyToEdit;
    }

    /**
     *
     * @return
     */
    public TaxonomyTreeController getTreeController() {
        return treeController;
    }

    /**
     *
     * @return
     */
    public TaxonomyValidityController getValidityController() {
        return validityController;
    }

    /**
     * looks for the last edit of a taxonomy and sets its date
     */
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

    /**
     *
     * @param event
     */
    public void onTaxonomyCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }

    /**
     *
     * @param event
     */
    public void onTaxonomyExpand(NodeExpandEvent event) {
        if (selectedTaxonomy == null) {
            treeController.setSelectedTaxonomy(event.getTreeNode());
        }
        event.getTreeNode().setExpanded(true);
    }

    /**
     * if in edit mode the taxonomy to be edited will get the selected taxonomy
     * as its parent. If in Show mode the selected taxonomy will be displayed.
     *
     * @param event
     */
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

    /**
     * Saves the new taxonomy entry with the public readable acl
     */
    private void saveNewTaxonomy() {
        taxonomyToCreate.setLevel(levelController.getSelectedLevel());
        if (selectedTaxonomy != null) {
            Taxonomy parent = (Taxonomy) selectedTaxonomy.getData();
            taxonomyToCreate.getTaxHierachy().add(parent);
            taxonomyToCreate.getTaxHierachy().addAll(parent.getTaxHierachy());
        }
        materialService.saveMaterialToDB(taxonomyToCreate, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>());

    }

    /**
     *
     * @param historyController
     */
    public void setHistoryController(TaxonomyHistoryController historyController) {
        this.historyController = historyController;
    }

    /**
     *
     * @param levelController
     */
    public void setLevelController(TaxonomyLevelController levelController) {
        this.levelController = levelController;
    }

    /**
     *
     * @param materialService
     */
    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     *
     * @param nameController
     */
    public void setNameController(TaxonomyNameController nameController) {
        this.nameController = nameController;
    }

    /**
     *
     * @param renderController
     */
    public void setRenderController(TaxonomyRenderController renderController) {
        this.renderController = renderController;
    }

    /**
     *
     * @param selectedTaxonomy
     */
    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
    }

    /**
     *
     * @param taxonomyBeforeEdit
     */
    public void setTaxonomyBeforeEdit(Taxonomy taxonomyBeforeEdit) {
        this.taxonomyBeforeEdit = taxonomyBeforeEdit;
    }

    /**
     *
     * @param taxonomyService
     */
    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    /**
     *
     * @param taxonomyToCreate
     */
    public void setTaxonomyToCreate(Taxonomy taxonomyToCreate) {
        this.taxonomyToCreate = taxonomyToCreate;
    }

    public void setTaxonomyToEdit(Taxonomy taxonomyToEdit) {
        this.taxonomyToEdit = taxonomyToEdit;
    }

    /**
     *
     * @param treeController
     */
    public void setTreeController(TaxonomyTreeController treeController) {
        this.treeController = treeController;
    }

    /**
     *
     * @param validityController
     */
    public void setValidityController(TaxonomyValidityController validityController) {
        this.validityController = validityController;
    }
}
