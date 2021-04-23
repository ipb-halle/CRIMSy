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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;

import java.io.ByteArrayOutputStream;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * Migration tool for the InhouseDB
 * This is work in progress.
 *
 * Example config file:
 * <pre>
 *  {
 *  ACLIST_ID:      1,
 *  DATABASE_URL:   "jdbc:postgresql://localhost:5432/lbac?charSet=UTF-8&user=lbac&password=lbac",
 *
 *  INITIAL_SQL: ["DROP ...", "INSERT ...", ...],
 *  INPUT_STRUCTURE_NAMES: "/dataPOOL/fblocal/inhouse/tblCompoundSynonym_20140325.txt",
 *  INPUT_STRUCTURES:      "/dataPOOL/fblocal/inhouse/Structure_20140325.SDF",
 *
 *  ... lots of other config ...
 * 
 *  MOLECULE_MATERIAL_TYPE_ID: 1,
 *  OWNER_ID:       1,
 *  PROJECT_ID:     1
 * }
 * </pre>
 * 
 * After mvn test-compile, this tool will be usually run from the REPO/ui direcory using the following command:
 * 
 * <pre>
 * java -cp "target/classes:target/test-classes:target/ui-1.3.0/WEB-INF/lib/*" de.ipb_halle.migration.InhouseDB
 * </pre>
 *
 * @author fbroda
 */
public class InhouseDB {
    
    public final static String ACLIST_ID = "ACLIST_ID";
    public final static String DATABASE_URL = "DATABASE_URL";
    public final static String INITIAL_SQL = "INITIAL_SQL";
    public final static String MATERIAL_INDEX_NAME = "name";
    public final static String OWNER_ID = "OWNER_ID";
    public final static String PROJECT_ID = "PROJECT_ID";

    public final static String UNKNOWN_COMPOUND_ID = "UNKNOWN_COMPOUND_ID";


    private final Connection connection;
    private final Map<String, SqlInsertBuilder> builderMap;
    private final Map<String, Integer> materialIndexTypes;
    private int aclist;
    private int owner;
    private int project;
    private int unknownCompoundId;

    private JsonObject jsonConfig;

    private InhouseDB(String configFileName) throws Exception {
        readConfig(configFileName);
        this.connection = DriverManager.getConnection(getConfigString(DATABASE_URL));
        this.builderMap = new HashMap<> ();
        this.materialIndexTypes = new HashMap<> ();
        addMaterialIndexTypes();
    }
    
    public void addInsertBuilder(String name, SqlInsertBuilder builder) {
            this.builderMap.put(name, builder);
    }

    private void addMaterialIndexTypes() throws Exception {
        PreparedStatement stmnt = this.connection.prepareStatement("SELECT id, name FROM indextypes");
        ResultSet result = stmnt.executeQuery();
        while(result.next()) {
            this.materialIndexTypes.put(result.getString(2), result.getInt(1));
        }
        this.materialIndexTypes.put("CAS-RN", this.materialIndexTypes.get("CAS/RN"));
    }

    private void close() throws SQLException {
        this.connection.close();
    }

    public int getACList() {
        return aclist;
    }

    public SqlInsertBuilder getBuilder(String name) {
        return this.builderMap.get(name);
    }

    public Integer getConfigInt(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return Integer.valueOf(value.getAsInt());
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

    public String getConfigString(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return value.getAsString();
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Integer getMaterialIndexType(String name) {
        return this.materialIndexTypes.get(name);
    }

    public Map<String, Integer> getMaterialIndexTypes() {
        return this.materialIndexTypes;
    }

    public int getOwner() {
        return this.owner;
    }

    public int getProject() {
        return this.project;
    }

    public int getUnknownCompoundId() {
        return this.unknownCompoundId;
    }

    private void importData() throws Exception {
        init();
        Compounds compounds = new Compounds(this);
        Taxonomy taxonomy = new Taxonomy(this);
        Experiments experiments = new Experiments(this);
        Correlation correlation = new Correlation(this);
        Samples samples = new Samples(this);

/*
        runInitial();
        compounds.importData();
        taxonomy.importData();
        experiments.importData();
        correlation.importData();
*/
        samples.importData();
    }

    private void init() {
        this.aclist = getConfigInt(ACLIST_ID);
        this.owner = getConfigInt(OWNER_ID);
        this.project = getConfigInt(PROJECT_ID);
        this.unknownCompoundId = getConfigInt(UNKNOWN_COMPOUND_ID);
    }

    public int loadRefId(String sql, int id, String refKey) throws Exception {
        PreparedStatement stmnt = this.connection.prepareStatement(sql);
        stmnt.setInt(1, id);
        stmnt.setString(2, refKey);
        ResultSet result = stmnt.executeQuery();
        if (result.next()) {
            return result.getInt(1);
        }
        System.out.printf("No reference found: %s-%d\n", refKey, id);
        return 0;
    }

    private void readConfig(String fileName) throws Exception {
        JsonElement element = JsonParser.parseReader(
            new FileReader(fileName));
        if (! element.isJsonObject()) {
            throw new Exception("readConfig() could not parse Json object");
        }
        this.jsonConfig = element.getAsJsonObject();
    }
    
    private void runInitial() throws Exception {
        JsonArray array = this.jsonConfig.getAsJsonArray(INITIAL_SQL);
        if (array != null) {
            Iterator<JsonElement> iter = array.iterator();
            while (iter.hasNext()) {
                String sql = iter.next().getAsString();
                this.connection.prepareStatement(sql).execute();
            }
        }
    }

    public void saveTriple(String sql, Integer id, Integer other, String value) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setInt(1, id);
        statement.setInt(2, other);
        statement.setString(3, value);
        statement.execute();
    }

    public static void doTheStuff(String config) {
        try {
            InhouseDB inhouseDB = new InhouseDB(config);
            inhouseDB.importData();
            inhouseDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        if (argv.length != 1) {
//          System.out.println("Usage: java -cp ... InhouseDB JSON_CONFIG_FILE");

            // during development / debugging
            doTheStuff("/dataPOOL/fblocal/inhouse/config.json");
            return;
        }

        doTheStuff(argv[0]);
    }
}
