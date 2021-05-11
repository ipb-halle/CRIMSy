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
package de.ipb_halle.lbac.material.common.bean.history;

import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.mocks.ProjectBeanMock;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class HistoryOperationNameTest {

    List<MaterialName> names;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialIndexDifference mid;
    MaterialNameBean mnb;
        Random random = new Random();

    @Before
    public void init() {
        names = new ArrayList<>();
        s = new Structure("H2O", 0d, 0d, 0, names, 0, new HazardInformation(), new StorageClassInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);
        mes.setCurrentVersiondate(currentDate);
        mnb = new MaterialNameBean();
        mnb.setNames(names);
        mid = new MaterialIndexDifference();
        mid.initialise(0, random.nextInt(100000), currentDate);
        instance = new HistoryOperation(mes, new ProjectBeanMock(), mnb, null, new StructureInformation(),new StorageClassInformation(),null,new ArrayList<>());
    }

    @Test
    public void test01_nameDifferenceOperations() {

        //################
        //Testcase 1: [B,en,0],[C,de,1] --> [A,de,0]
        names.add(new MaterialName("B", "en", 0));
        names.add(new MaterialName("C", "de", 1));
        mid.getLanguageNew().add("en");
        mid.getLanguageNew().add("de");
        mid.getValuesNew().add("B");
        mid.getValuesNew().add("C");
        mid.getRankNew().add(0);
        mid.getRankNew().add(1);
        mid.getValuesOld().add("A");
        mid.getValuesOld().add(null);
        mid.getRankOld().add(0);
        mid.getRankOld().add(null);
        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add(null);
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 1 - only 1 name must exist", 1, resultNames.size());
        Assert.assertEquals("Testcase 1 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 1 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
    }

    @Test
    public void test02_nameDifferenceOperations() {

        //################
        //Testcase 2: [B,en,0],[A,de,1] --> [A,de,0]
        mnb.getNames().add(new MaterialName("B", "en", 0));
        mnb.getNames().add(new MaterialName("A", "de", 1));

        mid.getLanguageNew().add("en");
        mid.getLanguageNew().add("de");
        mid.getValuesNew().add("B");
        mid.getValuesNew().add("A");
        mid.getRankNew().add(0);
        mid.getRankNew().add(1);
        mid.getValuesOld().add("A");
        mid.getValuesOld().add(null);
        mid.getRankOld().add(0);
        mid.getRankOld().add(null);
        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add(null);
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 2- only 1 name must exist", 1, resultNames.size());
        Assert.assertEquals("Testcase 2 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 2 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 2 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());

    }

    @Test
    public void test03_nameDifferenceOperations() {
        //################
        //Testcase 3: [A,de,0] --> [A,de,0]
        mnb.getNames().add(new MaterialName("A", "de", 0));
        mid.getLanguageNew().add("de");
        mid.getValuesNew().add("A");
        mid.getRankNew().add(0);
        mid.getValuesOld().add("A");
        mid.getRankOld().add(0);
        mid.getLanguageOld().add("de");
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 3- only 1 name must exist", 1, resultNames.size());
        Assert.assertEquals("Testcase 3 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 3 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 3 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
    }

    @Test
    public void test04_nameDifferenceOperations() {
        //################
        //Testcase 4: [A,de,0],[B,en,1] --> [A,de,0]
        mnb.getNames().add(new MaterialName("A", "de", 0));
        mnb.getNames().add(new MaterialName("B", "en", 1));
        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add("en");
        mid.getValuesNew().add("A");
        mid.getValuesNew().add("B");
        mid.getRankNew().add(0);
        mid.getRankNew().add(1);
        mid.getValuesOld().add("A");
        mid.getValuesOld().add(null);
        mid.getRankOld().add(0);
        mid.getRankOld().add(null);
        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add(null);

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 4- only 1 name must exist", 1, resultNames.size());
        Assert.assertEquals("Testcase 4 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 4 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 4 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
    }

    @Test
    public void test05_nameDifferenceOperations() {
        //################
        //Testcase 6: [A,de,0],[B,en,1],[C,de,2] --> [B,en,0],[A,de,1]
        mnb.getNames().add(new MaterialName("A", "de", 0));
        mnb.getNames().add(new MaterialName("B", "en", 1));
        mnb.getNames().add(new MaterialName("C", "de", 2));

        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add("en");
        mid.getLanguageNew().add("de");
        mid.getValuesNew().add("A");
        mid.getValuesNew().add("B");
        mid.getValuesNew().add("C");
        mid.getRankNew().add(0);
        mid.getRankNew().add(1);
        mid.getRankNew().add(2);
        mid.getValuesOld().add("B");
        mid.getValuesOld().add("A");
        mid.getValuesOld().add(null);
        mid.getRankOld().add(0);
        mid.getRankOld().add(1);
        mid.getRankOld().add(null);
        mid.getLanguageOld().add("en");
        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add(null);

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 5- only 2 names must exist", 2, resultNames.size());
        Assert.assertEquals("Testcase 5.1 - Language must be en", "en", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 5.1 - Value must be B", "B", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 5.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 5.2 - Language must be de", "de", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 5.2 - Value must be A", "A", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 5.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
    }

    @Test
    public void test06_nameDifferenceOperations() {
        //################
        //Testcase 6: [A,de,0],[B,de,1] --> [C,en,0],[D,de,1]
        mnb.getNames().add(new MaterialName("A", "de", 0));
        mnb.getNames().add(new MaterialName("B", "en", 1));

        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add("en");

        mid.getValuesNew().add("A");
        mid.getValuesNew().add("B");

        mid.getRankNew().add(0);
        mid.getRankNew().add(1);

        mid.getValuesOld().add("C");
        mid.getValuesOld().add("D");

        mid.getRankOld().add(0);
        mid.getRankOld().add(1);

        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add("en");

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 6- only 2 names must exist", 2, resultNames.size());
        Assert.assertEquals("Testcase 6.1 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 6.1 - Value must be C", "C", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 6.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 6.2 - Language must be en", "en", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 6.2 - Value must be D", "D", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 6.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
    }

    @Test
    public void test07_nameDifferenceOperations() {
        //################
        //Testcase 7: [C,de,0],[A,de,1] --> [A,de,0],[C,de,1]
        mnb.getNames().add(new MaterialName("C", "de", 0));
        mnb.getNames().add(new MaterialName("A", "de", 1));

        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add("de");

        mid.getValuesNew().add("A");
        mid.getValuesNew().add("C");

        mid.getRankNew().add(1);
        mid.getRankNew().add(0);

        mid.getValuesOld().add("C");
        mid.getValuesOld().add("A");

        mid.getRankOld().add(1);
        mid.getRankOld().add(0);

        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add("de");

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 7- only 2 names must exist", 2, resultNames.size());
        Assert.assertEquals("Testcase 7.1 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 7.1 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 7.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 7.2 - Language must be de", "de", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 7.2 - Value must be C", "C", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 7.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
    }

    @Test
    public void test08_nameDifferenceOperations() {
        //################
        //Testcase 8: [D,en,0] --> [A,de,0],[B,en,1],[C,de,2]
        mnb.getNames().add(new MaterialName("D", "en", 0));

        mid.getLanguageNew().add("en");
        mid.getLanguageNew().add(null);
        mid.getLanguageNew().add(null);

        mid.getValuesNew().add("D");
        mid.getValuesNew().add(null);
        mid.getValuesNew().add(null);

        mid.getRankNew().add(0);
        mid.getRankNew().add(null);
        mid.getRankNew().add(null);

        mid.getValuesOld().add("A");
        mid.getValuesOld().add("B");
        mid.getValuesOld().add("C");

        mid.getRankOld().add(0);
        mid.getRankOld().add(1);
        mid.getRankOld().add(2);

        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add("en");
        mid.getLanguageOld().add("de");

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 8- only 3 names must exist", 3, resultNames.size());
        Assert.assertEquals("Testcase 8.1 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 8.1 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 8.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 8.2 - Language must be en", "en", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 8.2 - Value must be B", "B", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 8.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
        Assert.assertEquals("Testcase 8.3 - Language must be de", "de", resultNames.get(2).getLanguage());
        Assert.assertEquals("Testcase 8.3 - Value must be C", "C", resultNames.get(2).getValue());
        Assert.assertEquals("Testcase 8.3 - Rank must be 2", (long) 2, (long) resultNames.get(2).getRank());
    }

    @Test
    public void test09_nameDifferenceOperations() {
        //################
        //Testcase 9: [A,de,0] --> [B,en,0],[A,de,1]
        mnb.getNames().add(new MaterialName("A", "de", 0));

        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add(null);

        mid.getValuesNew().add("A");
        mid.getValuesNew().add(null);

        mid.getRankNew().add(0);
        mid.getRankNew().add(null);

        mid.getValuesOld().add("B");
        mid.getValuesOld().add("A");
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        mid.getRankOld().add(0);
        mid.getRankOld().add(1);

        mid.getLanguageOld().add("en");
        mid.getLanguageOld().add("de");

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 8- only 2 names must exist", 2, resultNames.size());
        Assert.assertEquals("Testcase 8.1 - Language must be en", "en", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 8.1 - Value must be B", "B", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 8.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 8.2 - Language must be de", "de", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 8.2 - Value must be A", "A", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 8.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
    }

    @Test
    public void test10_nameDifferenceOperations() {
        //################
        //Testcase 10: [C,de,0],[B,en,1] --> [A,de,0],[B,en,1],[C,de,2]
        mnb.getNames().add(new MaterialName("C", "de", 0));
        mnb.getNames().add(new MaterialName("B", "en", 1));

        mid.getLanguageNew().add("de");
        mid.getLanguageNew().add("en");
        mid.getLanguageNew().add(null);

        mid.getValuesNew().add("C");
        mid.getValuesNew().add("B");
        mid.getValuesNew().add(null);

        mid.getRankNew().add(0);
        mid.getRankNew().add(1);
        mid.getRankNew().add(null);

        mid.getValuesOld().add("A");
        mid.getValuesOld().add("B");
        mid.getValuesOld().add("C");

        mid.getRankOld().add(0);
        mid.getRankOld().add(1);
        mid.getRankOld().add(2);

        mid.getLanguageOld().add("de");
        mid.getLanguageOld().add("en");
        mid.getLanguageOld().add("de");

        mid.getTypeId().add(1);
        mid.getTypeId().add(1);
        mid.getTypeId().add(1);

        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();

        List<MaterialName> resultNames = mnb.getNames();
        Assert.assertEquals("Testcase 10- only 3 names must exist", 3, resultNames.size());
        Assert.assertEquals("Testcase 10.1 - Language must be de", "de", resultNames.get(0).getLanguage());
        Assert.assertEquals("Testcase 10.1 - Value must be A", "A", resultNames.get(0).getValue());
        Assert.assertEquals("Testcase 10.1 - Rank must be 0", (long) 0, (long) resultNames.get(0).getRank());
        Assert.assertEquals("Testcase 10.2 - Language must be en", "en", resultNames.get(1).getLanguage());
        Assert.assertEquals("Testcase 10.2 - Value must be B", "B", resultNames.get(1).getValue());
        Assert.assertEquals("Testcase 10.2 - Rank must be 1", (long) 1, (long) resultNames.get(1).getRank());
        Assert.assertEquals("Testcase 10.3 - Language must be de", "de", resultNames.get(2).getLanguage());
        Assert.assertEquals("Testcase 10.3 - Value must be C", "C", resultNames.get(2).getValue());
        Assert.assertEquals("Testcase 10.3 - Rank must be 2", (long) 2, (long) resultNames.get(2).getRank());

    }
}
