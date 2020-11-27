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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
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
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
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
 *  INPUT_STRUCTURE_NAMES: "/dataPOOL/fblocal/inhouse/tblCompoundSynonym_20140325.txt",
 *  INPUT_STRUCTURES:      "/dataPOOL/fblocal/inhouse/Structure_20140325.SDF",
 *
 *  MOLECULE_MATERIAL_TYPE_ID: 1,
 *  OWNER_ID:       1,
 *  PROJECT_ID:     1
 * }
 * </pre>
 *
 * @author fbroda
 */
public class InhouseDB {
    
    public final static String ACLIST_ID = "ACLIST_ID";
    public final static String DATABASE_URL = "DATABASE_URL";

    public final static String INPUT_STRUCTURE_NAMES = "INPUT_STRUCTURE_NAMES";
    public final static String INPUT_STRUCTURES = "INPUT_STRUCTURES";

    public final static String MOLECULE_MATERIAL_TYPE_ID = "MOLECULE_MATERIAL_TYPE_ID";
    public final static String OWNER_ID = "OWNER_ID";
    public final static String PROJECT_ID = "PROJECT_ID";

    private final Connection connection;
    private final Map<String, SqlInsertBuilder> builderMap;

    private JsonObject jsonConfig;

    private InhouseDB(String configFileName) throws Exception {
        readConfig(configFileName);
        this.connection = DriverManager.getConnection(getConfigString(DATABASE_URL));
        this.builderMap = new HashMap<> ();
        addInsertBuilders();
    }
    
    private void addInsertBuilders() {
        this.builderMap.put(MaterialEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MaterialEntity.class)));
        this.builderMap.put(StructureEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(StructureEntity.class)));
        this.builderMap.put(MoleculeEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MoleculeEntity.class)));
    }
    
    private void close() throws SQLException {
        this.connection.close();
    }

    private Integer getConfigInt(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return Integer.valueOf(value.getAsInt());
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

    private String getConfigString(String key) {
        JsonPrimitive value = this.jsonConfig.getAsJsonPrimitive(key);
        if (value != null) {
            return value.getAsString();
        }
        throw new NullPointerException("getConfigInt(" + key + ") returned null");
    }

//    private void importExperiments() throws Exception {
//        throw new Exception("Hallo");
//    }
            

    private void importCompounds(String fileName) throws Exception {

        MolecularFormulaManipulator manipulator = new MolecularFormulaManipulator();

        IteratingSDFReader sdfReader = new IteratingSDFReader(
                new FileInputStream(fileName),
                DefaultChemObjectBuilder.getInstance());

        while (sdfReader.hasNext()) {
            IAtomContainer cdkMolecule = sdfReader.next();
            IMolecularFormula cdkFormula = manipulator.getMolecularFormula(cdkMolecule);
            String molFormula = manipulator.getString(cdkFormula);
            double molMass = manipulator.getTotalNaturalAbundance(cdkFormula); 
            importSingleCompound(cdkMolecule, molFormula, molMass);
        }
    }

    private void importCompoundNames(String fileName) {

    }

    private void importData() throws Exception {
        importCompounds(getConfigString(INPUT_STRUCTURES));
        importCompoundNames(getConfigString(INPUT_STRUCTURE_NAMES));
    }

    private void importSingleCompound(IAtomContainer cdkMolecule, String molFormula, Double molMass) throws Exception {
        MaterialEntity mat = new MaterialEntity();
        mat.setAclist_id(getConfigInt(ACLIST_ID));
        mat.setCtime(new Date());
        mat.setMaterialtypeid(getConfigInt(MOLECULE_MATERIAL_TYPE_ID));
        mat.setOwnerid(getConfigInt(OWNER_ID));
        mat.setProjectid(getConfigInt(PROJECT_ID));
        
        mat = (MaterialEntity) this.builderMap
                .get(mat.getClass().getName())
                .insert(connection, mat);
        
        MoleculeEntity mol = new MoleculeEntity();
        saveMolString(mol, cdkMolecule);
        mol = (MoleculeEntity) this.builderMap
                .get(mol.getClass().getName())
                .insert(connection, mol);
                
        StructureEntity struc = new StructureEntity();
        struc.setId(mat.getMaterialid());
        struc.setMolarmass(molMass);
        struc.setSumformula(molFormula);
        struc.setMoleculeid(mol.getId());
        this.builderMap
                .get(struc.getClass().getName())
                .insert(connection, struc);

        saveMolProperties(cdkMolecule, mat.getMaterialid());
    }
    
    private void readConfig(String fileName) throws Exception {
        JsonElement element = JsonParser.parseReader(
            new FileReader(fileName));
        if (! element.isJsonObject()) {
            throw new Exception("readConfig() could not parse Json object");
        }
        this.jsonConfig = element.getAsJsonObject();
    }
    
    private void saveMolProperties(IAtomContainer cdkMolecule, Integer id) {
        // molId
        // IPBcode
        // date
        // reliability
        // InChi, InChiKey
        //
    }

    private void saveMolString(MoleculeEntity mol, IAtomContainer cdkMolecule) throws CDKException, IOException {
        ByteArrayOutputStream molStream = new ByteArrayOutputStream();
        IChemObjectWriter cdkWriter;
        if (cdkMolecule.getAtomCount() > 900) {
            mol.setFormat("V3000");
            cdkWriter = new MDLV3000Writer(molStream);
        } else {
            mol.setFormat("V2000");
            cdkWriter = new MDLV2000Writer(molStream);
        }
        cdkWriter.write(cdkMolecule);
        cdkWriter.close();
        mol.setMolecule(molStream.toString());
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
