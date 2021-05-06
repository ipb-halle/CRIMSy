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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.history.ItemComparator;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.items.entity.ItemHistoryEntity;
import de.ipb_halle.lbac.items.entity.ItemPositionsHistoryEntity;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MemberService;

import de.ipb_halle.lbac.items.Code25LabelGenerator;
import de.ipb_halle.lbac.items.search.ItemSearchConditionBuilder;
import de.ipb_halle.lbac.label.LabelService;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.inaccessible.InaccessibleMaterial;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.DbField;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.OrderDirection;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.SqlCountBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class ItemService {

    @Inject
    private LabelService labelService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ArticleService articleService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ContainerPositionService containerPositionService;

    @Inject
    private MaterialService materialService;

    @Inject
    private MemberService memberService;

    @Inject
    private ProjectService projectService;

    @Inject
    private ACListService aclistService;

    @Inject
    private NodeService nodeService;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Code25LabelGenerator labelGenerator;
    private Date time;

    @PostConstruct
    public void init() {
        labelGenerator = new Code25LabelGenerator();
    }

    public SearchResult loadItems(SearchRequest request) {
        time = new Date();
        logger.info(new Date());
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        ItemEntityGraphBuilder graphBuilder = new ItemEntityGraphBuilder();
        EntityGraph itemGraph = graphBuilder.buildEntityGraph(true);
        SqlBuilder sqlBuilder = new SqlBuilder(itemGraph);
        ItemSearchConditionBuilder conditionBuilder = new ItemSearchConditionBuilder(
                itemGraph, "items");
        Condition condition = conditionBuilder.convertRequestToCondition(request, ACPermission.permREAD);
        String sql = sqlBuilder.query(
                condition,
                createOrderList());

        Query q = createQueryWithParams(sqlBuilder, sql, ItemEntity.class);
        q.setFirstResult(request.getFirstResult());
        q.setMaxResults(request.getMaxResults());
        logger.info(new Date().getTime() - time.getTime());
        List<ItemEntity> entities = q.getResultList();
        for (ItemEntity ie : entities) {
            Item item = createItemFromEntity(ie, request.getUser());
            logger.info("  " + (new Date().getTime() - time.getTime()));
            item.setHistory(loadHistoryOfItem(item));
            result.addResult(item);
        }
        logger.info(new Date().getTime() - time.getTime());

        return result;
    }

    public int loadItemAmount(SearchRequest request) {
        ItemEntityGraphBuilder graphBuilder = new ItemEntityGraphBuilder();
        EntityGraph itemGraph = graphBuilder.buildEntityGraph(true);
        SqlCountBuilder countBuilder = new SqlCountBuilder(
                itemGraph,
                new Attribute("items", AttributeType.ID));

        ItemSearchConditionBuilder conditionBuilder = new ItemSearchConditionBuilder(itemGraph, "items");
        Condition condition = conditionBuilder.convertRequestToCondition(request, ACPermission.permREAD);
        String sql = countBuilder.query(condition);
        Query q = createQueryWithParams(countBuilder, sql);
        BigInteger bi = (BigInteger) q.getResultList().get(0);
        return bi.intValue();
    }

    private Query createQueryWithParams(SqlBuilder builder, String sql) {
        return createQueryWithParams(builder, sql, null);
    }

    private Query createQueryWithParams(SqlBuilder builder, String sql, Class entityClass) {
        Query q;
        if (entityClass == null) {
            q = em.createNativeQuery(sql);
        } else {
            q = em.createNativeQuery(sql, entityClass);
        }
        for (Value param : builder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        return q;
    }

    private Item createItemFromEntity(ItemEntity entity, User user) {
        Material m = materialService.loadMaterialById(entity.getMaterialid());
        logger.info("Material loaded " + (new Date().getTime() - time.getTime()));
        if (!aclistService.isPermitted(ACPermission.permREAD, m, user)) {
            m = InaccessibleMaterial.createNewInstance(GlobalAdmissionContext.getPublicReadACL());
        }
        Item item = new Item(entity,
                entity.getArticleid() == null ? null : articleService.loadArticleById(entity.getArticleid()),
                entity.getContainerid() == null ? null : containerService.loadSlimContainerById(entity.getContainerid()),
                m,
                memberService.loadUserById(entity.getOwner()),
                entity.getProjectid() == null ? null : projectService.loadProjectById(entity.getProjectid()),
                entity.getSolventid() == null ? null : loadSolventById(entity.getSolventid()),
                entity.getContainerid() == null ? new ArrayList<>() : Arrays.asList(containerService.loadSlimContainerById(entity.getContainerid())),
                aclistService.loadById(entity.getACList())
        );
        logger.info("Details loaded " + (new Date().getTime() - time.getTime()));
        return item;
    }

    public Item loadItemById(int id) {
        ItemEntity entity = this.em.find(ItemEntity.class, id);
        Item item = new Item(entity,
                entity.getArticleid() == null ? null : articleService.loadArticleById(entity.getArticleid()),
                entity.getContainerid() == null ? null : containerService.loadContainerById(entity.getContainerid()),
                materialService.loadMaterialById(entity.getMaterialid()),
                memberService.loadUserById(entity.getOwner()),
                entity.getProjectid() == null ? null : projectService.loadProjectById(entity.getProjectid()),
                entity.getSolventid() == null ? null : loadSolventById(entity.getSolventid()),
                containerService.loadNestedContainer(entity.getContainerid()),
                aclistService.loadById(entity.getACList()));
        item.setHistory(loadHistoryOfItem(item));
        return item;
    }

    public Item loadItemByIdWithoutContainer(int id) {
        ItemEntity entity = this.em.find(ItemEntity.class, id);

        return new Item(entity,
                entity.getArticleid() == null ? null : articleService.loadArticleById(entity.getArticleid()),
                null,
                materialService.loadMaterialById(entity.getMaterialid()),
                memberService.loadUserById(entity.getOwner()),
                entity.getProjectid() == null ? null : projectService.loadProjectById(entity.getProjectid()),
                entity.getSolventid() == null ? null : loadSolventById(entity.getSolventid()),
                new ArrayList<>(),
                aclistService.loadById(entity.getACList()));
    }

    /**
     *
     * @param id
     * @return
     */
    public Solvent loadSolventById(int id) {
        Query q = em.createNativeQuery("SELECT name FROM solvents WHERE id=:sid");
        q.setParameter("sid", id);
        String solventName = (String) q.getSingleResult();
        return new Solvent(id, solventName, solventName);
    }

    public List<Solvent> loadSolvents() {
        List<Solvent> solvents = new ArrayList<>();
        Query q = em.createNativeQuery("SELECT id,name FROM solvents");
        for (Object[] o : (List<Object[]>) q.getResultList()) {
            int id = (int) o[0];
            String name = (String) o[1];
            solvents.add(new Solvent(id, name, name));
        }

        return solvents;
    }

    /**
     * /**
     *
     * @param item
     * @return
     */
    public Item saveItem(Item item) {
        item.setACList(aclistService.save(item.getACList()));
        ItemEntity entity = item.createEntity();

        entity = em.merge(entity);
        if (entity.getLabel() == null) {
            String label = labelService.createLabel(entity.getId(), Item.class);
            labelService.saveItemLabel(label, entity.getId());
        }
        item.setId(entity.getId());
        return item;
    }

    public Item saveEditedItem(Item editedItem, Item origItem, User user) {
        return saveEditedItem(editedItem, origItem, user, new HashSet<>());
    }

    public Item saveEditedItem(Item editedItem, Item origItem, User user, Set<int[]> newPositions) {
        Date mdate = new Date();
        ItemComparator comparator = new ItemComparator(mdate);
        ItemHistory history = comparator.compareItems(origItem, editedItem, user);
        if (history != null) {
            saveItem(editedItem);
            saveItemHistory(history);
        }

        containerPositionService.moveItemToNewPosition(origItem, editedItem.getContainer(), newPositions, user, mdate);
        if (editedItem.getContainer() != null) {
            editedItem.setContainer(containerService.loadContainerById(editedItem.getContainer().getId()));
        }
        return editedItem;
    }

    public void saveItemHistory(ItemHistory history) {
        this.em.merge(history.createEntity());
    }

    public SortedMap<Date, ItemPositionHistoryList> loadItemPositionHistory(Item item) {
        SortedMap<Date, ItemPositionHistoryList> histories = new TreeMap<>();
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<ItemPositionsHistoryEntity> criteriaQuery = builder.createQuery(ItemPositionsHistoryEntity.class);
        Root<ItemPositionsHistoryEntity> itemHistoryRoot = criteriaQuery.from(ItemPositionsHistoryEntity.class);
        criteriaQuery.select(itemHistoryRoot);
        Predicate predicate = builder.equal(itemHistoryRoot.get("itemid"), item.getId());
        criteriaQuery.where(predicate).orderBy(builder.desc(itemHistoryRoot.get("mdate")));
        for (ItemPositionsHistoryEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            ItemPositionsHistory dto = new ItemPositionsHistory(e, memberService.loadUserById(e.getActorid()));
            if (histories.get(dto.getmDate()) == null) {
                histories.put(e.getMdate(), new ItemPositionHistoryList());
            }
            histories.get(e.getMdate()).addHistory(dto);
        }
        return histories;
    }

    public SortedMap<Date, List<ItemDifference>> loadHistoryOfItem(Item item) {
        SortedMap<Date, List<ItemDifference>> histories = new TreeMap<>();
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<ItemHistoryEntity> criteriaQuery = builder.createQuery(ItemHistoryEntity.class);
        Root<ItemHistoryEntity> itemHistoryRoot = criteriaQuery.from(ItemHistoryEntity.class);
        criteriaQuery.select(itemHistoryRoot);
        Predicate predicate = builder.equal(itemHistoryRoot.get("id").get("itemid"), item.getId());
        criteriaQuery.where(predicate).orderBy(builder.desc(itemHistoryRoot.get("id").get("mdate")));
        for (ItemHistoryEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            User actor = memberService.loadUserById(e.getId().getActorid());
            User ownerNew = e.getOwner_new() == null ? null : memberService.loadUserById(e.getOwner_new());
            User ownerOld = e.getOwner_old() == null ? null : memberService.loadUserById(e.getOwner_old());
            Project projectOld = e.getProjectid_old() == null ? null : projectService.loadProjectById(e.getProjectid_old());
            Project projectNew = e.getProjectid_new() == null ? null : projectService.loadProjectById(e.getProjectid_new());
            ACList aclistNew = e.getAclistid_new() == null ? null : aclistService.loadById(e.getAclistid_new());
            ACList aclistOld = e.getAclistid_old() == null ? null : aclistService.loadById(e.getAclistid_old());
            ItemHistory history = new ItemHistory(
                    e,
                    actor,
                    ownerOld,
                    ownerNew,
                    item,
                    projectOld,
                    projectNew,
                    loadParentContainer(e.getParent_containerid_old()),
                    loadParentContainer(e.getParent_containerid_new()),
                    aclistOld,
                    aclistNew
            );
            history.setAction(e.getAction());
            history.setAmountNew(e.getAmount_new());
            history.setAmountOld(e.getAmount_old());
            history.setConcentrationNew(e.getConcentration_new());
            history.setConcentrationOld(e.getConcentration_old());
            history.setMdate(e.getId().getMdate());
            history.setPurityNew(e.getPurity_new());
            history.setPurityOld(e.getPurity_old());
            ArrayList< ItemDifference> diffs = new ArrayList<>();
            diffs.add(history);
            histories.put(history.getMdate(), diffs);
        }

        SortedMap<Date, ItemPositionHistoryList> positions = loadItemPositionHistory(item);

        for (Date d : positions.keySet()) {
            if (histories.get(d) == null) {
                histories.put(d, new ArrayList<>());
            }
            histories.get(d).add(positions.get(d));
        }

        return histories;
    }

    private Container loadParentContainer(Integer containerId) {
        if (containerId != null) {
            return containerService.loadContainerById(containerId);
        }
        return null;
    }

    private List<DbField> createOrderList() {
        DbField labelField = new DbField()
                .setColumnName("id")
                .setTableName("items")
                .setOrderDirection(OrderDirection.ASC);

        List<DbField> orderList = new ArrayList<>();
        orderList.add(labelField);
        return orderList;
    }
}
