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

import de.ipb_halle.lbac.exp.ExperimentEntity;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.exp.text.TextEntity;
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
 * Note: the procedures table needs extensive cleaning (invalid 3 letter codes, ...)
 *
 * @author fbroda
 */
public class Experiments {
    
    public final static String INPUT_EXPERIMENTS = "INPUT_EXPERIMENTS";
    public final static String EXPERIMENT_FOLDER_ID = "EXPERIMENT_FOLDER_ID";


    private InhouseDB inhouseDB;
    private int folderId;

    public Experiments(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
        this.folderId = inhouseDB.getConfigInt(EXPERIMENT_FOLDER_ID);
        addInsertBuilders();
    }
    
    private void addInsertBuilders() {
        this.inhouseDB.addInsertBuilder(ExperimentEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ExperimentEntity.class)));
        this.inhouseDB.addInsertBuilder(ExpRecordEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ExpRecordEntity.class)));
        this.inhouseDB.addInsertBuilder(TextEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(TextEntity.class)));
    }

    private void importExperiments(String fileName) throws Exception {
        System.out.println("Importing experiments");

        String pattern = "^'([A-Z]{2,3})';"             // 'RefProducerID';
            + "'([0-9]{3}[^']*)';"                      // 'IndividualCode';
            + "'(.*)';"                                 // 'LabJournal';
            + "([0-9]+);"                               // ProcedureID;
            + "([0-9]*);"                               // RefMol_ID;
            + ";"                                       // RefOrganismID;
            + ";"                                       // TransferDate;
            + "(\\d+\\.\\d+\\.\\d+ 00:00:00)?;"         // Date;
            + "('(.*)')?;"                              // ProcedureRemarks;
            + ";"                                       // TLC;
            + "('(.*)')?$";                             // FileNamePublication

        String datePattern = "(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+)";

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        int line = 1;
        while(reader.ready()) {
                String st = reader.readLine(); 
                line++;

                String threeLC = st.replaceAll(pattern, "$1");
                String code = st.replaceAll(pattern, "$2");
                String journal = st.replaceAll(pattern, "$3");
                String proc = st.replaceAll(pattern, "$4");

                int procId = Integer.parseInt(proc); 

                String dateStr = st.replaceAll(pattern, "$6");
                Date date = new Date();
                if (! dateStr.isEmpty()) {
                    int year = Integer.parseInt(dateStr.replaceAll(datePattern, "$3"));
                    int month = Integer.parseInt(dateStr.replaceAll(datePattern, "$2"));
                    int day = Integer.parseInt(dateStr.replaceAll(datePattern, "$1"));
                    if ((day > 0) && (day < 32) && (year > 1980) && (year < 2030) && (month > 0) && (month < 13)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.set(year, month - 1, day);
                        date = calendar.getTime();
                    }
                }

                String remarks = st.replaceAll(pattern, "$8");

//              if (! remarks.isEmpty()) {
//                  System.out.printf("%d \t %s  \t%s  \t%s  \t%s  \t%s\n", procId, threeLC, code, journal, date.toString(), remarks);
//              }
                try {
                    save(threeLC, code, journal, procId, date, remarks);
                    if ((line % 1000) == 0) {
                        System.out.printf("imported %d experiments\n", line);
                    }
                } catch(Exception e) {
                    System.out.printf("Error in line %d\n", line);
                    throw e;
                }
        }
        reader.close();

    }

    public void importData() throws Exception {
        importExperiments(inhouseDB.getConfigString(INPUT_EXPERIMENTS));
    }

    private void save(String threeLC, String code, String journal, int procId, Date date, String remarks) throws Exception {
        ExperimentEntity exp = new ExperimentEntity();
        exp.setCode(threeLC + code);
        exp.setCtime(date);
        exp.setDescription("Imported from NWC InhouseDB (IPB Halle)");
        exp.setFolderId(this.folderId);
        exp.setProjectid(this.inhouseDB.getConfigInt(InhouseDB.PROJECT_ID));
        exp.setOwner(this.inhouseDB.getConfigInt(InhouseDB.OWNER_ID)); 
        exp.setACList(this.inhouseDB.getConfigInt(InhouseDB.ACLIST_ID));

        // save exp
        exp = (ExperimentEntity) this.inhouseDB.getBuilder(exp.getClass().getName())
                .insert(this.inhouseDB.getConnection(), exp);

        ExpRecordEntity rec = new ExpRecordEntity();
        rec.setExperimentId(exp.getExperimentId());
        rec.setChangeTime(date);
        rec.setCreationTime(date);
        rec.setNext(null);
        rec.setRevision(1);
        rec.setType(ExpRecordType.TEXT);

        // save rec
        rec = (ExpRecordEntity) this.inhouseDB.getBuilder(rec.getClass().getName())
                .insert(this.inhouseDB.getConnection(), rec);

        StringBuilder sb = new StringBuilder();
        sb.append("<p>");
        sb.append("<b>Experiment:</b> ");
        sb.append(threeLC);
        sb.append(code);
        sb.append("<br/>");
        sb.append("<b>Lab journal (volume, page):</b> ");
        sb.append(journal);
        sb.append("<br/>");
        sb.append("<b>Remarks:</b> ");
        sb.append(remarks);
        sb.append("<br/></p>");

        TextEntity text = new TextEntity();
        text.setExpRecordId(rec.getExpRecordId());
        text.setText(sb.toString());
    
        // save text
        this.inhouseDB.getBuilder(text.getClass().getName())
                .insert(this.inhouseDB.getConnection(), text);

        // save reference
        String sql = "INSERT INTO tmp_import (old_id, new_id, type) VALUES (?, ?, ?)";
        this.inhouseDB.saveTriple(sql, procId, rec.getExpRecordId().intValue(), "Procedure");
    }
}
