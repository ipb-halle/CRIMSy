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
import de.ipb_halle.lbac.material.biomaterial.TaxonomyEntity;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlInsertBuilder;

import java.io.ByteArrayOutputStream;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Migration tool for the InhouseDB
 *
 * @author fbroda
 */
public class Taxonomy {
    
    public final static String INPUT_TAXONOMY_CLASSES = "INPUT_TAXONOMY_CLASSES";
    public final static String INPUT_TAXONOMY_FAMILIES = "INPUT_TAXONOMY_FAMILIES";
    public final static String INPUT_TAXONOMY_SPECIES = "INPUT_TAXONOMY_SPECIES";
    public final static String INPUT_TAXONOMY_SPECIES_SYNONYMS = "INPUT_TAXONOMY_SPECIES_SYNONYMS";
    public final static String INPUT_TAXONOMY_STRAINS = "INPUT_TAXONOMY_STRAINS";
    public final static String INPUT_ORGANISM_ID = "INPUT_ORGANISM_ID";

    public final static String INPUT_TAXONOMY_CLASS_LEVEL = "INPUT_TAXONOMY_CLASS_LEVEL";
    public final static String INPUT_TAXONOMY_FAMILY_LEVEL = "INPUT_TAXONOMY_FAMILY_LEVEL";
    public final static String INPUT_TAXONOMY_SPECIES_LEVEL = "INPUT_TAXONOMY_SPECIES_LEVEL";
    public final static String INPUT_TAXONOMY_STRAIN_LEVEL = "INPUT_TAXONOMY_STRAIN_LEVEL";

    public final static String TAXONOMY_NAME_DEFAULT_LANG = "la";
    public final static String TAXONOMY_MATERIAL_TYPE_ID = "TAXONOMY_MATERIAL_TYPE_ID";

    /* reference key in tmp_import */
    public final static String ORGANISM_ID_REF = "organismId";
    public final static String TAXONOMY_CLASS_REF = "class";
    public final static String TAXONOMY_FAMILY_REF = "family";
    public final static String TAXONOMY_SPECIES_REF = "species";
    public final static String TAXONOMY_STRAIN_REF = "strain";

    public final static String UNKNOWN_TAXONOMY_PARENT_ID = "UNKNOWN_TAXONOMY_PARENT_ID";

    private InhouseDB inhouseDB;
    private int unknownParentId;       // link to this id, if no reference is given

    public Taxonomy(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        addInsertBuilders();
        this.unknownParentId = inhouseDB.getConfigInt(UNKNOWN_TAXONOMY_PARENT_ID);
    }
    
    private void addInsertBuilders() {
        this.inhouseDB.addInsertBuilder(TaxonomyEntity.class.getName(),
                new SqlInsertBuilder(new EntityGraph(TaxonomyEntity.class)));
    }

    public void importData() throws Exception {
        importTaxonomyClasses(this.inhouseDB.getConfigString(INPUT_TAXONOMY_CLASSES)); 
        importTaxonomyFamilies(this.inhouseDB.getConfigString(INPUT_TAXONOMY_FAMILIES));
        importTaxonomySpecies(this.inhouseDB.getConfigString(INPUT_TAXONOMY_SPECIES),
            importTaxonomySpeciesSynonyms(this.inhouseDB.getConfigString(INPUT_TAXONOMY_SPECIES_SYNONYMS)));
        importTaxonomyStrains(this.inhouseDB.getConfigString(INPUT_TAXONOMY_STRAINS));
        importOrganismIds(this.inhouseDB.getConfigString(INPUT_ORGANISM_ID));
    }

    private void importOrganismIds(String fileName) throws Exception {
        System.out.println("Importing organism Ids");
        String pattern = "^(\\d+);(\\d+);(\\d*);(.*)$";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        reader.readLine(); // discard 2nd line too
        while(reader.ready()) {
                String st = reader.readLine();
                int orgId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int speciesId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                String strain = st.replaceAll(pattern, "$3");
                int strainId = strain.isEmpty() ? 0 : Integer.parseInt(strain);
                String remarks = st.replaceAll(pattern, "$4");

                saveOrganism(orgId,  speciesId, strainId, remarks);
        }
        reader.close();
    }

