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

/**
 *
 * @author fmauz
 */
public class SqlStringEnchanter {

    public static String[] splitSqlAtKeyword(String sql, String keyword) {
        int lastIndex = sql.toLowerCase().lastIndexOf(keyword.toLowerCase());
        if (lastIndex == -1) {
            return new String[]{sql};
        }
        String firstPart = sql.substring(0, lastIndex);
        String lastPart = sql.substring(lastIndex, sql.length());
        return new String[]{firstPart, lastPart};
    }

    public static String aclEnchanter(String originalSql, String aclColumn, boolean read) {

        String[] splittedBeforeWhere = splitSqlAtKeyword(originalSql, "where");
        switch (splittedBeforeWhere.length) {
            case 2:
                return splittedBeforeWhere[0]
                        + aclTableJoin(aclColumn)
                        + aclPermissionConditionWithWhere(read)
                        + splittedBeforeWhere[1].substring(5, splittedBeforeWhere[1].length());
            case 1:
                String[] splittedBeforeGroupBy = splitSqlAtKeyword(originalSql, "group by");
                switch (splittedBeforeGroupBy.length) {
                    case 1:
                        return splittedBeforeGroupBy[0] + aclTableJoin(aclColumn) + aclPermissionConditionWithoutWhere(read);
                    case 2:
                        return splittedBeforeGroupBy[0] + aclTableJoin(aclColumn) + aclPermissionConditionWithoutWhere(read) + splittedBeforeGroupBy[1];
                    default:
                        throw new RuntimeException("Strings with multiple where clauses(e.g. union) are not supported");
                }
            default:
                throw new RuntimeException("Strings with multiple where clauses(e.g. union) are not supported");
        }
    }

    public static String aclTableJoin(String aclColumn) {
        return String.format(
                " JOIN acentries ace ON ace.aclist_id=%s "
                + " JOIN memberships me ON ace.member_id=me.group_id ",
                aclColumn);
    }

    public static String aclPermissionConditionWithWhere(boolean read) {
        String back = " WHERE";
        if (read) {
            back += " ace.permread=true ";
        }
        return back + " AND ";
    }

    public static String aclPermissionConditionWithoutWhere(boolean read) {
        String back = " WHERE";
        if (read) {
            back += " ace.permread=true ";
        }
        return back;
    }
}
