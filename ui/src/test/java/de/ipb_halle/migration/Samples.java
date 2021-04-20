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

import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingId;
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
    
    public final static String CONTAINER_DIMENSIONS = "CONTAINER_DIMENSIONS";
    public final static String CONTAINERTYPE_OTHER = "CUPBOARD";
    public final static String CONTAINERTYPE_TRAY = "TRAY";
    public final static String CONTAINERTYPE_VIAL = "GLASS_VIAL";
    public final static String INPUT_SAMPLES = "INPUT_SAMPLES";
    public final static String INPUT_EXTRACTS = "INPUT_EXTRACTS";
    public final static String PARENT_CONTAINER_ID = "PARENT_CONTAINER_ID";
    public final static String UNKNOWN_CONTAINER = "UNKNOWN_CONTAINER";

    private InhouseDB inhouseDB;
    private int parentContainerId;
    private int projectId;
    private Map<String, ContainerEntity> containers;
    private Map<String, String> dimensions;

    public Samples(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
        addInsertBuilders();
        init();
    }
    
    private void addInsertBuilders() {
        this.inhouseDB.addInsertBuilder(ItemEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ItemEntity.class)));
        this.inhouseDB.addInsertBuilder(ContainerEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(ContainerEntity.class)));
    }

    private ContainerEntity createContainer(String name) throws Exception {
        ContainerEntity container = new ContainerEntity();
        container.setLabel(name);
        container.setProjectid(projectId);
        String dimension = getDimension(name);
        if (dimension != null) {
            container.setDimension(dimension);
            container.setType(CONTAINERTYPE_TRAY);
            container.setZeroBased(true);
        } else {
            container.setType(CONTAINERTYPE_OTHER);
        }

        container = (ContainerEntity) this.inhouseDB.getBuilder(container.getClass().getName())
                .insert(this.inhouseDB.getConnection(), container);
        this.containers.put(name, container);

        ContainerNestingEntity nesting = new ContainerNestingEntity();
        nesting.setId(new ContainerNestingId(this.parentContainerId, container.getId()));
        this.inhouseDB.getBuilder(nesting.getClass().getName())
                .insert(this.inhouseDB.getConnection(), nesting);

        return container;
    }

    public ContainerEntity getContainer(String place) throws Exception {
        if ((place == null) || place.isEmpty()) {
            return getContainer(UNKNOWN_CONTAINER);
        }
        ContainerEntity container = this.containers.get(place);
        if (container == null) {
            container = createContainer(place);
        }
        return container;
    }

    public Integer getMaterialId(int molid) throws Exception {
        String sql = "SELECT old_id FROM tmp_import WHERE new_id=? AND type=?";
        return this.inhouseDB.loadRefId(sql, molid, Compounds.TMP_MatId_MolId);
    }

    public Integer getMolId(int id) throws Exception {
        String sql = "SELECT old_id FROM tmp_import WHERE new_id=? AND type=?";
        return this.inhouseDB.loadRefId(sql, id, Correlation.CORRELATION_MOLPROCMAT);
    }

    /**
     * return a dimensions string for a given container name
     */
    private String getDimension(String name) {
        String pattern = "^([A-Za-z]+).*$";
        String prefix = name.replaceAll(pattern, "$1");
        return this.dimensions.get(prefix);
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


    private void init() {
        this.containers = new HashMap<> ();
        this.dimensions = new HashMap<> ();
        this.projectId = inhouseDB.getConfigInt(InhouseDB.PROJECT_ID);
        this.parentContainerId = inhouseDB.getConfigInt(PARENT_CONTAINER_ID);

        /* 
         * Intialize container dimensions. Dimensions are specified as 
         * "prefix.dimensionString[/prefix.dimensionString]*", e.g. "TS.10;25;1/TM.8;15;1/TL...."
         * 
         * The pattern given below should capture container prefixes 
         * like TS, TM, TL, TH, MTP, ...
         */
        String pattern = "^([A-Za-z]+)\\.([0-9;]+)$";
        String[] dim = inhouseDB.getConfigString(CONTAINER_DIMENSIONS).split("/");
        for (String d : dim) {
            this.dimensions.put(d.replaceAll(pattern, "$1"), d.replaceAll(pattern, "$2"));
        }
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

        ContainerEntity container = getContainer(place);
        Integer molId = getMolId(molProcId);
        Integer materialId = getMaterialId(molId);

        StringBuilder sb = new StringBuilder();
        sb.append("Samplecode: ");
        sb.append(sampleCode);
        sb.append(String.format("; MolId: %d", molId));
        sb.append(String.format("; Tara: %.3f mg; Appearance: ", tara));
        sb.append(appearance);
        sb.append("; Remarks: ");
        sb.append(remarks);

        ItemEntity item = new ItemEntity();
        item.setAmount(amount);
        item.setUnit("mg");
        item.setConcentration(Double.valueOf(purity));
        item.setConcentrationUnit("%");
        item.setDescription(sb.toString());
        item.setMaterialid(materialId);
        item.setProjectid(this.inhouseDB.getConfigInt(InhouseDB.PROJECT_ID));
        item.setPurity(purity);                                                  // free text might not be appropriate!
//      item.setLabel();
        item.setContainertype(CONTAINERTYPE_VIAL);
        item.setContainerid(container.getId());

        item = (ItemEntity) this.inhouseDB.getBuilder(item.getClass().getName())
                .insert(this.inhouseDB.getConnection(), item);


        /* item position */

        /* tmp reference */

    }
}