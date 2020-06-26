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

import de.ipb_halle.lbac.globals.NestingService;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class TaxonomyNestingService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    protected String SQL_LOAD_PARENT_OF_CONTAINER
            = "SELECT ef.parentid "
            + "FROM effective_taxonomy ef "
            + "JOIN taxonomy t ON ef.parentid=t.id "
            + "WHERE ef.taxoid=:containerid "
            + "ORDER BY level desc "
            + "LIMIT 1 ";
    protected String SQL_LOAD_NESTED_CONTAINER
            = "SELECT parentid "
            + "FROM effective_taxonomy "
            + "WHERE taxoid =:id";
    protected String SQL_LOAD_SUB_CONTAINER
            = "SELECT taxoid "
            + "FROM effective_taxonomy "
            + "WHERE parentid =:id";
    protected String SQL_DELETE_PATH
            = "DELETE FROM effective_taxonomy "
            + "WHERE taxoid =:id";
    protected String SQL_BUILD_PATH
            = "INSERT INTO effective_taxonomy "
            + "(taxoid,parentid) "
            + "VALUES(:sourceid,:targetid)";

    public void updateParentOfTaxonomy(int taxonomyid, int newparentId) {
        NestingService service = new NestingService(em);
        service.setSqlLoadParentOfObject(SQL_LOAD_PARENT_OF_CONTAINER);
        service.setSqlLoadNestedObjects(SQL_LOAD_NESTED_CONTAINER);
        service.setSqlLoadSubObjects(SQL_LOAD_SUB_CONTAINER);
        service.setSqlDeletePath(SQL_DELETE_PATH);
        service.setSqlBuildPath(SQL_BUILD_PATH);

        service.updateNestedObjectFor(taxonomyid, newparentId);
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

}
