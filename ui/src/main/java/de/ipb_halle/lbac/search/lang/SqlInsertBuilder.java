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
package de.ipb_halle.lbac.search.lang;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds an INSERT statement for an EntityGraph using given
 * entity annotations. NOTE: The methods of 
 * this class are not thread safe.
 *
 * @author fbroda
 */
public class SqlInsertBuilder {

    private EntityGraph entityGraph;
    private List<DbField> allFields;
    private String insertSql;

    /**
     * constructor
     */
    public SqlInsertBuilder(EntityGraph graph) {
        this.entityGraph = graph;
        this.allFields = new ArrayList(this.entityGraph.getAllFields());
        this.insertSql = insert();
        
    }

    public EntityGraph getEntityGraph() {
        return this.entityGraph;
    }
    
    protected String getInsertSql() {
        return this.insertSql;
    }

    /**
     * create an insert statement
     */
    private String insert() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbParam = new StringBuilder();
//        List<DbField> generatedFields = new ArrayList<> ();
        String sep = " (";

        sb.append("INSERT INTO ");
        sb.append(this.entityGraph.getTableName());

        for (DbField field : this.allFields) {
            if (! field.isGeneratedField()) {
//                generatedFields.add(field);
//            } else {
                sb.append(sep);
                sb.append(field.getColumnName());
                sbParam.append(sep);
                sbParam.append("?");
                sep = ", ";
            }
        }
        sb.append(") VALUES");
        sb.append(sbParam);
        sb.append(")" );

//        sep = " RETURNING ";
//        for (DbField field : generatedFields) {
//            sb.append(sep);
//            sb.append(field.getColumnName());
//            sep = ", ";
//        }

        return sb.toString();
    }

    public Object insert(Connection conn, Object obj) throws Exception {
        PreparedStatement statement = conn.prepareStatement(
                insertSql, Statement.RETURN_GENERATED_KEYS);
        int index = 1;
        List<DbField> generatedValues = new ArrayList<> ();

        for (DbField field : this.allFields) {
            if (! field.isGeneratedField()) {
                Object value = field.get(obj);
                if (value != null) {
                    setParameter(index, statement, value);
                } else {
                    setNull(index, statement, field.getFieldClass());
                }
                index++;
            } else {
                generatedValues.add(field);
            }
        }

        statement.executeUpdate();
        if (generatedValues.size() > 0) {
            index = 1;
            ResultSet result = statement.getGeneratedKeys();

            for (DbField field : generatedValues) {
                    field.set(obj, result.getObject(index));
                    index++;
            }
            result.close();
        }
        return obj;
    }

    private void setNull(int index, PreparedStatement statement, Class clazz) 
            throws SQLException {
        switch(clazz.getName()) {
            case "java.lang.Boolean" :
                statement.setNull(index, Types.BOOLEAN);
                break;
            case "java.lang.Date" :
                statement.setNull(index, Types.DATE);
                break;
            case "java.lang.Double" :
                statement.setNull(index, Types.DOUBLE);
                break;
            case "java.lang.Float" :
                statement.setNull(index, Types.FLOAT);
                break;
            case "java.lang.Integer" :
                statement.setNull(index, Types.INTEGER);
                break;
            case "java.lang.Long" :
                statement.setNull(index, Types.BIGINT);
                break;
            case "java.lang.String" :
                statement.setNull(index, Types.VARCHAR);
                break;
            case "java.sql.Timestamp" :
                statement.setNull(index, Types.TIMESTAMP);
                break;
            case "java.util.UUID" :
                statement.setNull(index, Types.VARCHAR);
            default : throw new IllegalArgumentException("Unrecognized type: " 
                + clazz.getName());
        }
    }

    private void setParameter(int index, PreparedStatement statement, Object obj) 
            throws SQLException {
        switch(obj.getClass().getName()) {
            case "java.lang.Boolean" :
                statement.setBoolean(index, (Boolean) obj);
                break;
            case "java.lang.Date" :
                statement.setDate(index, new java.sql.Date(
                    ((Date) obj).getTime()));
                break;
            case "java.lang.Double" :
                statement.setDouble(index, (Double) obj);
                break;
            case "java.lang.Float" :
                statement.setFloat(index, (Float) obj);
                break;
            case "java.lang.Integer" : 
                statement.setInt(index, (Integer) obj);
                break;
            case "java.lang.Long" :
                statement.setLong(index, (Long) obj);
                break;
            case "java.lang.String" :
                statement.setString(index, (String) obj);
                break;
            case "java.sql.TimeStamp" :
                statement.setTimestamp(index, (Timestamp) obj);
                break;
//            case "java.util.UUID" :
//                statement.setString(index, obj.toString());
//                break;
            default :
                throw new IllegalArgumentException("Unrecognized type: "
                    + obj.getClass().getName());           
       } 
    }
}
