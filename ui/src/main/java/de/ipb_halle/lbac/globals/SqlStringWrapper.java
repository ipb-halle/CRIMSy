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

import de.ipb_halle.lbac.admission.ACPermission;

/**
 *
 * @author fmauz
 */
public class SqlStringWrapper {

    public static final String JOIN_KEYWORD = "#ACL_JOIN#";
    public static final String WHERE_KEYWORD = "#ACL_WHERE#";

    public static String aclWrapper(
            String originalSqlString,
            String acListColumn,
            String ownerColumn,
            ACPermission... permissions) {
        return originalSqlString
                .replaceAll(JOIN_KEYWORD, join(acListColumn))
                .replaceAll(WHERE_KEYWORD, where(ownerColumn, permissions))
                .replaceAll("  ", " ")
                .trim();
    }

    private static String join(String aclColumn) {
        return String.format(
                " JOIN acentries ace ON ace.aclist_id=%s "
                + " JOIN memberships me ON ace.member_id=me.group_id ",
                aclColumn);
    }

    private static String where(String ownerColumn, ACPermission... permissions) {
        String back = "";
        for (int i = 0; i < permissions.length; i++) {
            back += String.format(" ace.%s=true ", permissions[i].toString().toLowerCase());
            back += " AND ";
        }

        back += "(:userid=me.member_id "
                + "OR " + ownerColumn + "=:userid) ";
        return back;
    }
}
