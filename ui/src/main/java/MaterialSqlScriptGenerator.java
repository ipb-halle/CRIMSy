
import java.io.FileWriter;

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
/**
 * Creates a script to insert many materials inclusive a history into database.
 * <ol>
 * <li>copy the file from target folder to lbac server into data folder</li>
 * <li>psql -q -f /data/db/insertMaterials.sql -U lbac lbac</li>
 * </ol>
 * Parameter
 * <ol>
 * <li>amount of materials</li>
 * </ol>
 * @author fmauz
 */
public class MaterialSqlScriptGenerator {

    static int MATERIALS = 10000;
    static final String ACLIST_ID = "7e298ef6-e6cf-4a22-977b-4acd9ed11793";
    static final String USER_ID = "088e3bc0-7fb2-422e-b29a-71ca3ec907d2";

    public static void main(String[] args) throws Exception {
        String targetFile = "target/insertMaterials.sql";
        FileWriter fw = new FileWriter(targetFile);
        int indexHistoryId = 1;
        fw.append("BEGIN TRANSACTION;" + System.lineSeparator());
        int materialIndeyKey=1;
        for (int i = 0; i < MATERIALS; i++) {
            //Insert the material, every 1000th material is deactivated
            String sql = String.format(
                    "INSERT INTO materials VALUES(%d,1,now(),cast('%s' as UUID),cast('%s' as UUID),%s,1);",
                    i, ACLIST_ID, USER_ID, i % 1000 == 0 ? "true" : "false");
            fw.append(sql + System.lineSeparator());
            //Add a material name, every third got two names, every fifths got a
            //additional CAS Number
            fw.append("INSERT INTO material_indices VALUES(" + materialIndeyKey + "," + i + ",1,'TESTMATERIAL_" + i + "','de',0);" + System.lineSeparator());
            if (i % 3 == 0) {
                materialIndeyKey++;
                fw.append("INSERT INTO material_indices VALUES(" + materialIndeyKey + "," + i + ",1,'ALTERNATIVENAME_" + i + "','en',1);" + System.lineSeparator());
            }
             if (i % 5 == 0) {
                 materialIndeyKey++;
                 fw.append("INSERT INTO material_indices VALUES(" + materialIndeyKey + "," + i + ",3,'CAS: " + i + "',null,null);" + System.lineSeparator());
             }
             materialIndeyKey++;

            //Add structure information
            fw.append("INSERT INTO structures VALUES (" + i + ",'xx',0,0,null);" + System.lineSeparator());

            //Every second material has a history in ther indices
            if (i % 2 == 0) {
                fw.append("INSERT INTO material_indices_hist"
                        + " VALUES (" + indexHistoryId + ","
                        + i + ","
                        + "1, "
                        + "now(),"
                        + "cast('088e3bc0-7fb2-422e-b29a-71ca3ec907d2' as UUID),"
                        + "'',"
                        + "'TESTMATERIAL_" + i + " (old)',"
                        + "'TESTMATERIAL_" + i + "',"
                        + "null,null,null,null);" + System.lineSeparator());
                indexHistoryId++;
            }

            fw.append("INSERT INTO storages VALUES (" + i + ",1,null);" + System.lineSeparator());
        }
        fw.append("END TRANSACTION;" + System.lineSeparator());
        fw.flush();
        fw.close();
    }

}
