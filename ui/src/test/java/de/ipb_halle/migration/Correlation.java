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
package de.ipb_halle.migration;

import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Migration tool for the InhouseDB
 *
 *
 * @author fbroda
 */
public class Correlation {
    
    public final static String INPUT_MOLPROC = "INPUT_MOLPROC";
    public final static String INPUT_ORGPROC = "INPUT_ORGPROC";

    public final static String MOLPROC_QUERY = "MOLPROC_QUERY";
    public final static String MOLPROC_UPDATE = "MOLPROC_UPDATE";
    public final static String ORGPROC_QUERY = "ORGPROC_QUERY";
    public final static String ORGPROC_UPDATE = "ORGPROC_UPDATE";

    private InhouseDB inhouseDB;
    private int folderId;

    public Correlation(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
        addInsertBuilders();
    }
    
    private void addInsertBuilders() {
    }

    private void createLinkedData() throws Exception {
        for(String sql : new String[] { MOLPROC_QUERY, MOLPROC_UPDATE, ORGPROC_QUERY, ORGPROC_UPDATE} )  {
            sql = this.inhouseDB.getConfigString(MOLPROC_QUERY);
            this.inhouseDB.getConnection().prepareStatement(sql).execute();
        }
    }

    private void importMolProc(String fileName) throws Exception {
        System.out.println("Importing correlation table compound / experiment");

        String pattern = "^(\\d+);(\\d+);(\\d+)$";

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        int line = 1;
        while(reader.ready()) {
                String st = reader.readLine(); 
                line++;

                int correlationId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int molId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                int experimentId = Integer.parseInt(st.replaceAll(pattern, "$3"));


                try {
                    saveMolProc(correlationId, molId, experimentId);
                    if ((line % 1000) == 0) {
                        System.out.printf("imported %d link records\n", line);
                    }
                } catch(Exception e) {
                    System.out.printf("Error in line %d\n", line);
                    throw e;
                }
        }
        reader.close();
    }

    private void importOrgProc(String fileName) throws Exception {
        System.out.println("Importing correlation table compound / experiment");

        String pattern = "^(\\d+);(\\d+);(\\d+)$";

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        int line = 1;
        while(reader.ready()) {
                String st = reader.readLine();
                line++;

                int correlationId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int organismId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                int experimentId = Integer.parseInt(st.replaceAll(pattern, "$3"));


                try {
                    saveOrgProc(correlationId, organismId, experimentId);
                    if ((line % 1000) == 0) {
                        System.out.printf("imported %d link records\n", line);
                    }
                } catch(Exception e) {
                    System.out.printf("Error in line %d\n", line);
                    throw e;
                }
        }
        reader.close();
    }

    public void importData() throws Exception {
        importMolProc(inhouseDB.getConfigString(INPUT_MOLPROC));
        importOrgProc(inhouseDB.getConfigString(INPUT_ORGPROC));
        createLinkedData();
    }

    private void saveMolProc(int correlationId, int molId, int experimentId) throws Exception {

        String sql = "INSERT INTO tmp_import (old_id, new_id, type) SELECT ? AS old_id, new_id AS new_id, 'MolProcMat' FROM tmp_import WHERE old_id=? AND type=?";
        this.inhouseDB.saveTriple(sql, correlationId, molId, Compounds.TMP_MatId_MolId);

        sql = "INSERT INTO tmp_import (old_id, new_id, type) SELECT ? AS old_id, new_id AS new_id, 'MolProcExp' FROM tmp_import WHERE old_id=? AND type=?";
        this.inhouseDB.saveTriple(sql, correlationId, experimentId, Experiments.TMP_Procedure);
    }

    private void saveOrgProc(int correlationId, int organismId, int experimentId) throws Exception {

        String sql = "INSERT INTO tmp_import (old_id, new_id, type) SELECT ? AS old_id, new_id AS new_id, 'OrgProcMat' FROM tmp_import WHERE old_id=? AND type=?";
        this.inhouseDB.saveTriple(sql, correlationId, organismId, Taxonomy.ORGANISM_ID_REF);

        sql = "INSERT INTO tmp_import (old_id, new_id, type) SELECT ? AS old_id, new_id AS new_id, 'OrgProcExp' FROM tmp_import WHERE old_id=? AND type=?";
        this.inhouseDB.saveTriple(sql, correlationId, experimentId, Experiments.TMP_Procedure);
    }

}
