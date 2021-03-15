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

    public final static String INITIAL_SQL = "INITIAL_SQL";
    public final static String INPUT_STRUCTURE_NAMES = "INPUT_STRUCTURE_NAMES";
    public final static String INPUT_STRUCTURES = "INPUT_STRUCTURES";

    public final static String MOLECULE_MATERIAL_TYPE_ID = "MOLECULE_MATERIAL_TYPE_ID";
    public final static String OWNER_ID = "OWNER_ID";
    public final static String PROJECT_ID = "PROJECT_ID";

    public final static String TMP_MatId_MolId = "TMP_MatId_MolId";

    private final Connection connection;
    private final Map<String, SqlInsertBuilder> builderMap;
    private final Map<String, Integer> materialIndices;

    private JsonObject jsonConfig;

    private InhouseDB(String configFileName) throws Exception {
        readConfig(configFileName);
        this.connection = DriverManager.getConnection(getConfigString(DATABASE_URL));
        runInitial();
        this.builderMap = new HashMap<> ();
        this.materialIndices = new HashMap<> ();
        addInsertBuilders();
        addMaterialIndices();
    }
    
    private void addInsertBuilders() {
        this.builderMap.put(MaterialEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MaterialEntity.class)));
        this.builderMap.put(StructureEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(StructureEntity.class)));
        this.builderMap.put(MoleculeEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MoleculeEntity.class)));
        this.builderMap.put(MaterialIndexEntryEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MaterialIndexEntryEntity.class)));
    }

    private void addMaterialIndices() throws Exception {
        PreparedStatement stmnt = this.connection.prepareStatement("SELECT id, name FROM indextypes");
        ResultSet result = stmnt.executeQuery();
        while(result.next()) {
            this.materialIndices.put(result.getString(2), result.getInt(1));
        }
        this.materialIndices.put("CAS-RN", this.materialIndices.get("CAS/RN"));
    }
    
    /**
     *
     * @param cdkMolecule
     * @return
     */
    private boolean cleanMolecule(IAtomContainer cdkMolecule) throws CDKException {
        List<IAtom> atomsToRemove = new ArrayList<> ();
        int nAtoms = 0;
        for (IAtom atom : cdkMolecule.atoms()) {
            nAtoms++;
            if (atom.getAtomicNumber().equals(0)) {
                atomsToRemove.add(atom);
            }
        }
        for(IAtom atom : atomsToRemove) {
            cdkMolecule.removeAtom(atom);
            nAtoms--;
        }
        return (nAtoms > 0);
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

        IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();

        IteratingSDFReader sdfReader = new IteratingSDFReader(
                new FileInputStream(fileName), builder);

        while (sdfReader.hasNext()) {
            IAtomContainer cdkMolecule = sdfReader.next();

            IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(cdkMolecule);

            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkMolecule);
            CDKHydrogenAdder.getInstance(builder).addImplicitHydrogens(cdkMolecule);
            Kekulization.kekulize(cdkMolecule);

            String molFormula = MolecularFormulaManipulator.getString(cdkFormula);
            double molMass = MolecularFormulaManipulator.getMass(cdkFormula,
                    MolecularFormulaManipulator.MolWeight);
            double exactMass = MolecularFormulaManipulator.getMass(cdkFormula,
                    MolecularFormulaManipulator.MonoIsotopic);
            importSingleCompound(cdkMolecule, molFormula, molMass, exactMass);
        }
    }

    private void importCompoundNames(String fileName) throws Exception {
        RTF rtf = new RTF(this.connection, this.builderMap, this.materialIndices);
        rtf.readCompoundSynonym(fileName);

    }

    private void importData() throws Exception {
        // importCompounds(getConfigString(INPUT_STRUCTURES));
        importCompoundNames(getConfigString(INPUT_STRUCTURE_NAMES));
    }

    private void importSingleCompound(IAtomContainer cdkMolecule,
            String molFormula,
            Double molMass,
            Double exactMass) throws Exception {
        MaterialEntity mat = new MaterialEntity();
        mat.setACList(getConfigInt(ACLIST_ID));
        mat.setCtime(new Date());
        mat.setMaterialtypeid(getConfigInt(MOLECULE_MATERIAL_TYPE_ID));
        mat.setOwner(getConfigInt(OWNER_ID));
        mat.setProjectid(getConfigInt(PROJECT_ID));
        
        mat = (MaterialEntity) this.builderMap
                .get(mat.getClass().getName())
                .insert(connection, mat);

        if (cleanMolecule(cdkMolecule)) {
            MoleculeEntity mol = new MoleculeEntity();
            saveMolString(mol, cdkMolecule);
            mol = (MoleculeEntity) this.builderMap
                    .get(mol.getClass().getName())
                    .insert(connection, mol);

            StructureEntity struc = new StructureEntity();
            struc.setId(mat.getMaterialid());
            struc.setMolarmass(molMass);
            struc.setExactmolarmass(exactMass);
            struc.setSumformula(molFormula);
            struc.setMoleculeid(mol.getId());
            this.builderMap
                    .get(struc.getClass().getName())
                    .insert(connection, struc);
        }
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

    private void saveMolProperties(IAtomContainer cdkMolecule, Integer id)
            throws Exception {
        String sql = "INSERT INTO material_indices (materialid, typeid, value) VALUES (?,?,?)";
        String sql2 = "INSERT INTO tmp_import (old_id, new_id, type) VALUES (?, ?, ?)";
        for( Map.Entry<String, Integer> entry : this.materialIndices.entrySet()) {
            String key = entry.getKey();
            String propValue = cdkMolecule.getProperty(key);
            if (propValue != null) {
                saveTriple(sql, id, entry.getValue(), propValue);
                if (key.equals("Mol_ID")) {
                    saveTriple(sql2, Integer.valueOf(propValue), id, TMP_MatId_MolId);
                }
            }
        }
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

    private void saveTriple(String sql, Integer id, Integer other, String value) throws SQLException {
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
