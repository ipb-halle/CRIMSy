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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.admission.MemberService;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author fmauz
 */
@Stateless
public class TaxonomyService implements Serializable {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private final String SQL_GET_TAXONOMY_LEVELS = "SELECT id,name,rank FROM taxonomy_level";

    private final String SQL_GET_TAXONOMY
            = "SELECT id,level "
            + "FROM taxonomy "
            + "WHERE (level=:level OR :level=-1) "
            + "AND (id=:id OR :id=-1) "
            + "ORDER BY level";

    private final String SQL_GET_MATERIAL_INFOS
            = "SELECT ctime,aclist_id,owner_id "
            + "FROM materials "
            + "WHERE materialid=:mid";

    private final String SQL_CHECK_ROOT_TAXONOMY_PRESENT
            = "SELECT count(*) "
            + "FROM taxonomy "
            + "WHERE level=1";

    private final String SQL_GET_DIRECT_CHILDREN
            = "SELECT taxoid "
            + "FROM effective_taxonomy ef "
            + "WHERE ef.parentid=:taxoid "
            + "AND NOT EXISTS( "
            + "SELECT ef2.taxoid "
            + "FROM effective_taxonomy ef2 "
            + "JOIN taxonomy  t on t.id=ef2.parentid "
            + "JOIN taxonomy  t2 on t2.id=:taxoid "
            + "WHERE ef2.taxoid=ef.taxoid "
            + "AND t2.level < t.level);";

    private final String SQL_GET_NESTED_TAXONOMIES = "SELECT parentid FROM effective_taxonomy WHERE taxoid=:id";

    @Inject
    private MaterialService materialService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private MemberService memberService;

    @Inject
    private IndexService indexService;

    @Inject
    private ACListService acListService;

    @SuppressWarnings("unchecked")
    public List<TaxonomyLevel> loadTaxonomyLevel() {
        List<TaxonomyLevel> levels = new ArrayList<>();
        List<TaxonomyLevelEntity> dbentities = this.em.createNativeQuery(SQL_GET_TAXONOMY_LEVELS, TaxonomyLevelEntity.class).getResultList();
        for (TaxonomyLevelEntity entity : dbentities) {
            levels.add(new TaxonomyLevel(entity));
        }
        return levels;
    }

    @SuppressWarnings("unchecked")
    public TaxonomyLevel loadTaxonomyLevelById(Integer id) {
        return new TaxonomyLevel(this.em.find(TaxonomyLevelEntity.class, id));
    }

    private final String TAXONOMY_IDS_OF_ROOT_WITH_DEPTH = "select taxoid"
            + " from effective_taxonomy "
            + " group by taxoid"
            + " having count(taxoid)<= ( select count(taxoid)+:depth"
            + " from effective_taxonomy"
            + " group by taxoid"
            + " having bool_or(parentid=:rootId)"
            + " order by count(taxoid)"
            + " limit 1   )"
            + " AND bool_or(parentid=:rootId);";

