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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service for manipulating the nesting of containers
 *
 * @author fmauz
 */
@Stateless
public class ContainerNestingService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private final String SQL_LOAD_PARENT_OF_CONTAINER
            = "SELECT parentcontainer "
            + "FROM containers "
            + "WHERE id=:containerid";
    private final String SQL_LOAD_NESTED_CONTAINER
            = "SELECT targetid "
            + "FROM nested_containers "
            + "WHERE sourceid =:containerid";
    private final String SQL_LOAD_SUB_CONTAINER
            = "SELECT sourceid "
            + "FROM nested_containers "
            + "WHERE targetid =:containerid";
    private final String SQL_DELETE_PATH
            = "DELETE FROM nested_containers "
            + "WHERE sourceid =:containerid";
    private final String SQL_BUILD_PATH
            = "INSERT INTO nested_containers "
            + "(sourceid,targetid,nested) "
            + "VALUES(:sourceid,:targetid,:nested)";

    /**
     * adds a new path of a container
     *
     * @param cid
     * @param path list of ids of path
     */
    public void addNewPath(int cid, Set<Integer> path) {
        Integer parentid = loadParentIdOfContainer(cid);
        for (Integer pe : path) {
            this.em.createNativeQuery(SQL_BUILD_PATH)
                    .setParameter("sourceid", cid)
                    .setParameter("targetid", pe)
                    .setParameter("nested", Objects.equals(parentid, pe))
                    .executeUpdate();
        }
    }

    /**
     * deletes the path a of container
     *
     * @param cid
     */
    public void deletePathForContainer(int cid) {
        this.em.createNativeQuery(SQL_DELETE_PATH).setParameter("containerid", cid).executeUpdate();
    }

    /**
     * Loads all container ids which has the given container (cid) in their
     * path.
     *
     * @param cid
     * @return subcontainer ids (unordered)
     */
    public Set<Integer> loadAllSubContainer(int cid) {
        Set<Integer> ids = new HashSet<>();
        List results = em.createNativeQuery(SQL_LOAD_SUB_CONTAINER).
                setParameter("containerid", cid)
                .getResultList();
        for (Object o : results) {
            ids.add((Integer) o);
        }
        return ids;
    }

    /**
     * Loads all container in which the container (cid) is direct or indirect
     * positioned
     *
     * @param cid
     * @return container ids of path (unordered)
     */
    public Set<Integer> loadNestedInContainers(int cid) {
        Set<Integer> ids = new HashSet<>();
        List results = em.createNativeQuery(SQL_LOAD_NESTED_CONTAINER).
                setParameter("containerid", cid)
                .getResultList();
        for (Object o : results) {
            ids.add((Integer) o);
        }
        return ids;
    }

    /**
     * Loads the id of the direct parent of a container
     *
     * @param cid
     * @return id or null if none exists
     */
    public Integer loadParentIdOfContainer(int cid) {
        List resultId = this.em.createNativeQuery(SQL_LOAD_PARENT_OF_CONTAINER).
                setParameter("containerid", cid).
                getResultList();
        return (Integer) resultId.get(0);
    }

    /**
     * loads a part of a path from container x to container y
     *
     * @param from
     * @param to
     * @return ids of container which are between x and y
     */
    public Set<Integer> loadSubpath(int from, int to) {
        Set<Integer> fromPath = loadNestedInContainers(from);
        Set<Integer> toPath = loadNestedInContainers(to);
        fromPath.removeAll(toPath);
        return fromPath;
    }

    /**
     * Actualizes the nesting path for the edited container and updates all
     * containers which have the edited container in ist path (direct or
     * indirect)
     *
     * @param cid id of edited container
     * @param newParent id of new parent, can be null
     */
    public void updateNestedContainerFor(int cid, Integer newParent) {
        Set<Integer> newPath = new HashSet<>();
        if (newParent != null) {
            newPath = loadNestedInContainers(newParent);
            newPath.add(newParent);
        }
        Set<Integer> subContainer = loadAllSubContainer(cid);
        for (Integer sc : subContainer) {
            Set<Integer> subpath = loadSubpath(sc, cid);
            subpath.addAll(newPath);
            deletePathForContainer(sc);
            addNewPath(sc, subpath);
        }
        deletePathForContainer(cid);
        addNewPath(cid, newPath);
    }

}
