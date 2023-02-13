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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerNesting;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerTypeEntity;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Provides methods for loading and saving containers and containertypes
 *
 * @author fmauz
 */
@Stateless
public class ContainerService implements Serializable {

    @Inject
    private ContainerPositionService positionService;

    @Inject
    private ProjectService projectService;

    @Inject
    ItemService itemService;

    @Inject
    private ContainerNestingService nestingService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    /**
     * Gets all containersnames which matches the pattern %name%
     *
     * @param name name for searching
     * @param user
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public Set<Container> getSimilarContainerNames(String name, User user) {
        Set<Container> container = new HashSet<>();
        List<Integer> l = this.em.createNativeQuery(SQL_GET_SIMILAR_NAMES)
                .setParameter("label", "%" + name + "%")
                .getResultList();
        for (Integer id : l) {
            Container c = loadContainerById(id);
            container.add(c);
        }
        return container;
    }

    /**
     * Loads a container by id without its items
     *
     * @param id
     * @return
     */
    public Container loadContainerWithoutItemsById(int id) {
        ContainerEntity entity = this.em.find(ContainerEntity.class, id);
        Container container = new Container(entity);
        container.setType(loadContainerTypeByName(entity.getType()));
        container.setAutoCompleteString(container.getId() + "-" + container.getLabel() + " (" + container.getLocation(true, false) + ")");
        addProjectToContainer(entity.getProjectid(), container);
        loadContainerHierarchy(container);
        return container;
    }

    /**
     * Loads a container by id with its items
     *
     * @param id
     * @return
     */
    public Container loadContainerById(int id) {
        Container container = loadContainerWithoutItemsById(id);
        container.setItems(loadItemIdsOfContainer(container));
        return container;
    }

    /**
     * Saves a new container with its nested containers. Does not save the items
     * which are stored in the container
     *
     * @param c
     * @return the saved container with the id, given by the database
     */
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

    /**
     * Loads all containers with its nesting containers, but without its items
     *
     * @param u
     * @return
     */
    public List<Container> loadContainersWithoutItems(
            User u) {
        return loadContainersWithoutItems(u, new HashMap<>());
    }

    public void saveEditedContainer(Container c) {
        em.merge(c.createEntity());
        nestingService.updateContainerNesting(
                c.getId(),
                c.getParentContainer() == null ? null : c.getParentContainer().getId()
        );
    }

