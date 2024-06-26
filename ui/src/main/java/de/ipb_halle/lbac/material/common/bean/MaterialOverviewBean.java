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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.reporting.ReportMgr;
import de.ipb_halle.lbac.util.resources.ResourceLocation;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.util.NonEmpty;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import de.ipb_halle.reporting.Report;
import de.ipb_halle.reporting.ReportType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the presentation of readable materials of the logged in user.
 *
 * @author fmauz
 */
@Named
@SessionScoped
public class MaterialOverviewBean implements Serializable, ACObjectBean {

    private static final long serialVersionUID = 1L;

    private ACObjectController acObjectController;
    private User currentUser;
    private Logger logger;
    private List<Material> materials = new ArrayList<>();
    private Material materialInFocus;
    private NamePresenter namePresenter;
    private MaterialSearchMaskController searchController;
    private MaterialTableController tableController;

    @Inject
    private MessagePresenter messagePresenter;
    private final String NAVIGATION_ITEM_EDIT = "item/itemEdit";
    private final String NAVIGATION_MATERIAL_EDIT = "material/materialsEdit";
    private final int HAZARD_RADIACTIVE_ID = 16;
    private final int HAZARD_ATTENTION_ID = 18;
    private final int HAZARD_DANGER_ID = 19;

    private Report selectedReport;
    private List<Report> availableReports;

    @Inject
    private ItemBean itemBean;

    @Inject
    private MaterialBean materialEditBean;

    @Inject
    protected MaterialService materialService;

    @Inject
    private MemberService memberService;

    @Inject
    private Navigator navigator;

    @Inject
    private ProjectService projectService;

    @Inject
    protected HazardService hazardService;

    @Inject
    private ReportMgr reportMgr;

    @Inject
    protected LoggingProfiler loggingProfiler;

    public MaterialOverviewBean() {
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Creates the tablecontroller and the controller for managing the search
     * values
     */
    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("MaterialOverviewBean");

        tableController = new MaterialTableController(materialService);
        this.searchController = new MaterialSearchMaskController(
                this,
                tableController,
                materialService,
                projectService,
                memberService,
                Arrays.asList(
                        MaterialType.COMPOSITION,
                        MaterialType.BIOMATERIAL,
                        MaterialType.CONSUMABLE,
                        MaterialType.SEQUENCE,
                        MaterialType.STRUCTURE));
        namePresenter = new NamePresenter();

        loggingProfiler.profilerStop("MaterialOverviewBean");
    }

    /**
     *
     * @param evt
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("MaterialOverviewBean.setCurrentAccount");
        currentUser = evt.getCurrentAccount();
        tableController.setLastUser(currentUser);
        searchController.clearInputFields();
        searchController.actionStartMaterialSearch();
        loggingProfiler.profilerStop("MaterialOverviewBean.setCurrentAccount");
    }

    public MaterialTableController getTableController() {
        return tableController;
    }

    /**
     * Creates a html tag to show the materialnames in a an appropriate way for
     * the ui
     *
     * @param material
     * @param maxNamesShown
     * @return
     */
    public String getWrappedNames(Material material, int maxNamesShown) {
        return namePresenter.getFormatedNames(material, maxNamesShown);
    }

    public List<Material> getReadableMaterials() {
        return tableController.getShownMaterials();
    }

    public boolean isDetailSubComponentVisisble(String type, Material mat) {
        return MaterialType.valueOf(type) == mat.getType();
    }

    public boolean isNotAllowed(Material m, String action) {
        return false;
    }

    public void actionCreateNewMaterial() {
        materialEditBean.startMaterialCreation();
        navigator.navigate(NAVIGATION_MATERIAL_EDIT);
    }

    public void actionEditMaterial(Material m) {
        try {
            m.setHistory(materialService.loadHistoryOfMaterial(m.getId()));

            materialEditBean.startMaterialEdit(m);
        } catch (Exception e) {
            logger.error("actionEditMaterial() caught an exception:", (Throwable) e);

        }
        navigator.navigate(NAVIGATION_MATERIAL_EDIT);
    }

    public void actionDeactivateMaterial(Material m) {
        materialService.deactivateMaterial(
                m.getId(),
                currentUser);
        searchController.actionStartMaterialSearch();
    }

    public void actionCreateNewItem(Material m) {
        itemBean.actionStartItemCreation(m);
        navigator.navigate(NAVIGATION_ITEM_EDIT);
    }

    public Report getSelectedReport() {
        return selectedReport;
    }

    public void setSelectedReport(Report selectedReport) {
        this.selectedReport = selectedReport;
    }

