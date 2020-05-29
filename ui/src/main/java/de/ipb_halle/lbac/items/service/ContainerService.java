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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerNesting;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerTypeEntity;
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

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private final String SQL_NESTED_TARGETS = "SELECT targetid FROM nested_containers WHERE sourceid=:source";
    private final String SQL_GET_SIMILAR_NAMES = "SELECT label FROM containers WHERE LOWER(label) LIKE LOWER(:label)";

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
        ContainerEntity dbe = new ContainerEntity();
        if (c.getParentContainer() != null) {
            dbe.setParentcontainer(c.getParentContainer().getId());
        }
        dbe.setLabel(c.getLabel());
        if (c.getProject() != null) {
            dbe.setProjectid(c.getProject().getId());
        }
        dbe.setDimension(c.getDimension());
        dbe.setType(c.getType().getName());
        dbe.setFiresection(c.getFireSection());
        dbe.setGvo_class(c.getGvoClass());
        dbe.setBarcode(c.getBarCode());
        em.persist(dbe);
        c.setId(dbe.getId());
        if (c.getParentContainer() != null) {
            saveContainerNesting(new ContainerNesting(c.getId(), c.getParentContainer().getId(), false));
        }
        c.setId(dbe.getId());
        return c;
    }

    public List<Container> loadContainerHierarchy(Container container) {
        ArrayList<Container> containerHierarchy = new ArrayList<>();
        if (container.getParentContainer() != null) {
            return new ArrayList<>();
        } else {

        }
        return containerHierarchy;
    }

    /**
     * Lazy loading of containers. This means the parent container is only
     * loaded for one hierarchy, all container further in the hierachy must be
     * loaded manually.
     *
     * @param u
     * @return
     */
    public List<Container> loadContainers(User u) {
        List<Project> projects
                = projectService.loadReadableProjectsOfUser(u);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ContainerEntity> cq = cb.createQuery(ContainerEntity.class);
        Root<ContainerEntity> rootEntry = cq.from(ContainerEntity.class);
        CriteriaQuery<ContainerEntity> all = cq.select(rootEntry);

        TypedQuery<ContainerEntity> allQuery = em.createQuery(all);

        List<ContainerEntity> dbEntities = allQuery.getResultList();
        List<Container> result = new ArrayList<>();

        for (ContainerEntity dbe : dbEntities) {
            Container container = null;
            if (dbe.getProjectid() == null) {
                container = loadContainerById(dbe.getId());
                result.add(container);
            } else {
                if (getProjectById(dbe.getProjectid(), projects) != null) {
                    container = loadContainerById(dbe.getId());
                    result.add(container);
                }
            }
            if (container != null && dbe.getParentcontainer() != null) {
                container.setParentContainer(loadContainerById(dbe.getParentcontainer()));
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

    private Project getProjectById(Integer id, List<Project> project) {
        for (Project p : project) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
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
        String sql = "SELECT "
                + "itemid,"
                + "col,"
                + "row "
                + "FROM item_positions "
                + "WHERE containerId=:containerId";
        List<Object[]> results = this.em.createNativeQuery(sql)
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
     * Removes a container from another container and removes all direct and
     * indirect relationships.
     *
     * @param cn
     */
    public void removeContainerWithNesting(ContainerNesting cn) {
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

    public void saveItemInContainer(int itemid, int containerid, int posX, int posY) {
        String sql = "INSERT INTO item_positions ("
                + "itemid,"
                + "containerid,"
                + "row,"
                + "col) "
                + "VALUES("
                + ":itemid,"
                + ":containerid,"
                + ":posY,"
                + ":posX)";

        this.em.createNativeQuery(sql)
                .setParameter("itemid", itemid)
                .setParameter("containerid", containerid)
                .setParameter("posX", posX)
                .setParameter("posY", posY)
                .executeUpdate();
    }
}
