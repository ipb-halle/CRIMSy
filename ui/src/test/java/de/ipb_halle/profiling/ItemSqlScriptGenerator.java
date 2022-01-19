package de.ipb_halle.profiling;


import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
/**
 * Creates a script to insert many items inclusive a history into database.
 * <ol>
 * <li>copy the file from target folder to lbac server into data folder</li>
 * <li>psql -q -f /data/db/insertItems.sql -U lbac lbac</li>
 * </ol>
 * Parameter
 * <ol>
 * <li>Range of materialids </li>
 * <li>amount of items</li>
 * </ol>
 *
 * @author fmauz
 */
public class ItemSqlScriptGenerator {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS");
    static int MIN_MATERIAL_ID = 1;
    static int MAX_MATERIAL_ID = 5000;
    static int ITEMS = 100000;
    static final String ACLIST_ID = "7e298ef6-e6cf-4a22-977b-4acd9ed11793";
    static final String USER_ID = "088e3bc0-7fb2-422e-b29a-71ca3ec907d2";

    public static void main(String[] args) throws Exception {
        String targetFile = "target/insertItems.sql";
        FileWriter fw = new FileWriter(targetFile);
        fw.append("BEGIN TRANSACTION;" + System.lineSeparator());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < ITEMS; i++) {
            int materialid = (int) (Math.random() * (MAX_MATERIAL_ID - MIN_MATERIAL_ID) + MIN_MATERIAL_ID);
            int amount = (int) (Math.random() * (1000));
            int projectid = 1;
            int concentration = (int) (Math.random() * (100));

            String sql = String.format("INSERT INTO items ("
                    + "id,"
                    + "materialid,"
                    + "amount,"
                    + "articleid,"
                    + "projectid,"
                    + "concentration,"
                    + "unit,"
                    + "purity,"
                    + "solventid,"
                    + "description,"
                    + "containersize,"
                    + "containertype,"
                    + "containerid,"
                    + "owner,"
                    + "ctime) "
                    + "VALUES("
                    + "%d," //itemid
                    + "%d," //materialid
                    + "%d," // amount
                    + "null," //articleid
                    + "%d," // projectid
                    + "%d," // concentration
                    + "'kg',"
                    + "'pure',"
                    + "null," //solventid
                    + "'item %s'," //description                    
                    + "100," //containersize
                    + "'GLASS_FLASK',"
                    + "null,"
                    + "cast('%s' as UUID),"
                    + "now());",
                    i + 1, materialid,
                    amount, projectid, concentration, Integer.toString(i), USER_ID);
            fw.append(sql + System.lineSeparator());

//            //Every second item has up to 5 histories in their descritption or amount
            if (i % 2 == 0) {
                int histories = (int) (Math.random() * 5 + 1);
                int oldAmount = amount;
                for (int j = 0; j < histories; j++) {
                    cal.add(Calendar.MILLISECOND, 10);
                    int newAmount = (int) oldAmount / 2;
                    fw.append(String.format("INSERT INTO items_history(id,mdate,actorid,action,amount_old ,amount_new) "
                            + "Values(%d,to_timestamp('%s','YYYYMMDD HH24:MI:SS:MS'),cast('%s' as UUID),'EDIT',%d,%d );",
                            i + 1,
                            SDF.format(cal.getTime()),
                            USER_ID, oldAmount, newAmount));
                    oldAmount = newAmount;
                    fw.append(System.lineSeparator());
                }
                fw.append("UPDATE items set amount=" + oldAmount + " where id=" + (i + 1) + ";");
                fw.append(System.lineSeparator());
            }
        }
        fw.append("END TRANSACTION;" + System.lineSeparator());
        fw.flush();
        fw.close();
    }

}
