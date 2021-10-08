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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.StorageInformationBuilder;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.mocks.ProjectBeanMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class HistoryOperationIndexTest {
    
    List<IndexEntry> indices;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialIndexDifference mid;
    MaterialIndexBean mib;
    Random random = new Random();
    
    @Inject
    private MaterialService materialService;
    
    @Before
    public void init() {
        indices = new ArrayList<>();
        s = new Structure("H2O", 0d, 0d, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);
        mes.setCurrentVersiondate(currentDate);
        mib = new MaterialIndexBean();
        s.setIndices(indices);
        mid = new MaterialIndexDifference();
        mid.initialise(0, random.nextInt(100000), currentDate);
        
        MaterialBeanMock mock = new MaterialBeanMock();
        mock.setMaterialIndexBean(mib);
        mock.setMaterialEditState(mes);
        mock.setMaterialNameBean(new MaterialNameBean());
        instance = new HistoryOperation(mock);
    }
    
    @Test
    public void test01_indexDifferenceOperations() {

        //################
        //Testcase 1: add a new index of type 2 
        mib.getIndices().add(new IndexEntry(2, "A38", null));
        
        mid.getLanguageNew().add(null);
        mid.getLanguageOld().add(null);
        mid.getValuesNew().add("A38");
        mid.getValuesOld().add(null);
        mid.getRankNew().add(0);
        mid.getRankOld().add(0);
        mid.getTypeId().add(2);
        
        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();
        
        List<IndexEntry> resultIndices = mib.getIndices();
        Assert.assertTrue("Testcase 1 - no index must be found", resultIndices.isEmpty());
    }
    
    @Test
    public void test02_indexDifferenceOperations() {
        //################
        //Testcase 2: remove a new index of type 2 
        mib.getIndices().clear();
        
        mid.getLanguageNew().add(null);
        mid.getLanguageOld().add(null);
        mid.getValuesNew().add(null);
        mid.getValuesOld().add("A38");
        mid.getRankNew().add(0);
        mid.getRankOld().add(0);
        mid.getTypeId().add(2);
        
        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();
        
        List<IndexEntry> resultIndices = mib.getIndices();
        
        Assert.assertEquals("Testcase 2 - 1 index must be found", 1, resultIndices.size());
        Assert.assertEquals("Testcase 2 - Type of index must be 2", 2, resultIndices.get(0).getTypeId());
        Assert.assertEquals("Testcase 2 - Value of index must be A38", "A38", resultIndices.get(0).getValue());
        
    }
    
    @Test
    public void test03_indexDifferenceOperations() {
        //################
        //Testcase 3: change the value of an index
        mib.getIndices().add(new IndexEntry(2, "B38", null));
        
        mid.getLanguageNew().add(null);
        mid.getLanguageOld().add(null);
        mid.getValuesNew().add("B38");
        mid.getValuesOld().add("A38");
        mid.getRankNew().add(0);
        mid.getRankOld().add(0);
        mid.getTypeId().add(2);
        
        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();
        
        List<IndexEntry> resultIndices = mib.getIndices();
        
        Assert.assertEquals("Testcase 3 - 1 index must be found", 1, resultIndices.size());
        Assert.assertEquals("Testcase 3 - Type of index must be 2", 2, resultIndices.get(0).getTypeId());
        Assert.assertEquals("Testcase 3 - Value of index must be A38", "A38", resultIndices.get(0).getValue());
        
    }
    
    @Test
    public void test04_indexDifferenceOperations() {
        //################
        //Testcase 4: switch value and type index
        mib.getIndices().add(new IndexEntry(2, "B38", null));
        
        mid.getLanguageNew().add(null);
        mid.getLanguageOld().add(null);
        mid.getValuesNew().add("B38");
        mid.getValuesOld().add(null);
        mid.getRankNew().add(0);
        mid.getRankOld().add(0);
        mid.getTypeId().add(2);
        
        mid.getLanguageNew().add(null);
        mid.getLanguageOld().add(null);
        mid.getValuesNew().add(null);
        mid.getValuesOld().add("A38");
        mid.getRankNew().add(0);
        mid.getRankOld().add(0);
        mid.getTypeId().add(3);
        
        s.getHistory().addDifference(mid);
        instance.applyNextNegativeDifference();
        
        List<IndexEntry> resultIndices = mib.getIndices();
        
        Assert.assertEquals("Testcase 4 - 1 index must be found", 1, resultIndices.size());
        Assert.assertEquals("Testcase 4 - Type of index must be 2", 3, resultIndices.get(0).getTypeId());
        Assert.assertEquals("Testcase 4 - Value of index must be A38", "A38", resultIndices.get(0).getValue());
        
    }
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationIndexTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
    
}
