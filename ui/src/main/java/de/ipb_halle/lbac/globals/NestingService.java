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
package de.ipb_halle.lbac.globals;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;

/**
 *
 * @author fmauz
 */
public class NestingService implements Serializable {

    protected EntityManager em;

    protected String SQL_LOAD_PARENT_OF_OBJECT;
    protected String SQL_LOAD_NESTED_OBJECTS;
    protected String SQL_LOAD_SUB_OBJECTS;
    protected String SQL_DELETE_PATH;
    protected String SQL_BUILD_PATH;

    public NestingService(EntityManager em) {
        this.em = em;
    }

    public void addNewPath(int cid, Set<Integer> path) {
        Integer parentid = loadParentIdOfObject(cid);
        for (Integer pe : path) {
            if (parentid != null) {
                this.em.createNativeQuery(SQL_BUILD_PATH)
                        .setParameter("sourceid", cid)
                        .setParameter("targetid", pe)
                        .setParameter("nested", Objects.equals(parentid, pe))
                        .executeUpdate();
            } else {
                this.em.createNativeQuery(SQL_BUILD_PATH)
                        .setParameter("sourceid", cid)
                        .setParameter("targetid", pe)
                        .executeUpdate();
            }
        }
    }

    /**
     * deletes the path a of container
     *
     * @param cid
     */
    public void deletePathForObject(int cid) {
        this.em.createNativeQuery(SQL_DELETE_PATH).setParameter("id", cid).executeUpdate();
    }

    /**
     * Loads all container ids which has the given container (cid) in their
     * path.
     *
     * @param cid
     * @return subcontainer ids (unordered)
     */
    public Set<Integer> loadAllSubObjects(int cid) {
        Set<Integer> ids = new HashSet<>();
        List results = em.createNativeQuery(SQL_LOAD_SUB_OBJECTS).
                setParameter("id", cid)
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
    public Set<Integer> loadNestedInObjects(int cid) {
        Set<Integer> ids = new HashSet<>();
        List results = em.createNativeQuery(SQL_LOAD_NESTED_OBJECTS).
                setParameter("id", cid)
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
    public Integer loadParentIdOfObject(int cid) {
        List resultId = this.em.createNativeQuery(SQL_LOAD_PARENT_OF_OBJECT).
                setParameter("id", cid).
                getResultList();
        if (resultId.isEmpty()) {
            return null;
        }
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
        Set<Integer> fromPath = loadNestedInObjects(from);
        Set<Integer> toPath = loadNestedInObjects(to);
        fromPath.removeAll(toPath);
        return fromPath;
    }

    /**
     * Actualizes the nesting path for the edited container and updates all
     * containers which have the edited container in its path (direct or
     * indirect)
     *
     * @param cid id of edited container
     * @param newParent id of new parent, can be null
     */
    public void updateNestedObjectFor(int cid, Integer newParent) {
        Set<Integer> newPath = new HashSet<>();
        if (newParent != null) {
            newPath = loadNestedInObjects(newParent);
            newPath.add(newParent);
        }
        Set<Integer> subContainer = loadAllSubObjects(cid);
        for (Integer sc : subContainer) {
            Set<Integer> subpath = loadSubpath(sc, cid);
            subpath.addAll(newPath);
            deletePathForObject(sc);
            addNewPath(sc, subpath);
        }
        deletePathForObject(cid);
        addNewPath(cid, newPath);
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public String getSqlLoadParentOfObject() {
        return SQL_LOAD_PARENT_OF_OBJECT;
    }

    public void setSqlLoadParentOfObject(String sql) {
        this.SQL_LOAD_PARENT_OF_OBJECT = sql;
    }

    public String getSqlLoadNestedObjects() {
        return SQL_LOAD_NESTED_OBJECTS;
    }

    public void setSqlLoadNestedObjects(String sql) {
        this.SQL_LOAD_NESTED_OBJECTS = sql;
    }

    public String getSqlLoadSubObjects() {
        return SQL_LOAD_SUB_OBJECTS;
    }

    public void setSqlLoadSubObjects(String sql) {
        this.SQL_LOAD_SUB_OBJECTS = sql;
    }

    public String getSqlDeletePath() {
        return SQL_DELETE_PATH;
    }

    public void setSqlDeletePath(String sql) {
        this.SQL_DELETE_PATH = sql;
    }

    public String getSqlBuildPath() {
        return SQL_BUILD_PATH;
    }

    public void setSqlBuildPath(String sql) {
        this.SQL_BUILD_PATH = sql;
    }

}