    private void importTaxonomyClasses(String fileName) throws Exception {
        System.out.println("Importing taxonomic classes");
        String pattern = "^([0-9]+);'(.*)';([0-9]+)$";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
                String st = reader.readLine(); 
                int oldId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                String name = st.replaceAll(pattern, "$2");
                int parentId = Integer.parseInt(st.replaceAll(pattern, "$3"));

                save(name, 
                    this.inhouseDB.getConfigInt(INPUT_TAXONOMY_CLASS_LEVEL),
                    parentId,
                    oldId,
                    TAXONOMY_CLASS_REF);
        }
        reader.close();
    }

    private void importTaxonomyFamilies(String fileName) throws Exception {
        System.out.println("Importing taxonomic families");
        String sql = "SELECT new_id FROM tmp_import WHERE old_id=? AND type=?";
        String pattern = "^([0-9]+);([0-9]+);'(.*)'$";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
                String st = reader.readLine(); 
                int oldId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int origParentId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                String name = st.replaceAll(pattern, "$3");

                int parentId = this.inhouseDB.loadRefId(sql, origParentId, TAXONOMY_CLASS_REF);

                save(name,
                    this.inhouseDB.getConfigInt(INPUT_TAXONOMY_FAMILY_LEVEL),
                    ((parentId != 0) ? parentId : unknownParentId),
                    oldId,
                    TAXONOMY_FAMILY_REF);
        }
        reader.close();
    }

    private void importTaxonomySpecies(String fileName, Map<Integer, Set<String>> allSynonyms) throws Exception {
        System.out.println("Importing taxonomic species");
        String sql = "SELECT new_id FROM tmp_import WHERE old_id=? AND type=?";
        String pattern = "^([0-9]+);([0-9]+)$";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
                String st = reader.readLine();
                int oldId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int origParentId = Integer.parseInt(st.replaceAll(pattern, "$2"));

                int parentId = this.inhouseDB.loadRefId(sql, origParentId, TAXONOMY_FAMILY_REF);

                int id = 0; 
                int rank = 2;
                Set<String> names = allSynonyms.get(Integer.valueOf(oldId));
                if ( names == null) {
                    names = new HashSet<> ();
                    names.add("unknown");
                }
                for (String name : names) {
                    if (id == 0) {
                        id = save(name,
                            this.inhouseDB.getConfigInt(INPUT_TAXONOMY_SPECIES_LEVEL),
                            ((parentId != 0) ? parentId : unknownParentId),
                            oldId,
                            TAXONOMY_SPECIES_REF);
                    } else {
                        saveName(name, id, rank);
                        rank++;
                    }
                }
        }
        reader.close();
    }

    private Map<Integer, Set<String>> importTaxonomySpeciesSynonyms(String fileName)  throws Exception {
        System.out.println("Reading species synonyms");
        Map<Integer, Set<String>> allSynonyms = new HashMap<> ();
        String pattern = "^([0-9]+);([0-9]+);'(.*)';'[NYny]'$";

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
            String st = reader.readLine();
            Integer speciesId = Integer.valueOf(st.replaceAll(pattern, "$2"));
            String synonym = st.replaceAll(pattern, "$3");

            Set<String> synonyms = allSynonyms.get(speciesId);
            if (synonyms == null) {
                synonyms = new HashSet<> ();
                allSynonyms.put(speciesId, synonyms);
            }
            synonyms.add(synonym);
        }
        reader.close();
        return allSynonyms;
    }

    private void importTaxonomyStrains(String fileName) throws Exception {
        System.out.println("Importing strains");
        String sql = "SELECT new_id FROM tmp_import WHERE old_id=? AND type=?";
        String pattern = "^([0-9]+);([0-9]+);'(.*)'$";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header
        while(reader.ready()) {
                String st = reader.readLine();
                int oldId = Integer.parseInt(st.replaceAll(pattern, "$1"));
                int origParentId = Integer.parseInt(st.replaceAll(pattern, "$2"));
                String name = st.replaceAll(pattern, "$3");

                int parentId = this.inhouseDB.loadRefId(sql, origParentId, TAXONOMY_SPECIES_REF);

                save(name,
                    this.inhouseDB.getConfigInt(INPUT_TAXONOMY_STRAIN_LEVEL),
                    ((parentId != 0) ? parentId : unknownParentId),
                    oldId,
                    TAXONOMY_STRAIN_REF);
        }
        reader.close();
    }

    /*
     * need to fill the following tables:
     * <ul>
     *  <li>materials</li>
     *  <li>material_indices</li>
     *  <li>taxonomy</li>
     *  <li>effective_taxonomy</li>
     *  <li>tmp_import for correlation among entries</li>
     * </ul>
     * @param name the taxonomic name
     * @param level the level (class, family, species, strain)
     * @param parentId the id of the parent taxonomic entity
     * @param origId the original id from the imported DB
     * @param refKey the reference key in the tmp_import table to correlate the new id with the 
     * old id ("class", "family", "species", "strain")
     * 
     */
    private int save(String name, int level, int parentId, int origId, String refKey) throws Exception {

        MaterialEntity mat = new MaterialEntity();
        mat.setACList(this.inhouseDB.getACList()); 
        mat.setCtime(new Date());
        mat.setMaterialtypeid(this.inhouseDB.getConfigInt(TAXONOMY_MATERIAL_TYPE_ID));
        mat.setOwner(this.inhouseDB.getOwner()); 
        mat.setProjectid(this.inhouseDB.getProject()); 

        mat = (MaterialEntity) this.inhouseDB.getBuilder(mat.getClass().getName())
                .insert(this.inhouseDB.getConnection(), mat);

        saveName(name, mat.getMaterialid(), 0);
        saveTaxonomy(mat.getMaterialid(), level);
        saveEffectiveTaxonomy(mat.getMaterialid(), parentId);
        saveCrossRef(mat.getMaterialid(), origId, refKey);
        return mat.getMaterialid();
    }

    private void saveCrossRef(int id, int origId, String refKey) throws Exception {
        String sql = "INSERT INTO tmp_import (old_id, new_id, type) VALUES (?, ?, ?)";
        this.inhouseDB.saveTriple(sql, origId, id, refKey);
    }

    private void saveEffectiveTaxonomy(int id, int parentId) throws Exception {
        String sql = "INSERT INTO effective_taxonomy (taxoid, parentid) SELECT ? AS taxoid, parentid FROM effective_taxonomy WHERE taxoid=?";
        PreparedStatement statement = this.inhouseDB.getConnection().prepareStatement(sql);
        statement.setInt(1, id);
        statement.setInt(2, parentId);
        statement.execute();

        sql = "INSERT INTO effective_taxonomy (taxoid, parentid) VALUES (?, ?)";
        statement = this.inhouseDB.getConnection().prepareStatement(sql);
        statement.setInt(1, id);
        statement.setInt(2, parentId);
        statement.execute();
    }

    private void saveOrganism(int orgId, int speciesId, int strainId, String remarks) throws Exception {
        String sql = "INSERT INTO tmp_import (old_id, new_id, type) SELECT ? AS old_id, new_id, 'organismId' AS type FROM tmp_import WHERE old_id=? AND type=?";
        if (strainId > 0) {
            this.inhouseDB.saveTriple(sql, orgId, strainId, TAXONOMY_STRAIN_REF);
        } else {
            this.inhouseDB.saveTriple(sql, orgId, speciesId, TAXONOMY_SPECIES_REF);
        }
    }

    private void saveName(String name, int id, int rank) throws Exception {
        MaterialIndexEntryEntity idx = new MaterialIndexEntryEntity();
        idx.setLanguage(TAXONOMY_NAME_DEFAULT_LANG);
        idx.setMaterialid(id);
        idx.setRank(rank);
        idx.setValue(name);
        idx.setTypeid(this.inhouseDB.getMaterialIndexType(InhouseDB.MATERIAL_INDEX_NAME));

        this.inhouseDB.getBuilder(idx.getClass().getName())
            .insert(this.inhouseDB.getConnection(), idx);
    }

    private void saveTaxonomy(int id, int level) throws Exception {
        TaxonomyEntity tax = new TaxonomyEntity();
        tax.setId(id);
        tax.setLevel(level);
        this.inhouseDB.getBuilder(tax.getClass().getName())
            .insert(this.inhouseDB.getConnection(), tax);
    }
}
