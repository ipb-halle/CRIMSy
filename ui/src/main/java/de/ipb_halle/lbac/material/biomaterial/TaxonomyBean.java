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
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import java.io.Serializable;
import java.util.HashMap;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
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
    protected MaterialService materialService;
    @Inject
    protected MemberService memberService;
    @Inject
    protected TaxonomyService taxonomyService;
    @Inject
    protected MessagePresenter messagePresenter;
    @Inject
    private LoggingProfiler loggingProfiler;

    protected User currentUser;
    protected TaxonomyHistoryController historyController;
    protected TaxonomyLevelController levelController;
    protected transient Logger logger = LogManager.getLogger(this.getClass().getName());
    protected final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    protected TaxonomyNameController nameController;
    protected Mode mode;
    protected Taxonomy parentOfNewTaxo;
    protected TreeNode selectedTaxonomy;
    protected TaxonomyRenderController renderController;
    protected Taxonomy taxonomyBeforeEdit;
    protected Taxonomy taxonomyToCreate;
    protected Taxonomy taxonomyToEdit;
    protected TaxonomyTreeController treeController;
    protected TaxonomyValidityController validityController;

    public TaxonomyBean() {

    }

    public TaxonomyBean(LoggingProfiler profiler) {
        this.loggingProfiler = profiler;
    }

    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("TaxonomyBean.nameController");
        nameController = new TaxonomyNameController(this);
        loggingProfiler.profilerStop("TaxonomyBean.nameController");

        loggingProfiler.profilerStart("TaxonomyBean.levelController");
        levelController = new TaxonomyLevelController(this);
        levelController.setLevels(this.taxonomyService.loadTaxonomyLevel());
        loggingProfiler.profilerStop("TaxonomyBean.levelController");

        loggingProfiler.profilerStart("TaxonomyBean.validityController");
        validityController = new TaxonomyValidityController(this, messagePresenter);
        loggingProfiler.profilerStop("TaxonomyBean.validityController");

        loggingProfiler.profilerStart("TaxonomyBean.historyController");
        historyController = new TaxonomyHistoryController(this, nameController, taxonomyService, memberService);
        loggingProfiler.profilerStop("TaxonomyBean.historyController");

        loggingProfiler.profilerStart("TaxonomyBean.renderController");
        renderController = new TaxonomyRenderController(this, nameController, levelController, memberService, messagePresenter);
        loggingProfiler.profilerStop("TaxonomyBean.renderController");

        loggingProfiler.profilerStart("TaxonomyBean.treeController");
        treeController = new TaxonomyTreeController(loggingProfiler, selectedTaxonomy, taxonomyService, levelController);
        loggingProfiler.profilerStop("TaxonomyBean.treeController");
    }

    /**
     * Initialises all data if a new user is logged in
     *
     * @param evt
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("TaxonomyBean.setCurrentAccount");

        mode = Mode.SHOW;

        currentUser = evt.getCurrentAccount();

        levelController.setLevels(taxonomyService.loadTaxonomyLevel());

        levelController.setSelectedLevel(levelController.getRootLevel());

        treeController.initialise();

        selectedTaxonomy = (TreeNode) treeController.getTaxonomyTree().getChildren().get(0);

        initHistoryDate();

        loggingProfiler.profilerStop("TaxonomyBean.setCurrentAccount");

    }

    /**
     * Triggered by an ui element to either cancel a create or edit action or to
     * start an edit action with a choosen taxonomy
     */
    public void actionClickFirstButton() {
        if (mode == Mode.CREATE || mode == Mode.EDIT) {
            mode = Mode.SHOW;
            treeController.reorganizeTaxonomyTree();
        } else if (mode == Mode.SHOW) {
            try {
                mode = Mode.EDIT;
                taxonomyBeforeEdit = (Taxonomy) selectedTaxonomy.getData();
                taxonomyToEdit = taxonomyBeforeEdit.copyMaterial();
                treeController.disableTreeNodeEntries(taxonomyBeforeEdit);
                levelController.setSelectedLevel(taxonomyToEdit.getLevel());
            } catch (Exception e) {
                logger.error("actionClickFirstButton() caught an exception:", (Throwable) e);
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
                    Taxonomy savedTaxo = saveNewTaxonomy();
                    treeController.shownTaxonomies.add(savedTaxo);
                    treeController.reorganizeTaxonomyTree();
                    mode = Mode.SHOW;
                }
            }
            if (mode == Mode.EDIT) {
                taxonomyToEdit.setLevel(levelController.getSelectedLevel());
                materialService.saveEditedMaterial(taxonomyToEdit, taxonomyBeforeEdit, null, currentUser.getId());
                treeController.replaceTaxonomy(taxonomyToEdit);
                treeController.reorganizeTaxonomyTree();
                levelController.setSelectedLevel(taxonomyToEdit.getLevel());
                selectedTaxonomy = treeController.selectedTaxonomy;
                taxonomyBeforeEdit = null;
                taxonomyToEdit = null;
                mode = Mode.SHOW;
            }
        } catch (Exception e) {
            logger.error("actionClickSecondButton() caught an exception:", (Throwable) e);
            taxonomyBeforeEdit = null;
            taxonomyToEdit = null;
            mode = Mode.SHOW;
        }
        initHistoryDate();
    }

    public TaxonomyHistoryController getHistoryController() {
        return historyController;
    }

    public TaxonomyLevelController getLevelController() {
        return levelController;
    }

    public Mode getMode() {
        return mode;
    }

    public TaxonomyNameController getNameController() {
        return nameController;
    }

    public Taxonomy getParentOfNewTaxo() {
        return parentOfNewTaxo;
    }

    public TaxonomyRenderController getRenderController() {
        return renderController;
    }

    public TreeNode getSelectedTaxonomy() {
        return selectedTaxonomy;
    }

    public Taxonomy getTaxonomyBeforeEdit() {
        return taxonomyBeforeEdit;
    }

    public Taxonomy getTaxonomyToCreate() {
        return taxonomyToCreate;
    }

    public Taxonomy getTaxonomyToEdit() {
        return taxonomyToEdit;
    }

    public TaxonomyTreeController getTreeController() {
        return treeController;
    }

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
                    t.getHistory().getChanges().lastKey());
        }
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

    /**
     * if in edit mode the taxonomy to be edited will get the selected taxonomy
     * as its parent. If in Show mode the selected taxonomy will be displayed.
     *
     * @param event
     */
    public void onTaxonomySelect(NodeSelectEvent event) {
        if (mode == Mode.EDIT) {
            Taxonomy t = (Taxonomy) event.getTreeNode().getData();
            taxonomyToEdit.getTaxHierarchy().clear();
            taxonomyToEdit.getTaxHierarchy().add(t);
            taxonomyToEdit.getTaxHierarchy().addAll(t.getTaxHierarchy());
        } else {
            selectedTaxonomy = event.getTreeNode();
            parentOfNewTaxo = (Taxonomy) selectedTaxonomy.getData();
            initHistoryDate();
        }
    }

    /**
     * Saves the new taxonomy entry with the public readable acl
     */
    private Taxonomy saveNewTaxonomy() {
        taxonomyToCreate.setLevel(levelController.getSelectedLevel());
        if (selectedTaxonomy != null) {
            Taxonomy parent = (Taxonomy) selectedTaxonomy.getData();
            taxonomyToCreate.getTaxHierarchy().add(parent);
            taxonomyToCreate.getTaxHierarchy().addAll(parent.getTaxHierarchy());
        }
        materialService.saveMaterialToDB(
                taxonomyToCreate,
                GlobalAdmissionContext.getPublicReadACL().getId(),
                new HashMap<>(),
                currentUser);
        return taxonomyToCreate;
    }

    /**
     *
     * @param historyController
     */
    public void setHistoryController(TaxonomyHistoryController historyController) {
        this.historyController = historyController;
    }

    public void setLevelController(TaxonomyLevelController levelController) {
        this.levelController = levelController;
    }

    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setNameController(TaxonomyNameController nameController) {
        this.nameController = nameController;
    }

    public void setRenderController(TaxonomyRenderController renderController) {
        this.renderController = renderController;
    }

    public void setSelectedTaxonomy(TreeNode selectedTaxonomy) {
        this.selectedTaxonomy = selectedTaxonomy;
    }

    public void setTaxonomyBeforeEdit(Taxonomy taxonomyBeforeEdit) {
        this.taxonomyBeforeEdit = taxonomyBeforeEdit;
    }

    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public void setTaxonomyToCreate(Taxonomy taxonomyToCreate) {
        this.taxonomyToCreate = taxonomyToCreate;
    }

    public void setTaxonomyToEdit(Taxonomy taxonomyToEdit) {
        this.taxonomyToEdit = taxonomyToEdit;
    }

    public void setTreeController(TaxonomyTreeController treeController) {
        this.treeController = treeController;
    }

    public void setValidityController(TaxonomyValidityController validityController) {
        this.validityController = validityController;
    }
}