    public void actionCreateReport() {
        reportMgr.submitReport(selectedReport, collectReportParameters(), ReportType.PDF, currentUser);
        messagePresenter.info("reporting_reportSubmittedGrowlMsg");
    }

    private Map<String, Object> collectReportParameters() {
        Map<String, Object> reportParams = new HashMap<String, Object>();

        reportParams.put("paramCurrentUserId", currentUser.getId());
        reportParams.put("paramOwnerId", GlobalAdmissionContext.OWNER_ACCOUNT_ID);

        reportParams.put("paramMaterialName", NonEmpty.nullOrNonEmpty(searchController.getName()));
        reportParams.put("paramMaterialId", NonEmpty.nullOrNonZero(searchController.getId()));
        reportParams.put("paramUserName", NonEmpty.nullOrNonEmpty(searchController.getUserName()));
        reportParams.put("paramProjectName", NonEmpty.nullOrNonEmpty(searchController.getProjectName()));
        reportParams.put("paramIndex", NonEmpty.nullOrNonEmpty(searchController.getIndex()));

        MaterialType materialType = searchController.getMaterialType();
        reportParams.put("paramMaterialType", materialType == null ? null : materialType.getId());

        String molfile = searchController.getMolecule();
        reportParams.put("paramMolQuery", new Molecule(molfile, -1).isEmptyMolecule() ? null : molfile);

        return reportParams;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public MaterialSearchMaskController getSearchController() {
        return searchController;
    }

    @Override
    public void applyAclChanges() {
        materialService.updateMaterialAcList(materialInFocus);
        searchController.actionStartMaterialSearch();
        materialInFocus = null;
    }

    @Override
    public void cancelAclChanges() {
        materialInFocus = null;
    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        materialInFocus = (Material) aco;
        acObjectController = new ACObjectController(
                materialInFocus,
                memberService.loadGroups(
                        new HashMap<>()),
                this,
                materialInFocus.getFirstName());
    }

    @Override
    public ACObjectController getAcObjectController() {
        return acObjectController;
    }

    public List<String> getImageLocationOfHazards(Material m) {
        List<String> locations = new ArrayList<>();
        for (HazardType ht : m.getHazards().getHazards().keySet()) {
            if (ht.getCategory() == HazardType.Category.GHS) {
                if (ht.getId() != HAZARD_ATTENTION_ID && ht.getId() != HAZARD_DANGER_ID) {
                    locations.add(ResourceLocation.getHazardImageLocation(ht.getName()));
                }
            }
        }
        return locations;
    }

    public String getHazardRemark(Material m, int hazardId) {
        for (HazardType h : m.getHazards().getHazards().keySet()) {
            if (h.getId() == hazardId) {
                return m.getHazards().getHazards().get(h);
            }
        }
        return "";
    }

    public boolean hasHazard(Material m, int hazardId) {
        for (HazardType h : m.getHazards().getHazards().keySet()) {
            if (h.getId() == hazardId) {
                return true;
            }
        }
        return false;
    }

    public boolean isRadioactive(Material m) {
        for (HazardType ht : m.getHazards().getHazards().keySet()) {
            if (ht.getId() == HAZARD_RADIACTIVE_ID) {
                return true;
            }
        }
        return false;
    }

    public String getRadioactiveImageLocation() {
        return ResourceLocation.getHazardImageLocation(hazardService.getHazardById(16));
    }

    public String getLocalizedMaterialType(Material m) {
        return messagePresenter.presentMessage(
                "search_category_" + m.getType());
    }

    public boolean hasAccessRight(Material m, String accessRight) {
        try {
            ACPermission permission = ACPermission.valueOf(accessRight);
            return materialService.getAcListService().isPermitted(permission, m, currentUser);
        } catch (Exception e) {
            logger.error("hasAccessRight() caught an exception:", (Throwable) e);
            return false;
        }
    }

    public String getComponentOfComposition(Concentration conc) {
        Double value = conc.getConcentration();
        String valueString = "";
        if (isPrintableValue(value)) {
            valueString = String.format(Locale.US, " %.4f", value) + conc.getUnitString();
        }
        String name = conc.getMaterialName();
        if (conc.getMaterial().getNames().isEmpty()) {
            name = "Materialid: " + conc.getMaterialName();
        }

        return "-" + valueString + " " + name;
    }

    private boolean isPrintableValue(Double value) {
        if (value == null) {
            return false;
        }
        return Double.isFinite(value)
                && !Double.isNaN(value);

    }
}
