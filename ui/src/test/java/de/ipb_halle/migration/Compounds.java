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
 *
 * @author fbroda
 */
public class Compounds {
    
    public final static String INPUT_STRUCTURE_NAMES = "INPUT_STRUCTURE_NAMES";
    public final static String INPUT_STRUCTURES = "INPUT_STRUCTURES";

    public final static String MOLECULE_MATERIAL_TYPE_ID = "MOLECULE_MATERIAL_TYPE_ID";

    public final static String TMP_MatId_MolId = "TMP_MatId_MolId";

    private InhouseDB inhouseDB;

    public Compounds(InhouseDB inhouseDB) throws Exception {
        this.inhouseDB = inhouseDB;
        addInsertBuilders();
    }
    
    private void addInsertBuilders() {
        this.inhouseDB.addInsertBuilder(MaterialEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MaterialEntity.class)));
        this.inhouseDB.addInsertBuilder(MaterialIndexEntryEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MaterialIndexEntryEntity.class)));
        this.inhouseDB.addInsertBuilder(StructureEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(StructureEntity.class)));
        this.inhouseDB.addInsertBuilder(MoleculeEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(MoleculeEntity.class)));
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

    private void importCompounds(String fileName) throws Exception {
        System.out.println("Importing compounds");

        IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();

        IteratingSDFReader sdfReader = new IteratingSDFReader(
                new FileInputStream(fileName), builder);

        int counter = 0;
        while (sdfReader.hasNext()) {
            try {
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
                counter++;
                if ((counter % 250) == 0) {
                    System.out.printf("imported %d structures\n", counter);
                }
            } catch(Exception e) {
                System.out.printf("WARN: Import failed for structure %d\n", counter);
                e.printStackTrace();
                counter++;
            }
        }
    }

    private void importCompoundNames(String fileName) throws Exception {
        RTF rtf = new RTF(this.inhouseDB);
        rtf.readCompoundSynonym(fileName);

    }

    public void importData() throws Exception {
        importCompounds(inhouseDB.getConfigString(INPUT_STRUCTURES));
        importCompoundNames(inhouseDB.getConfigString(INPUT_STRUCTURE_NAMES));
    }

    private void importSingleCompound(IAtomContainer cdkMolecule,
            String molFormula,
            Double molMass,
            Double exactMass) throws Exception {
        MaterialEntity mat = new MaterialEntity();
        mat.setACList(inhouseDB.getACList());
        mat.setCtime(new Date());
        mat.setMaterialtypeid(inhouseDB.getConfigInt(MOLECULE_MATERIAL_TYPE_ID));
        mat.setOwner(inhouseDB.getOwner());
        mat.setProjectid(inhouseDB.getProject()); 
        
        mat = (MaterialEntity) this.inhouseDB.getBuilder(mat.getClass().getName())
                .insert(this.inhouseDB.getConnection(), mat);

        StructureEntity struc = new StructureEntity();
        if (cleanMolecule(cdkMolecule)) {
            MoleculeEntity mol = new MoleculeEntity();
            saveMolString(mol, cdkMolecule);
            mol = (MoleculeEntity) this.inhouseDB.getBuilder(mol.getClass().getName())
                    .insert(this.inhouseDB.getConnection(), mol);

            struc.setMolarmass(molMass);
            struc.setExactmolarmass(exactMass);
            struc.setSumformula(molFormula);
            struc.setMoleculeid(mol.getId());
        }
        struc.setId(mat.getMaterialid());
        this.inhouseDB.getBuilder(struc.getClass().getName())
                .insert(this.inhouseDB.getConnection(), struc);
        saveMolProperties(cdkMolecule, mat.getMaterialid());
    }
    
    private void saveMolProperties(IAtomContainer cdkMolecule, Integer id)
            throws Exception {
        String sql = "INSERT INTO material_indices (materialid, typeid, value) VALUES (?,?,?)";
        String sql2 = "INSERT INTO tmp_import (old_id, new_id, type) VALUES (?, ?, ?)";
        for( Map.Entry<String, Integer> entry : this.inhouseDB.getMaterialIndexTypes().entrySet()) {
            String key = entry.getKey();
            String propValue = cdkMolecule.getProperty(key);
            if (propValue != null) {
                inhouseDB.saveTriple(sql, id, entry.getValue(), propValue);
                if (key.equals("Mol_ID")) {
                    inhouseDB.saveTriple(sql2, Integer.valueOf(propValue), id, TMP_MatId_MolId);
                }
            }
        }
    }

    private void saveMolString(MoleculeEntity mol, IAtomContainer cdkMolecule) throws CDKException, IOException {
        ByteArrayOutputStream molStream = new ByteArrayOutputStream();
        IChemObjectWriter cdkWriter;
        if (cdkMolecule.getAtomCount() > 900) {
            cdkWriter = new MDLV3000Writer(molStream);
        } else {
            cdkWriter = new MDLV2000Writer(molStream);
        }
        cdkWriter.write(cdkMolecule);
        cdkWriter.close();
        mol.setMolecule(molStream.toString());
    }
}
