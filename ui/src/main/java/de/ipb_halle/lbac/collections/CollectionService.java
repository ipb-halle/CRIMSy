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
package de.ipb_halle.lbac.collections;

/**
 * CollectionService provides service to load, save, update collections.
 */
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionEntity;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.service.NodeService;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

//import javax.naming.Context;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class CollectionService {

    String SQL_FILE_COUNT = "SELECT COUNT(*) "
            + "FROM FILES "
            + "WHERE collection_id=:collectionId";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ACListService aclistService;

    @Inject
    private MemberService memberService;

    @Inject
    private NodeService nodeService;

    private Logger logger;

    public CollectionService() {
    }

    @PostConstruct
    public void CollectionServiceInit() {
        logger = LogManager.getLogger(this.getClass().getName());
        if (nodeService == null) {
            logger.error("Injection failed for nodeService.");
        }
        if (em == null) {
            logger.error("Injection failed for Entitimanager em.");
        }
    }

    /**
     * Deletes a collection. All dependend objects (TermVectorEntities,
     * FileObjectEntities) must be cleaned before.
     *
     * @param collection
     * @return
     */
    public Boolean delete(Collection collection) {
        try {
            CollectionEntity tmp = this.em.find(CollectionEntity.class, collection.getId());
            if (tmp != null) {
                this.em.remove(tmp);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Counts all files in the database of a specific collection.
     *
     * @param collectionId
     * @return
     */
    public long getFileCount(Integer collectionId) {
        BigInteger cnt = (BigInteger) this.em.createNativeQuery(SQL_FILE_COUNT)
                .setParameter("collectionId", collectionId)
                .getSingleResult();
        return cnt.longValue();
    }

    /**
     * loads the local, public collection.
     *
     * @return null if public collection could not be found
     */
    public Collection getPublicCollectionFromDb() {
        Map<String, Object> cmap = new HashMap<>();
        try {
            cmap.put("name", "public");
            List<Collection> collectionList = load(cmap);
            if (!collectionList.isEmpty()) {
                return collectionList.get(0);
            } else {
                return null;
            }

        } catch (Exception e) {
            logger.error(String.format("Exception in checkPublicCollectionInDb: %s", e.getMessage()));
            return null;
        }
    }

    /**
     * Convert String to lower case and add SQL wildcard padding to it. This is
     * necessary as JPA2 does not provide means to createCollection an ilike
     * predicate.
     *
     * @param st input string
     * @return "%" + st.toLowerCase() + "%"
     */
    private String iWildcard(String st) {
        return "%" + st.toLowerCase() + "%";
    }

    /**
     * Load a list of collections
     * <p>
     * @param cmap the criteria map containing search criteria:<ul><li>
     * id (the collectionId or null if all collections should be fetched)</li>
     * <li> name (the name of one or more collections) </li><li>description (the
     * text describing a collection) </li></ul>
     * </p>
     * @return the list of collections
     */
    @SuppressWarnings("unchecked")
    public List<Collection> load(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<CollectionEntity> criteriaQuery = builder.createQuery(CollectionEntity.class);
        Root<CollectionEntity> collectionRoot = criteriaQuery.from(CollectionEntity.class);
        EntityType<CollectionEntity> collectionType = em.getMetamodel().entity(CollectionEntity.class);
        criteriaQuery.select(collectionRoot);
        List<Predicate> predicates = new ArrayList<>();

        if (cmap == null) {
            cmap = new HashMap<>();
        }

        // collection id
        if (cmap.get("id") != null) {
            predicates.add(builder.equal(collectionRoot.get("id"), cmap.get("id")));
        }

        // collection description like ...
        if (cmap.get("description") != null) {
            predicates.add(builder.like(builder.lower(
                    collectionRoot.get(collectionType.getDeclaredSingularAttribute("description", String.class))),
                    iWildcard((String) cmap.get("description"))));
        }

        // collection name like ...
        if (cmap.get("name") != null) {
            predicates.add(builder.like(builder.lower(
                    collectionRoot.get(collectionType.getDeclaredSingularAttribute("name", String.class))),
                    iWildcard((String) cmap.get("name"))));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));
        List<CollectionEntity> entities = this.em.createQuery(criteriaQuery.distinct(true)).getResultList();
        List<Collection> results = new ArrayList<>();

        for (CollectionEntity entity : entities) {
            results.add(new Collection(
                    entity,
                    nodeService.getLocalNode(),
                    aclistService.loadById(entity.getACList()),
                    memberService.loadUserById(entity.getOwner()),
                    getFileCount(entity.getId())));
        }

        return results;
    }

    /**
     * load a specific Collection by id
     *
     * @param id the collectionId
     * @return the Collection object
     */
    public Collection loadById(Integer id) {
        CollectionEntity entity = this.em.find(CollectionEntity.class, id);
        if (entity == null) {
            throw new NullPointerException("No local collection found with id " + id.toString());
        }
        return new Collection(
                entity,
                nodeService.getLocalNode(),
                aclistService.loadById(entity.getACList()),
                memberService.loadUserById(entity.getOwner()),
                getFileCount(id));
    }

    /**
     * @param c Collection
     * @return Saved collection with updated AC List
     */
    public Collection save(Collection c) {
        ACList aclist = this.aclistService.save(c.getACList());
        Node node = c.getNode();
        User owner = c.getOwner();
        c.setACList(aclist);
        return new Collection(
                this.em.merge(c.createEntity()),
                node,
                aclist,
                owner,
                c.getCountDocs());
    }

}
