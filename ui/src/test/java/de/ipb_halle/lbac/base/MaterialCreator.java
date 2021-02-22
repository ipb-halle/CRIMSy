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
package de.ipb_halle.lbac.base;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.material.MaterialType;

/**
 *
 * @author fmauz
 */
public class MaterialCreator {

    protected EntityManagerService entityManagerService;
    protected ACList detailRightsACList;

    public MaterialCreator(EntityManagerService entityManagerService) {
        this.entityManagerService = entityManagerService;
    }

    public void setDetailRightsACList(ACList detailRightsACList) {
        this.detailRightsACList = detailRightsACList;
    }

    public int createStructure(int userid, int aclid, Integer projectid, String... names) {
        int detailACListId = aclid;
        if (detailRightsACList != null) {
            detailACListId = detailRightsACList.getId();
        }
        entityManagerService.doSqlUpdate(
                String.format(
                        SQL_INSERT_MATERIAL,
                        MaterialType.STRUCTURE.getId(), aclid, userid, projectid));

        Integer materialid = (Integer) entityManagerService.doSqlQuery(MAX_MATERIAL_ID).get(0);

        for (int i = 1; i <= 6; i++) {
            entityManagerService.doSqlUpdate(String.format(SQL_INSERT_DETAIL_RIGHTS, materialid, detailACListId, i));
        }
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(" + materialid + ",'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(" + materialid + ",1,'')");
        for (String n : names) {
            entityManagerService.doSqlUpdate(
                    String.format(
                            SQL_INSERT_MATERIAL_INDEX,
                            materialid,
                            1,
                            n,
                            "de",
                            0));
        }

        return materialid;
    }

    public void addIndexToMaterial(int materialId, int indexType, String indexValue) {
        entityManagerService.doSqlUpdate(
                String.format(
                        SQL_INSERT_MATERIAL_INDEX,
                        materialId,
                        indexType,
                        indexValue,
                        "de",
                        0));
    }

    String SQL_INSERT_DETAIL_RIGHTS = "INSERT INTO materialdetailrights("
            + "materialid,aclistid,materialtypeid) VALUES(%d,%d,%d)";

    String SQL_INSERT_MATERIAL = "INSERT INTO MATERIALS("
            + "materialtypeid, "
            + "aclist_id, "
            + "ownerid, "
            + "deactivated,"
            + "projectid) "
            + "VALUES("
            + "%d,"
            + " %d, "
            + "%d, "
            + "false,"
            + "%d)";

    String SQL_INSERT_MATERIAL_INDEX
            = "INSERT INTO material_indices("
            + "materialid, "
            + "typeid, "
            + "value, "
            + "language, "
            + "rank)"
            + " VALUES(%d,%d,'%s','%s',%d)";

    String MAX_MATERIAL_ID = "SELECT MAX(materialid) FROM materials";

}