    @SuppressWarnings("unchecked")
    public List<Taxonomy> loadTaxonomyByIdAndDepth(Integer rootId, Integer depth) {
        List<Taxonomy> loadedTaxonomy = new ArrayList<>();
        Map<Integer, Taxonomy> chachedTaxonomies = new HashMap<>();

        Set<Integer> setOfTaxoIds = getIdsOfRootWithDepth(rootId, depth - 1);
        Map<Integer, List<Integer>> mapOfAncestorIds = loadParentIdsOfTaxonomies(setOfTaxoIds);

        Set<Integer> combinedIds = combineIds(setOfTaxoIds, mapOfAncestorIds);

        List<MaterialEntity> materialEntities = createMaterialEntitiesFromTaxonomyIds(combinedIds);

        Map<Integer, List<MaterialName>> materialNamesMap = indexService.createMaterialNamesMapFromTaxonomyIds(combinedIds);

        HashSet<Integer> userIds = (HashSet<Integer>) materialEntities.stream().map(me -> me.getOwner()).collect(Collectors.toSet());
        Map<Integer, User> users = memberService.createUserMapFromGivenUsersIds(userIds);

        HashSet<Integer> aclIds = (HashSet<Integer>) materialEntities.stream().map(x -> x.getACList()).collect(Collectors.toSet());
        Map<Integer, ACList> aclLists = acListService.createACListMapFromGivenACIds(aclIds);

        String taxonomyEntitiesQuery = "SELECT id,level FROM taxonomy WHERE id in(:ids) ;";
        List<TaxonomyEntity> taxonomyEntities = em.createNativeQuery(taxonomyEntitiesQuery, TaxonomyEntity.class).setParameter("ids", combinedIds).getResultList();
        Set<Integer> levelIds = new HashSet(taxonomyEntities.stream().map(x -> x.getLevel()).toList());
        List<TaxonomyLevel> levels = loadTaxonomyLevel();

        //Load TaxonomyLevel;
        for (MaterialEntity materialEntity : materialEntities) {

            Taxonomy t = new Taxonomy(
                    materialEntity.getMaterialid(),
                    materialEntity.getProjectid(),
                    materialNamesMap.get(materialEntity.getMaterialid()),
                    users.get(materialEntity.getOwner()),
                    materialEntity.getCtime(),
                    aclLists.get(materialEntity.getACList()));
            chachedTaxonomies.put(t.getId(), t);
            loadedTaxonomy.add(t);

            for (TaxonomyEntity te : taxonomyEntities) {
                if (te.getId().equals(materialEntity.getMaterialid())) {
                    TaxonomyLevel level = levels.stream().filter(tl -> tl.getId() == te.getLevel()).toList().get(0);
                    t.setLevel(level);
                    //Setting of a history
                    t.setHistory(materialService.loadHistoryOfMaterial(te.getId()));
                }
            }
            taxonomyEntities.stream().filter(x -> x.getId().equals(t.getId())).toList().get(0);
        }

        addHierarchyToTaxonomies(loadedTaxonomy, mapOfAncestorIds, chachedTaxonomies);

        return loadedTaxonomy.stream().filter(t -> setOfTaxoIds.contains(t.getId())).toList();
    }

    private void addHierarchyToTaxonomies(List<Taxonomy> loadedTaxonomy, Map<Integer, List<Integer>> mapOfAncestorIds, Map<Integer, Taxonomy> chachedTaxonomies) {
        for (Taxonomy t : loadedTaxonomy) {
            List<Integer> parentIds = mapOfAncestorIds.get(t.getId());
            for (Integer parentId : parentIds) {
                if (parentId != null) {
                    t.getTaxHierarchy().add(chachedTaxonomies.get(parentId));
                }
            }
        }
    }

    private Set<Integer> combineIds(Set<Integer> setOfTaxoIds, Map<Integer, List<Integer>> mapOfAncestorIds) {
        Set<Integer> combinedIds = new HashSet<Integer>(setOfTaxoIds);
        for (List<Integer> parentsOfTaxo : mapOfAncestorIds.values()) {
            combinedIds.addAll(parentsOfTaxo);
        }
        return combinedIds;
    }

    private Map<Integer, List<Integer>> loadParentIdsOfTaxonomies(Collection<Integer> ids) {
        Map<Integer, List<Integer>> map = createParentsMap(ids);
        Map<Integer, List<Integer>> newValues = addMissingKeysToMap(map);
        map.putAll(newValues);
        return map;

    }

    private Map<Integer, List<Integer>> addMissingKeysToMap(Map<Integer, List<Integer>> map) {
        Map<Integer, List<Integer>> newValues = new HashMap<>();
        Set<Integer> idsNotInKeySet = new HashSet<>();
        for (List<Integer> idsOfKey : map.values()) {
            idsNotInKeySet.addAll(idsOfKey);
        }
        idsNotInKeySet.removeAll(map.keySet());
        for (Integer id : idsNotInKeySet) {
            for (List<Integer> values : map.values()) {
                if (values.indexOf(id) != -1) {
                    newValues.put(id, values.subList(values.indexOf(id) + 1, values.size()));
                }
            }
        }
        return newValues;
    }

    private final String IDS_OF_HIERARCHY = "select taxoid,parentid "
            + "from effective_taxonomy et "
            + "join taxonomy t on t.id = et.parentid "
            + "join taxonomy_level tl on t.level = tl.id "
            + "where et.taxoid in (:ids) "
            + "order by taxoid asc, rank desc; ";

