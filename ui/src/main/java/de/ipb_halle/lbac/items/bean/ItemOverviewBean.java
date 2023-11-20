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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.reporting.ReportMgr;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.NonEmpty;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import jakarta.annotation.PostConstruct;
import de.ipb_halle.reporting.Report;
import de.ipb_halle.reporting.ReportType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ItemOverviewBean implements Serializable, ACObjectBean {

    private transient Logger logger;
    private final int PAGE_SIZE = 10;

    @Inject
    protected ContainerService containerService;
    @Inject
    protected ItemBean itemBean;
    @Inject
    protected ItemService itemService;
    @Inject
    protected Navigator navigator;
    @Inject
    protected MaterialService materialService;
    @Inject
    protected MemberService memberService;
    @Inject
    protected ProjectService projectService;
    @Inject
    protected NodeService nodeService;
    @Inject
    protected ACListService aclistService;
    @Inject
    protected ReportMgr reportMgr;
    @Inject
    private transient MessagePresenter messagePresenter;
    @Inject
    protected LoggingProfiler loggingProfiler;

    private ACObjectController acObjectController;
    protected User currentUser;
    private int firstResult;
    private int itemAmount;
    private Item itemInFocus;
    protected ItemLocaliser itemLocaliser;
    private SearchResult searchResult;
    protected SearchMaskValues searchMaskValues;

    private Report selectedReport;
    private ReportType selectedReportType;

    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("ItemOverviewBean");
        logger = LogManager.getLogger(this.getClass().getName());
        itemLocaliser = new ItemLocaliser();
        searchMaskValues = new SearchMaskValues();
        loggingProfiler.profilerStop("ItemOverviewBean");
    }

    public void actionApplySearchFilter() {
        actionFirstItems();
    }

    public void actionClearSearchFilter() {
        searchMaskValues = new SearchMaskValues();
    }

    public void actionFirstItems() {
        firstResult = 0;
        reloadItems();
    }

    public void actionLastItems() {
        firstResult -= PAGE_SIZE;
        firstResult = Math.max(0, firstResult);
        reloadItems();
    }

    public void actionNextItems() {
        firstResult += PAGE_SIZE;
        firstResult = Math.min(firstResult, itemAmount - PAGE_SIZE);
        reloadItems();
    }

    public void actionEndItems() {
        firstResult = itemAmount - PAGE_SIZE;
        firstResult = Math.max(0, firstResult);
        reloadItems();
    }

    public List<Report> getAvailableReports() {
        return reportMgr.getAvailableReports(this.getClass().getName());
    }

    public Report getSelectedReport() {
        return selectedReport;
    }

    public void setSelectedReport(Report selectedReport) {
        this.selectedReport = selectedReport;
    }

    public ReportType[] getReportTypes() {
        return ReportType.values();
    }

    public ReportType getSelectedReportType() {
        return selectedReportType;
    }

    public void setSelectedReportType(ReportType selectedReportType) {
        this.selectedReportType = selectedReportType;
    }

    public void actionCreateReport() {
        reportMgr.submitReport(selectedReport, collectReportParameters(), selectedReportType, currentUser);
        messagePresenter.info("reporting_reportSumbittedGrowlMsg");
    }

    private Map<String, Object> collectReportParameters() {
        Map<String, Object> reportParams = new HashMap<String, Object>();

        reportParams.put("paramCurrentUserId", currentUser.getId());
        reportParams.put("paramOwnerId", GlobalAdmissionContext.OWNER_ACCOUNT_ID);

        reportParams.put("paramMaterialName", NonEmpty.nullOrNonEmpty(searchMaskValues.getMaterialName()));
        reportParams.put("paramItemLabel", NonEmpty.nullOrNonEmpty(searchMaskValues.getLabel()));
        reportParams.put("paramUserName", NonEmpty.nullOrNonEmpty(searchMaskValues.getUserName()));
        reportParams.put("paramProjectName", NonEmpty.nullOrNonEmpty(searchMaskValues.getProjectName()));
        reportParams.put("paramLocation", NonEmpty.nullOrNonEmpty(searchMaskValues.getLocation()));
        reportParams.put("paramDescription", NonEmpty.nullOrNonEmpty(searchMaskValues.getDescription()));

        return reportParams;
    }

    public void actionStartItemEdit(Item i) {
        itemBean.actionStartItemEdit(i);
        navigator.navigate("/item/itemEdit");
    }

    public List<Item> getItems() {
        return searchResult.getAllFoundObjects(Item.class, nodeService.getLocalNode());
    }

    public void reloadItems() {
        itemAmount = itemService.loadItemAmount(createSearchRequest());
        searchResult = itemService.loadItems(createSearchRequest());
        itemLocaliser.localiseContainerNamesOf(searchResult.getAllFoundObjects(Item.class, nodeService.getLocalNode()));
    }

    private SearchRequest createSearchRequest() {
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(currentUser, firstResult, PAGE_SIZE);
        builder.setSearchMaskValues(searchMaskValues);
        return builder.build();
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("ItemOverviewBean.setCurrentAccount");

        currentUser = evt.getCurrentAccount();
        firstResult = 0;
        searchMaskValues = new SearchMaskValues();
        reloadItems();
        loggingProfiler.profilerStop("ItemOverviewBean.setCurrentAccount");

    }

    public int getItemAmount() {
        return itemAmount;
    }

    public boolean isBackDeactivated() {
        return firstResult == 0;
    }

    public boolean isForwardDeactivated() {
        return (itemAmount - firstResult) <= PAGE_SIZE;
    }

    public int getLeftBorder() {
        return firstResult + 1;
    }

    public int getRightBorder() {
        return Math.min(PAGE_SIZE + firstResult, itemAmount);
    }

    public List<String> getSimilarMaterialNames(String input) {
        return materialService.getSimilarMaterialNames(input, currentUser);
    }

    public List<String> getSimilarProjectNames(String input) {
        return projectService.getSimilarProjectNames(input, currentUser);
    }

    public List<String> getSimilarUserNames(String input) {
        return new ArrayList<>(memberService.loadSimilarUserNames(input));
    }

    public List<String> getSimilarContainerNames(String input) {
        List<String> names = new ArrayList<>();
        Set<Container> container = containerService.getSimilarContainerNames(input, currentUser);
        for (Container c : container) {
            names.add(c.getLabel());
        }
        return names;
    }

    @Override
    public ACObjectController getAcObjectController() {
        return acObjectController;
    }

    @Override
    public void applyAclChanges() {
        itemService.saveItem(itemInFocus);
        ItemHistory h = new ItemHistory();
        h.setAction("EDIT");
        h.setActor(currentUser);
        h.setItem(itemInFocus);
        h.setMdate(new Date());
        h.setAcListChange(acObjectController.getOriginalAcList(), itemInFocus.getACList());
        itemService.saveItemHistory(h);

        reloadItems();
    }

    @Override
    public void cancelAclChanges() {

    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        itemInFocus = (Item) aco;
        acObjectController = new ACObjectController(aco, memberService.loadGroups(new HashMap<>()), this, itemInFocus.getDescription());
    }

    public SearchMaskValues getSearchMaskValues() {
        return searchMaskValues;
    }

    public boolean isEditDeactivated(Item item) {
        return !aclistService.isPermitted(ACPermission.permEDIT, item, currentUser);
    }
}
