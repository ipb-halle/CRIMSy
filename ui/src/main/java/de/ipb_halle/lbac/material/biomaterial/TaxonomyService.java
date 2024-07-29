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

import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.admission.MemberService;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
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

    private final String RECURSION_CHILD_TAXONOMIES
            = " WITH RECURSIVE current_recursion_result AS ( "
            + "SELECT et.taxoid,et.parentid, 1 AS depth "
            + "FROM taxonomy_direct_children et "
            + "WHERE parentid=:taxoid "
            + "UNION "
            + "SELECT  "
            + "et.taxoid, "
            + "et.parentid, "
            + "crr.depth+ 1 "
            + "FROM "
            + "taxonomy_direct_children et "
            + "INNER JOIN "
            + "current_recursion_result crr ON crr.taxoid = et.parentid "
            + "WHERE crr.depth<:MAX_DEPTH "
            + ") "
            + "SELECT frr.taxoid, mi.value,tl.id,tl.name,tl.rank,frr.parentid "
            + "FROM "
            + "current_recursion_result frr "
            + "INNER JOIN material_indices mi on mi.materialid = frr.taxoid "
            + "INNER JOIN taxonomy t on t.id=frr.taxoid "
            + "INNER JOIN taxonomy_level tl on tl.id = t.level; ";

    private final String SQL_GET_NESTED_TAXONOMIES = "SELECT parentid FROM effective_taxonomy WHERE taxoid=:id";

    @Inject
    private MaterialService materialService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private MemberService memberService;

    @PostConstruct
    public void init() {
    }

    public Set<String> getSimilarTaxonomy(String name) {
        return new HashSet<>();
    }

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

    @SuppressWarnings("unchecked")
    public List<Taxonomy> loadTaxonomy(Map<String, Object> queryParameters, boolean shouldChildrenBeLoaded) {
        List<Taxonomy> loadedTaxonomies = new ArrayList<>();

        List<TaxonomyEntity> loadedTaxonomyEntities = queryTaxonomyByParamater(queryParameters);

        for (TaxonomyEntity entity : loadedTaxonomyEntities) {
            List<Taxonomy> childrenTaxonomiesOfLoadedTaxonomy = new ArrayList<>();
            if (shouldChildrenBeLoaded) {
                childrenTaxonomiesOfLoadedTaxonomy.addAll(loadChildrenOfTaxonomy(entity.getId(), 100));
                Collections.sort(childrenTaxonomiesOfLoadedTaxonomy, (o1, o2) -> o1.getLevel().getRank() > o2.getLevel().getRank() ? -1 : 1);
            }
            MaterialAttributes materialAttributes = loadMaterialAttributes(entity);

            Taxonomy loadedTaxonomy = new Taxonomy(
                    entity.getId(),
                    materialService.loadMaterialNamesById(entity.getId()),
                    new HazardInformation(),
                    new StorageInformation(),
                    childrenTaxonomiesOfLoadedTaxonomy,
                    memberService.loadUserById(materialAttributes.getOwnerId()),
                    materialAttributes.getCreationTime());

            loadedTaxonomy.setLevel(new TaxonomyLevel(em.find(TaxonomyLevelEntity.class, entity.getLevel())));

            loadedTaxonomy.setHistory(materialService.loadHistoryOfMaterial(entity.getId()));
            loadedTaxonomies.add(loadedTaxonomy);
        }
        return loadedTaxonomies;
    }

    private MaterialAttributes loadMaterialAttributes(TaxonomyEntity entity) {
        List<Object> materialEntityList = this.em.createNativeQuery(SQL_GET_MATERIAL_INFOS)
                .setParameter("mid", entity.getId())
                .getResultList();

        MaterialAttributes materialAttributes = new MaterialAttributes(materialEntityList);
        return materialAttributes;
    }

    private List<Taxonomy> loadChildrenOfTaxonomy(int taxonomyId, int maxDepth) {
        List<Taxonomy> results = new ArrayList<>();

        Query q = this.em.createNativeQuery(RECURSION_CHILD_TAXONOMIES)
                .setParameter("taxoid", taxonomyId)
                .setParameter("MAX_DEPTH", maxDepth);

        List<Object[]> resultSet = q.getResultList();

        for (Object[] result : resultSet) {
            int id = (int) result[0];
            String name = (String) result[1];
            int levelid = (int) result[2];
            String levelName = (String) result[3];
            int levelRank = (int) result[4];
            int parentId = (int) result[5];
            TaxonomyLevel level = new TaxonomyLevel(levelid, levelName, levelRank);
            results.add(new TaxonomyTreeEntry(id, name, new ArrayList<>(), null, level, parentId));
        }
        return results;

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
        return loadChildrenOfTaxonomy(taxonomyId, 2);
    }

    public Taxonomy loadTaxonomyById(Integer id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", id);
        List<Taxonomy> results = loadTaxonomy(cmap, true);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public Taxonomy loadRootTaxonomy() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("level", loadTaxonomyLevel().get(0).getId());
        return loadTaxonomy(cmap, false).get(0);
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