    private Map<Integer, List<Integer>> createParentsMap(Collection<Integer> ids) {
        List<Integer[]> idsOfRootHierarchy = (List<Integer[]>) em.createNativeQuery(IDS_OF_HIERARCHY).setParameter("ids", ids).getResultList();
        Object xx = em.createNativeQuery("Select * from effective_taxonomy order by taxoid").getResultList();
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(1, new ArrayList<>());
        for (Object[] o : idsOfRootHierarchy) {
            Integer taxoId = (Integer) o[0];
            Integer parentId = (Integer) o[1];
            if (!map.containsKey(taxoId)) {
                map.put(taxoId, new ArrayList<>());
            }
            map.get(taxoId).add((parentId));
        }
        return map;
    }

    private Set<Integer> getIdsOfRootWithDepth(Integer rootId, Integer depth) {
        Query query = em.createNativeQuery(TAXONOMY_IDS_OF_ROOT_WITH_DEPTH);
        query.setParameter("rootId", rootId);
        query.setParameter("depth", depth);
        //here we got IDs of Taxonomies from Data Storage
        Set<Integer> listOfTaxonomiesIdsFromQuery = new HashSet<>((List<Integer>) query.getResultList());
        listOfTaxonomiesIdsFromQuery.add(rootId);
        return listOfTaxonomiesIdsFromQuery;
    }

    //Method for creation of Material Entities Lists from gotten Taxonomy IDs
    private List<MaterialEntity> createMaterialEntitiesFromTaxonomyIds(Set<Integer> listOfTaxonomiesIdsFromQuery) {
        //Native query
        String queryForMaterialEntietiesBasedOnTaxonomieIds = " select materialid, materialtypeid, ctime, aclist_id, owner_id, deactivated, projectid "
                + "from materials where materialid in (:listOfTaxonomieIds) ;";

        Query query = em.createNativeQuery(queryForMaterialEntietiesBasedOnTaxonomieIds, MaterialEntity.class);
        query.setParameter("listOfTaxonomieIds", listOfTaxonomiesIdsFromQuery);

        List<MaterialEntity> materialEntityList = (List<MaterialEntity>) query.getResultList();

        return materialEntityList;
    }

    private MaterialAttributes loadMaterialAttributes(TaxonomyEntity entity) {
        List<Object> materialEntityList = this.em.createNativeQuery(SQL_GET_MATERIAL_INFOS)
                .setParameter("mid", entity.getId())
                .getResultList();

        MaterialAttributes materialAttributes = new MaterialAttributes(materialEntityList);
        return materialAttributes;
    }

    @SuppressWarnings("unchecked")
    private List<TaxonomyEntity> queryTaxonomyByParamater(Map<String, Object> queryParameters) {
        Query query = this.em.createNativeQuery(SQL_GET_TAXONOMY, TaxonomyEntity.class);
        query.setParameter("level", queryParameters.containsKey("level") ? queryParameters.get("level") : -1);
        query.setParameter("id", queryParameters.containsKey("id") ? queryParameters.get("id") : -1);
        List<TaxonomyEntity> loadedTaxonomyEntities = (List<TaxonomyEntity>) query.getResultList();
        return loadedTaxonomyEntities;
    }

    @SuppressWarnings("unchecked")
    public int checkRootTaxonomy() {
        List<Long> results = this.em.createNativeQuery(SQL_CHECK_ROOT_TAXONOMY_PRESENT).getResultList();
        return results.get(0).intValue();
    }

    public List<Taxonomy> loadDirectChildrenOf(int taxonomyId) {
        List<Taxonomy> results = new ArrayList<>();
        Query q = this.em.createNativeQuery(SQL_GET_DIRECT_CHILDREN);
        q.setParameter("taxoid", taxonomyId);
        @SuppressWarnings("unchecked")
        List<Integer> a = (List) q.getResultList();
        for (Integer tid : a) {
            results.add(loadTaxonomyById(tid));
        }
        return results;
    }

    public Taxonomy loadTaxonomyById(Integer id) {
        List<Taxonomy> results = loadTaxonomyByIdAndDepth(id, 0);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public Taxonomy loadRootTaxonomy() {
        return loadTaxonomyByIdAndDepth(1, 0).get(0);
    }

    private class MaterialAttributes {

        private Object[] parameter;

        MaterialAttributes(List<Object> parameter) {
            this.parameter = (Object[]) parameter.get(0);
        }

        public int getOwnerId() {
            return (int) parameter[2];
        }

        public Date getCreationTime() {
            return new Date(((Timestamp) parameter[0]).getTime());
        }

    }

}
