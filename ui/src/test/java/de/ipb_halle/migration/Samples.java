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

import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
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
public class Samples {
    
    public final static String INPUT_SAMPLES = "INPUT_SAMPLES";
    public final static String INPUT_EXTRACTS = "INPUT_EXTRACTS";
    public final static String UNKNOWN_CONTAINER = "UNKNOWN_CONTAINER";

    private InhouseDB inhouseDB;
    private Map<String, ContainerEntity> containers;

    public Samples(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
        addInsertBuilders();
        this.containers = new HashMap<> ();
    }
    
    private void addInsertBuilders() {
        this.inhouseDB.addInsertBuilder(ItemEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ItemEntity.class)));
        this.inhouseDB.addInsertBuilder(ContainerEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ContainerEntity.class)));
    }

    private ContainerEntity createContainer() {
        return null;
    }

    public ContainerEntity getContainer(String place) {
        if ((place == null) || place.isEmpty()) {
            return containers.get(UNKNOWN_CONTAINER);
        }
        return null;
    }

    public void importData() throws Exception {
        importSamples(inhouseDB.getConfigString(INPUT_SAMPLES));
    }

    private void importExtracts(String fileName) throws Exception {
        System.out.println("Importing extract samples");

    }

    private void importSamples(String fileName) throws Exception {
        System.out.println("Importing compound samples");

        /* 01 SampleID                          internal id
         * 02 RefCorrProcedure_MoltableID       molProcId
         * 03 RefLastSolventUsed
         * 04 RefPermissionID                   ignored
         * 05 StorePlace
         * 06 LerbsMarker                       ignored
         * 07 Sample                            sample code
         * 08 Amount                            amount [mg]
         * 09 Tara                              tara [mg], store as remarks? 
         * 10 Purity                            given as percentage
         * 11 PhysicalPhase                     store as remarks
         * 12 ClaksID                           ignored (given for 27 items)
         * 13 SampleRemarks                     store as remarks
         * 14 HPLC                              unused
         * 15 Synthesized                       unused
         * 16 Isolated                          unused
         * 17 BiolDataAvailable                 unused
         */


        String pattern = "^(\\d+);"             //  1 sampleId
                + "(\\d+);"                     //  2 molProcId
                + "('(.*)')?;"                  //  4 last solvent
                + "(\\d?);"                     //  5 permission
                + "('(.*)')?;"                  //  7 storage place
                + "('.*')?;"                    //  8 "Lerbs-Marker"
                + "('(.*)')?;"                  // 10 sample code
                + "(\\d+,\\d+)?;"               // 11 amount [mg]
                + "(\\d+,\\d+)?;"               // 12 tara [mg]
                + "(\\d+)?;"                    // 13 purity
                + "('(.*)')?;"                  // 15 physical phase, 
                + "('\\d{10}')?;"               // 16 CLAKS-Id / KICKS-Label
                + "('(.*)')?;"                  // 18 remarks
                + "0;0;0;0$";                   //  - unused fields

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        int line = 1;
        while(reader.ready()) {
                String st = reader.readLine(); 
                line++;

                int sampleId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int molProcId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                String solvent = st.replaceAll(pattern, "$4");
                String place = st.replaceAll(pattern, "$7");
                String sampleCode = st.replaceAll(pattern, "$10");
                double amount = parseDecimalString(st.replaceAll(pattern, "$11"));
                double tara = parseDecimalString(st.replaceAll(pattern, "$12"));
                String purity = st.replaceAll(pattern, "$13");
                String appearance = st.replaceAll(pattern, "$15");
                String remarks = st.replaceAll(pattern, "$18");

//              System.out.printf("SAMPLE: %d \t%d \t%s \t%f \t%f \t%s \t%s \t%s \t%s\n", sampleId, molProcId, purity,
//                  amount, tara, place, solvent, appearance, remarks);

                try {
                    saveSample(sampleId, molProcId, solvent, place, sampleCode, amount, tara, purity, appearance, remarks);
                    if ((line % 1000) == 0) {
                        System.out.printf("imported %d samples\n", line);
                    }
                } catch(Exception e) {
                    System.out.printf("Error in line %d\n", line);
                    throw e;
                }
        }
        reader.close();
    }

    /**
     * numbers are given in German locale (',' as decimal separator)
     * @param a string representation of a number
     * @param the double value of the number (or 0.0)
     */
    private double parseDecimalString(String decimal) {
        if ((decimal != null) && (! decimal.isEmpty())) {
            return Double.parseDouble(decimal.replaceAll(",", "."));
        }
        return 0.0;
    }

    private void saveSample(int sampleId, int molProcId, String solvent, String place, String sampleCode, 
                double amount, double tara, String purity, String appearance, 
                String remarks) throws Exception {

/*
        ContainerEntity container = getContainer(place);
        Material material= getMaterial(molProcId);

        Item item = new Item();
        item.setAmount(amount);
        item.setUnit("mg");
        item.setConcentration(...purity...);
        item.setDescription(...);
        item.setMaterialid(material.getMaterialid());
        item.setProjectId(this.inhouseDB.getConfigInt(InhouseDB.PROJECT_ID));
        item.setPurity(...purity...);
        item.setSolventid(...);
        item.setLabel();
        item.setContainertype(... glass / plastic vial ...);
        item.setContainerid(container.getId());

*/



    }
}
