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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerNesting;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerTypeEntity;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.ACListService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class ContainerService implements Serializable {

    Logger logger = LogManager.getLogger(this.getClass().getName());
    @Inject
    private ProjectService projectService;

    @Inject
    private ACListService aclistService;

    @Inject
    ItemService itemService;

    @Inject
    private ContainerNestingService nestingService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private final String SQL_DELETE_ITEM_IN_CONTAINER
            = "DELETE FROM item_positions "
            + "WHERE itemid=:itemid";

    private final String SQL_NESTED_TARGETS
            = "SELECT targetid "
            + " FROM nested_containers "
            + "WHERE sourceid=:source";
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT label "
            + "FROM containers "
            + "WHERE LOWER(label) LIKE LOWER(:label) "
            + "AND deactivated =FALSE";

    private final String SQL_LOAD_CONTAINERS = "SELECT DISTINCT "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.dimension, "
            + "c.type, "
            + "c.firesection, "
            + "c.gmosafety, "
            + "c.barcode,"
            + "c.deactivated "
            + "FROM containers c "
            + "LEFT JOIN projects p ON p.id=c.projectid "
            + "LEFT JOIN acentries ace ON ace.aclist_id=p.aclist_id "
            + "LEFT JOIN memberships ms ON ms.group_id=ace.member_id "
            + "LEFT JOIN nested_containers nc ON nc.sourceid=c.id "
            + "LEFT JOIN containers c2 ON c2.id=nc.targetid "
            + "WHERE (ace.permread=true OR c.projectid IS NULL OR CAST(p.ownerid AS VARCHAR)=:userid) "
            + "AND (c.id=:id OR :id=-1) "
            + "AND (CAST(ms.member_id AS VARCHAR)=:userid  OR c.projectid IS NULL) "
            + "AND (p.name=CAST(:project AS VARCHAR) OR CAST(:project AS VARCHAR) ='no_project') "
            + "AND (LOWER(c.label) LIKE LOWER(:label) OR :label = 'no_label') "
            + "AND (c2.label=:location OR :location ='no_location') "
            + "AND c.deactivated =FALSE "
            + "ORDER BY c.id";

    private final String SQL_LOAD_NESTED_CONTAINER
            = "SELECT  "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.dimension, "
            + "c.type, "
            + "c.firesection, "
            + "c.gmosafety, "
            + "c.barcode,"
            + "c.deactivated  "
            + "FROM nested_containers nc "
            + "JOIN containers c ON c.id=nc.targetid "
            + "JOIN containertypes ct ON ct.name=c.type "
            + "WHERE nc.sourceid=:cid "
            + "ORDER BY ct.rank";

    String SQL_LOAD_ITEMS_OF_CONTAINER = "SELECT "
            + "itemid,"
            + "col,"
            + "row "
            + "FROM item_positions "
            + "WHERE containerId=:containerId";

    String SQL_SAVE_ITEM_IN_CONTAINER = "INSERT INTO item_positions ("
            + "itemid,"
            + "containerid,"
            + "row,"
            + "col) "
            + "VALUES("
            + ":itemid,"
            + ":containerid,"
            + ":posY,"
            + ":posX)";

    private final String SQL_LOAD_BY_CONTAINERNAME
            = "SELECT  "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.dimension, "
            + "c.type, "
            + "c.firesection, "
            + "c.gmosafety, "
            + "c.barcode,"
            + "c.deactivated  "
            + "FROM containers c "
            + "WHERE UPPER(label)=UPPER(:label) "
            + "AND c.deactivated =FALSE";

    private final String SQL_DELETE_NESTING
            = "DELETE "
            + "FROM nested_containers "
            + "WHERE sourceid=:containerid ";

    /**
     * Gets all containersnames which matches the pattern %name%
     *
     * @param name name for searching
     * @param user
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public Set<String> getSimilarContainerNames(String name, User user) {
        List l = this.em.createNativeQuery(SQL_GET_SIMILAR_NAMES)
                .setParameter("label", "%" + name + "%")
                .getResultList();
        return new HashSet<>(l);
    }

    public Container loadContainerById(int id) {
        ContainerEntity entity = this.em.find(ContainerEntity.class, id);
        Container container = new Container(entity);
        container.setType(loadContainerTypeByName(entity.getType()));
        container.setItems(loadItemsOfContainer(container));
        return container;

    }

    public List<Container> loadContainerHierarchy(Item item) {
        return new ArrayList<>();
    }

    public Container saveContainer(Container c) {
        ContainerEntity dbe = c.createEntity();
        em.persist(dbe);
        c.setId(dbe.getId());
        if (c.getParentContainer() != null) {
            saveContainerNesting(new ContainerNesting(c.getId(), c.getParentContainer().getId(), false));
        }
        c.setId(dbe.getId());
        return c;
    }

    public List<Container> loadContainers(
            User u) {
        return loadContainers(u, new HashMap<>());
    }

    public void saveEditedContainer(Container c) {
        em.merge(c.createEntity());
        nestingService.updateNestedContainerFor(
                c.getId(),
                c.getParentContainer() == null ? null : c.getParentContainer().getId()
        );
    }

    /**
     * Loading of containers with ist full hierarchy.
     *
     * @param u
     * @param cmap
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Container> loadContainers(
            User u,
            Map<String, Object> cmap) {
        List<Project> projects
                = projectService.loadReadableProjectsOfUser(u);

        List<ContainerEntity> dbEntities
                = em.createNativeQuery(SQL_LOAD_CONTAINERS, ContainerEntity.class)
                        .setParameter("userid", u.getId().toString())
                        .setParameter("id", cmap.containsKey("id") ? cmap.get("id") : -1)
                        .setParameter("project", cmap.containsKey("project") ? cmap.get("project") : "no_project")
                        .setParameter("label", cmap.containsKey("label") ? "%" + cmap.get("label") + "%" : "no_label")
                        .setParameter("location", cmap.containsKey("location") ? cmap.get("location") : "no_location")
                        .getResultList();

        List<Container> result = new ArrayList<>();

        for (ContainerEntity dbe : dbEntities) {
            Container container = loadContainerById(dbe.getId());
            if (dbe.getProjectid() == null) {
                container = loadContainerById(dbe.getId());
                result.add(container);
            } else {
                container = loadContainerById(dbe.getId());
                container.setProject(projectService.loadProjectById(dbe.getProjectid()));
                result.add(container);
            }
            //I think that is no more neccessary
            if (container != null && dbe.getParentcontainer() != null) {
                container.setParentContainer(loadContainerById(dbe.getParentcontainer()));
            }
            List<ContainerEntity> nestedContainers = this.em
                    .createNativeQuery(SQL_LOAD_NESTED_CONTAINER, ContainerEntity.class)
                    .setParameter("cid", dbe.getId())
                    .getResultList();
            for (ContainerEntity nce : nestedContainers) {
                container.getContainerHierarchy().add(loadContainerById(nce.getId()));
            }

        }
        return result;

    }

    public List<ContainerType> loadContainerTypes() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeEntity> cq = cb.createQuery(ContainerTypeEntity.class);
        Root<ContainerTypeEntity> rootEntry = cq.from(ContainerTypeEntity.class);
        CriteriaQuery<ContainerTypeEntity> all = cq.select(rootEntry);
        TypedQuery<ContainerTypeEntity> allQuery = em.createQuery(all);
        List<ContainerTypeEntity> dbEntities = allQuery.getResultList();
        List<ContainerType> result = new ArrayList<>();
        for (ContainerTypeEntity dbe : dbEntities) {
            result.add(new ContainerType(dbe.getName(), dbe.getRank()));
        }
        return result;
    }

    public Integer getRankOfContainerType(String type) {
        ContainerTypeEntity entity = em.find(ContainerTypeEntity.class, type);
        if (entity != null) {
            return entity.getRank();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Item[][][] loadItemsOfContainer(Container c) {
        int[] dimSize = c.getDimensionIndex();
        if (dimSize == null) {
            return null;
        }
        Item[][][] items = new Item[dimSize[0]][dimSize[1]][dimSize[2]];

        List<Object[]> results = this.em.createNativeQuery(SQL_LOAD_ITEMS_OF_CONTAINER)
                .setParameter("containerId", c.getId())
                .getResultList();

        for (Object[] entity : results) {
            Integer itemid = (Integer) entity[0];
            Integer x = (Integer) entity[1];
            Integer y = (Integer) entity[2];
            Item i = itemService.loadItemByIdWithoutContainer(itemid);
            items[x][y][0] = i;

        }

        return items;
    }

    /**
     * Load a list of container ids which has the given id as a source (direct
     * or indirect). That means that the container with id is positioned in the
     * list of result ids.
     *
     * @param id container id
     * @return list of ids in which the container is in (direct or indirect)
     */
    @SuppressWarnings("unchecked")
    public List<Integer> loadNestedTargets(int id) {
        return this.em.
                createNativeQuery(SQL_NESTED_TARGETS)
                .setParameter("source", id)
                .getResultList();
    }

    /**
     * Loads a list of container ids which has the given id as a target. (direct
     * or indirect) That means all the ids of the result are positioned in the
     * given id.
     *
     * @param id container id
     * @return list of ids which are positioned in the container
     */
    public List<Integer> loadNestedSources(int id) {
        throw new RuntimeException("Not Yet implemented!");
    }

    /**
     * Adds a new container relationship and adds all indirect relationships.
     *
     * @param cn
     */
    public void saveContainerNesting(ContainerNesting cn) {
        List<Integer> targets = loadNestedTargets(cn.getTarget());
        this.em.merge(cn.createEntity());
        for (Integer t : targets) {
            this.em.merge(
                    new ContainerNesting(cn.getSource(), t, true)
                            .createEntity());
        }
    }

    /**
     * Loads all containers in which the container is present. The hierarchy is
     * descent
     *
     * @param id
     * @return Descent ordered list of containers in which the container is
     * present.
     */
    public List<Container> loadNestedContainer(Integer id) {
        if (id == null) {
            return new ArrayList<>();
        }
        List<Integer> parentContainer = loadNestedTargets(id);
        List<Container> nestedContainer = new ArrayList<>();

        Map<Integer, List<Integer>> l = new HashMap<>();
        //Load all targets for every container in chain 
        for (int i : parentContainer) {
            l.put(i, loadNestedTargets(i));
        }
        while (!l.isEmpty()) {
            int leastElements = getChainWithLeastElements(l);
            nestedContainer.add(loadContainerById(leastElements));
            l.remove(leastElements);
            for (Integer k : l.keySet()) {
                l.get(k).remove(Integer.valueOf(leastElements));
            }
        }
        //Get the element with the fewest elements. 

        return nestedContainer;
    }

    private int getChainWithLeastElements(Map<Integer, List<Integer>> l) {
        int leastElementsId = -1;
        for (int key : l.keySet()) {
            if (leastElementsId == -1 || l.get(key).size() < l.get(leastElementsId).size()) {
                leastElementsId = key;
            }
        }
        return leastElementsId;
    }

    public ContainerType loadContainerTypeByName(String name) {
        ContainerTypeEntity entity = em.find(ContainerTypeEntity.class, name);
        return new ContainerType(entity.getName(), entity.getRank());
    }

    public void deleteItemInContainer(int itemid) {
        this.em.createNativeQuery(SQL_DELETE_ITEM_IN_CONTAINER)
                .setParameter("itemid", itemid)
                .executeUpdate();
    }

    public void saveItemInContainer(int itemid, int containerid, int posX, int posY) {

        this.em.createNativeQuery(SQL_SAVE_ITEM_IN_CONTAINER)
                .setParameter("itemid", itemid)
                .setParameter("containerid", containerid)
                .setParameter("posX", posX)
                .setParameter("posY", posY)
                .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public Container loadContainerByName(String containerName) {
        if (containerName == null) {
            return null;
        }
        List<ContainerEntity> entities = this.em
                .createNativeQuery(SQL_LOAD_BY_CONTAINERNAME, ContainerEntity.class)
                .setParameter("label", containerName)
                .getResultList();
        if (entities.isEmpty()) {
            return null;
        } else {
            return loadContainerById(entities.get(0).getId());
        }
    }

    public void deactivateContainer(Container c) {
        c.setDeactivated(true);
        this.em.merge(c.createEntity());
    }

}