    /**
     * Loads all containers with its nesting containers, but without its items.
     * Possible search criteria arguments: userid,id,project,label,location
     *
     * @param u
     * @param cmap
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Container> loadContainersWithoutItems(
            User u,
            Map<String, Object> cmap) {
        List<Container> result = new ArrayList<>();
        List<ContainerEntity> dbEntities
                = em.createNativeQuery(SQL_LOAD_CONTAINERS, ContainerEntity.class)
                        .setParameter("userid", u.getId())
                        .setParameter("id", cmap.containsKey("id") ? cmap.get("id") : -1)
                        .setParameter("project", cmap.containsKey("project") ? cmap.get("project") : "no_project")
                        .setParameter("label", cmap.containsKey("label") ? "%" + cmap.get("label") + "%" : "no_label")
                        .setParameter("location", cmap.containsKey("location") ? cmap.get("location") : "no_location")
                        .getResultList();
        for (ContainerEntity dbe : dbEntities) {
            result.add(loadContainerWithoutItemsById(dbe.getId()));
        }
        return result;
    }

    /**
     * Loads all containertypes which are available in the database.
     *
     * @return
     */
    public List<ContainerType> loadContainerTypes() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeEntity> cq = cb.createQuery(ContainerTypeEntity.class);
        Root<ContainerTypeEntity> rootEntry = cq.from(ContainerTypeEntity.class);
        CriteriaQuery<ContainerTypeEntity> all = cq.select(rootEntry);
        TypedQuery<ContainerTypeEntity> allQuery = em.createQuery(all);
        List<ContainerTypeEntity> dbEntities = allQuery.getResultList();
        List<ContainerType> result = new ArrayList<>();
        for (ContainerTypeEntity dbe : dbEntities) {
            result.add(
                    new ContainerType(
                            dbe.getName(),
                            dbe.getRank(),
                            dbe.isTransportable(),
                            dbe.isUnique_name()));
        }
        return result;
    }

    /**
     * Loads all ids of the items which are placed in a container. 
     *
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public Item[][] loadItemIdsOfContainer(Container c) {
        Item[][] items = c.createEmptyItemArray();
        if (items == null) {
            return null;
        }
        List<Object[]> results = this.em.createNativeQuery(SQL_LOAD_ITEMS_OF_CONTAINER)
                .setParameter("containerId", c.getId())
                .getResultList();

        for (Object[] entity : results) {
            Integer itemid = (Integer) entity[0];
            Integer x = (Integer) entity[1];
            Integer y = (Integer) entity[2];
            Item i = new Item();
            i.setId(itemid);
            i.setContainer(c);
            items[y][x] = i;

        }

        return items;
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
        return nestedContainer;
    }

    /**
     * Loads a containerType by its name
     *
     * @param name
     * @return
     */
    public ContainerType loadContainerTypeByName(String name) {
        ContainerTypeEntity entity = em.find(ContainerTypeEntity.class, name);
        return new ContainerType(
                entity.getName(),
                entity.getRank(),
                entity.isTransportable(),
                entity.isUnique_name()
        );
    }

    /**
     * Loads a container by its name
     *
     * @param containerName
     * @return
     */
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

    /**
     * Deacticvated a container
     *
     * @param c
     */
    public void deactivateContainer(Container c) {
        c.setDeactivated(true);
        this.em.merge(c.createEntity());
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

    private void addProjectToContainer(Integer projectid, Container container) {
        if (projectid != null) {
            container.setProject(projectService.loadProjectById(projectid));
        }
    }

    /**
     * Loads all containers in which the given container is placed (direct and
     * indirect). The loaded containers does not contain their items.
     *
     * @param c
     */
    @SuppressWarnings("unchecked")
    private void loadContainerHierarchy(Container c) {
        List<ContainerEntity> nestedContainers = this.em
                .createNativeQuery(SQL_LOAD_NESTED_CONTAINER, ContainerEntity.class)
                .setParameter("cid", c.getId())
                .getResultList();
        for (ContainerEntity nce : nestedContainers) {
            c.getContainerHierarchy().add(loadContainerWithoutItemsById(nce.getId()));
        }
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
    private List<Integer> loadNestedTargets(int id) {
        return this.em.
                createNativeQuery(SQL_NESTED_TARGETS)
                .setParameter("source", id)
                .getResultList();
    }

    private final String SQL_NESTED_TARGETS
            = "SELECT targetid "
            + " FROM nested_containers "
            + "WHERE sourceid=:source";
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT id "
            + "FROM containers "
            + "WHERE LOWER(label) LIKE LOWER(:label) "
            + "AND deactivated =FALSE";

    private final String SQL_LOAD_CONTAINERS = "SELECT DISTINCT "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.rows, "
            + "c.columns, "
            + "c.type, "
            + "c.firearea, "
            + "c.gmosafetylevel, "
            + "c.barcode,"
            + "c.swapdimensions,"
            + "c.zerobased,"
            + "c.deactivated "
            + "FROM containers c "
            + "LEFT JOIN projects p ON p.id=c.projectid "
            + "LEFT JOIN acentries ace ON ace.aclist_id=p.aclist_id "
            + "LEFT JOIN memberships ms ON ms.group_id=ace.member_id "
            + "LEFT JOIN nested_containers nc ON nc.sourceid=c.id "
            + "LEFT JOIN containers c2 ON c2.id=nc.targetid "
            + "WHERE (ace.permread=true OR c.projectid IS NULL OR p.owner_id=:userid) "
            + "AND (c.id=:id OR :id=-1) "
            + "AND (ms.member_id=:userid  OR c.projectid IS NULL) "
            + "AND (p.name=:project OR :project='no_project') "
            + "AND (LOWER(c.label) LIKE LOWER(:label) OR :label = 'no_label') "
            + "AND (c2.label=:location OR :location ='no_location') "
            + "AND c.deactivated =FALSE "
            + "ORDER BY c.id "
            + "LIMIT 100";

    private final String SQL_LOAD_NESTED_CONTAINER
            = "SELECT  "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.rows, "
            + "c.columns, "
            + "c.type, "
            + "c.firearea, "
            + "c.gmosafetylevel, "
            + "c.barcode,"
            + "c.swapdimensions,"
            + "c.zerobased,"
            + "c.deactivated  "
            + "FROM nested_containers nc "
            + "JOIN containers c ON c.id=nc.targetid "
            + "JOIN containertypes ct ON ct.name=c.type "
            + "WHERE nc.sourceid=:cid "
            + "ORDER BY ct.rank";

    String SQL_LOAD_ITEMS_OF_CONTAINER = "SELECT "
            + "itemid,"
            + "itemcol,"
            + "itemrow "
            + "FROM item_positions "
            + "WHERE containerId=:containerId";

    private final String SQL_LOAD_BY_CONTAINERNAME
            = "SELECT  "
            + "c.id, "
            + "c.parentcontainer, "
            + "c.label, "
            + "c.projectid, "
            + "c.rows, "
            + "c.columns, "
            + "c.type, "
            + "c.firearea, "
            + "c.gmosafetylevel, "
            + "c.barcode,"
            + "c.swapdimensions,"
            + "c.zerobased,"
            + "c.deactivated  "
            + "FROM containers c "
            + "WHERE UPPER(label)=UPPER(:label) "
            + "AND c.deactivated =FALSE";

}
