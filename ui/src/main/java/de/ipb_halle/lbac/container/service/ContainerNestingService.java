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

import de.ipb_halle.lbac.globals.NestingService;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service for manipulating the nesting of containers
 *
 * @author fmauz
 */
@Stateless
public class ContainerNestingService implements Serializable {

    private NestingService nestingService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    protected String SQL_LOAD_PARENT_OF_CONTAINER
            = "SELECT parentcontainer "
            + "FROM containers "
            + "WHERE id=:id";
    protected String SQL_LOAD_NESTED_CONTAINER
            = "SELECT targetid "
            + "FROM nested_containers "
            + "WHERE sourceid =:id";
    protected String SQL_LOAD_SUB_CONTAINER
            = "SELECT sourceid "
            + "FROM nested_containers "
            + "WHERE targetid =:id";
    protected String SQL_DELETE_PATH
            = "DELETE FROM nested_containers "
            + "WHERE sourceid =:id";
    protected String SQL_BUILD_PATH
            = "INSERT INTO nested_containers "
            + "(sourceid,targetid,nested) "
            + "VALUES(:sourceid,:targetid,:nested)";

    @PostConstruct
    public void init() {
        nestingService = new NestingService(em);
        nestingService.setSqlLoadParentOfObject(SQL_LOAD_PARENT_OF_CONTAINER);
        nestingService.setSqlLoadNestedObjects(SQL_LOAD_NESTED_CONTAINER);
        nestingService.setSqlLoadSubObjects(SQL_LOAD_SUB_CONTAINER);
        nestingService.setSqlDeletePath(SQL_DELETE_PATH);
        nestingService.setSqlBuildPath(SQL_BUILD_PATH);
    }

    /**
     * Updates the nesting path of the container
     *
     * @param idOfEditedContainer
     * @param newparentId
     */
    public void updateContainerNesting(int idOfEditedContainer, Integer newparentId) {
        nestingService.updateNestedObjectFor(idOfEditedContainer, newparentId);
    }

    public NestingService getNestingService() {
        return nestingService;
    }

    public Set<Integer> loadNestedInObjects(int id) {
        return nestingService.loadNestedInObjects(id);
    }

}
