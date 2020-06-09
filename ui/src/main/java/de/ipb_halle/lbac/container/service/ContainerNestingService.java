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

import de.ipb_halle.lbac.container.Container;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class ContainerNestingService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private final String SQL_LOAD_PARENT_OF_CONTAINER
            = " SELECT parentcontainer "
            + "FROM containers "
            + "WHERE id=:containerid";
    private final String SQL_LOAD_NESTED_CONTAINER
            = "SELECT "
            + "targetid "
            + "FROM nested_containers "
            + "WHERE sourceid =:containerid";

    private final String SQL_LOAD_SUB_CONTAINER
            = "SELECT "
            + "sourceid "
            + "FROM nested_containers "
            + "WHERE targetid =:containerid";

    private final String SQL_DELETE_PATH
            = "DELETE "
            + "FROM nested_containers "
            + "WHERE sourceid =:containerid";

    private final String SQL_BUILD_PATH
            = "INSERT INTO nested_containers "
            + "(sourceid,targetid,nested) "
            + "VALUES(:sourceid,:targetid,:nested)";

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

    public Integer loadParentIdOfContainer(int cid) {
        List resultId = this.em.createNativeQuery(SQL_LOAD_PARENT_OF_CONTAINER).
                setParameter("containerid", cid).
                getResultList();
        return (Integer) resultId.get(0);
    }

    public Set<Integer> loadSubpath(int from, int to) {
        Set<Integer> fromPath = loadNestedInContainers(from);
        Set<Integer> toPath = loadNestedInContainers(to);
        fromPath.removeAll(toPath);
        return fromPath;
    }

    private void deletePathForContainer(int cid) {
        this.em.createNativeQuery(SQL_DELETE_PATH).setParameter("containerid", cid).executeUpdate();
    }

    private void addNewPath(int cid, Set<Integer> path) {
        Integer parentid = loadParentIdOfContainer(cid);
        for (Integer pe : path) {
            this.em.createNativeQuery(SQL_BUILD_PATH)
                    .setParameter("sourceid", cid)
                    .setParameter("targetid", pe)
                    .setParameter("nested", Objects.equals(parentid, pe))
                    .executeUpdate();
        }
    }

}
