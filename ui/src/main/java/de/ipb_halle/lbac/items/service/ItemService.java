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
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.SqlStringWrapper;
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
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
    private final String SQL_LOAD_ITEMS = "Select DISTINCT i.id,"
            + "i.materialid,"
            + "i.amount,"
            + "i.articleid,"
            + "i.projectid,"
            + "i.concentration,"
            + "i.unit,"
            + "i.purity,"
            + "i.solventid,"
            + "i.description,"
            + "i.owner,"
            + "i.containersize,"
            + "i.containertype,"
            + "i.containerid,"
            + "i.ctime,"
            + "i.aclist_id,"
            + "i.expiry_date "
            + "FROM items i "
            + "JOIN material_indices mi ON mi.materialid=i.materialid "
            + "JOIN usersgroups u on u.id=i.owner "
            + "LEFT JOIN projects p on p.id=i.projectid "
            + "JOIN materials m ON m.materialid=mi.materialid "
            + "LEFT JOIN nested_containers nc ON i.containerid=nc.sourceid "
            + "LEFT JOIN containers c ON nc.targetid=c.id "
            + "LEFT JOIN containers c2 ON i.containerid=c2.id "
            + SqlStringWrapper.JOIN_KEYWORD + " "
            + "WHERE (mi.value=:MATERIAL_NAME OR :MATERIAL_NAME='no_name_filter') "
            + "AND " + SqlStringWrapper.WHERE_KEYWORD + " "
            + "AND (i.id=:ITEM_ID OR :ITEM_ID=-1) "
            + "AND (u.name=:OWNER_NAME OR :OWNER_NAME='no_user_filter') "
            + "AND (c2.label ILIKE(:LOCATION_NAME) OR :LOCATION_NAME='no_location_filter' OR c.label ILIKE(:LOCATION_NAME)) "
            + "AND (i.description ILIKE(:DESCRIPTION) OR :DESCRIPTION='no_description_filter') "
            + "AND (p.name=:PROJECT_NAME OR :PROJECT_NAME='no_project_filter') "
            + "ORDER BY i.id";

    private final String SQL_LOAD_ITEMS_AMOUNT = "Select COUNT(DISTINCT(i.id)) "
            + "FROM items i "
            + "JOIN material_indices mi ON mi.materialid=i.materialid "
            + "JOIN usersgroups u on u.id=i.owner "
            + "LEFT JOIN projects p on p.id=i.projectid "
            + "JOIN materials m ON m.materialid=mi.materialid "
            + "LEFT JOIN nested_containers nc ON i.containerid=nc.sourceid "
            + "LEFT JOIN containers c ON nc.targetid=c.id "
            + "LEFT JOIN containers c2 ON i.containerid=c2.id "
            + SqlStringWrapper.JOIN_KEYWORD + " "
            + "WHERE (mi.value=:MATERIAL_NAME OR :MATERIAL_NAME='no_name_filter') "
            + "AND " + SqlStringWrapper.WHERE_KEYWORD + " "
            + "AND (u.name=:OWNER_NAME OR :OWNER_NAME='no_user_filter') "
            + "AND (i.description ILIKE(:DESCRIPTION) OR :DESCRIPTION='no_description_filter') "
            + "AND (p.name=:PROJECT_NAME OR :PROJECT_NAME='no_project_filter') "
            + "AND (c2.label ILIKE(:LOCATION_NAME) OR :LOCATION_NAME='no_location_filter' OR c.label ILIKE(:LOCATION_NAME)) "
            + "AND (i.id=:ITEM_ID OR :ITEM_ID=-1)";

    /**
     *
     * @param u
     * @param cmap
     * @param firstResult
     * @param maxResults
     * @return
     */
    @SuppressWarnings("unchecked")
    public SearchResult loadItems(User u, Map<String, String> cmap, int firstResult, int maxResults) {

        SearchResult result = new SearchResultImpl();

        Query q = createItemQuery(SqlStringWrapper.aclWrapper(SQL_LOAD_ITEMS, "i.aclist_id", "i.owner", ACPermission.permREAD), cmap, ItemEntity.class);
        q.setParameter("userid", u.getId());
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);
        List<ItemEntity> entities = q.getResultList();

        for (ItemEntity entity : entities) {
            Item i = createItemFromEntity(entity);
            i.setHistory(loadHistoryOfItem(i));

            result.addResults(nodeService.getLocalNode(), Arrays.asList(i));
        }
        return result;
    }

    public SearchResult loadItems(SearchRequest request) {
        SearchResult result = new SearchResultImpl();
        SqlBuilder sqlBuilder = new SqlBuilder(request.getEntityGraph());
        String sql = sqlBuilder.query(request.getCondition());

        Query q = em.createNativeQuery(sql, ItemEntity.class);
        for (Value param : sqlBuilder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        q.setFirstResult(request.getFirstResult());
        q.setMaxResults(request.getMaxResults());
        List<ItemEntity> entities = q.getResultList();
        for (ItemEntity ie : entities) {
            Item item = createItemFromEntity(ie);
            item.setHistory(loadHistoryOfItem(item));
            result.addResults(nodeService.getLocalNode(), Arrays.asList(item));
        }

        return result;
    }

    private Item createItemFromEntity(ItemEntity entity) {
        Item item = new Item(entity,
                entity.getArticleid() == null ? null : articleService.loadArticleById(entity.getArticleid()),
                entity.getContainerid() == null ? null : containerService.loadContainerById(entity.getContainerid()),
                materialService.loadMaterialById(entity.getMaterialid()),
                memberService.loadUserById(entity.getOwner()),
                entity.getProjectid() == null ? null : projectService.loadProjectById(entity.getProjectid()),
                entity.getSolventid() == null ? null : loadSolventById(entity.getSolventid()),
                containerService.loadNestedContainer(entity.getContainerid()),
                aclistService.loadById(entity.getAclist_id())
        );
        return item;
    }

    public int getItemAmount(User u, Map<String, String> cmap) {
        Query q = createItemQuery(SqlStringWrapper.aclWrapper(SQL_LOAD_ITEMS_AMOUNT, "i.aclist_id", "i.owner", ACPermission.permREAD), cmap, null);
        q.setParameter("userid", u.getId());
        BigInteger bi = (BigInteger) q.getResultList().get(0);
        return bi.intValue();
    }

    private Query createItemQuery(String rawSql, Map<String, String> cmap, Class targetClass) {
        Query q;
        if (targetClass == null) {
            q = this.em.createNativeQuery(rawSql);
        } else {
            q = this.em.createNativeQuery(rawSql, targetClass);
        }

        return q
                .setParameter("DESCRIPTION", cmap.getOrDefault("DESCRIPTION", "no_description_filter"))
                .setParameter("MATERIAL_NAME", cmap.getOrDefault("MATERIAL_NAME", "no_name_filter"))
                .setParameter("OWNER_NAME", cmap.getOrDefault("OWNER_NAME", "no_user_filter"))
                .setParameter("PROJECT_NAME", cmap.getOrDefault("PROJECT_NAME", "no_project_filter"))
                .setParameter("LOCATION_NAME", cmap.getOrDefault("LOCATION_NAME", "no_location_filter"))
                .setParameter("ITEM_ID", cmap.containsKey("ITEM_ID") ? Integer.parseInt(cmap.get("ITEM_ID")) : -1);
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
                aclistService.loadById(entity.getAclist_id()));
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
                aclistService.loadById(entity.getAclist_id()));
    }

    /**
     *
     * @param id
     * @return
     */
    public Solvent loadSolventById(int id) {
        throw new UnsupportedOperationException("not yet implemented");
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

}
