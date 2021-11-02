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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.BioMaterialDifference;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
public class HistoryOperationBiomaterialTest extends HistoryOperationTest {

    private static final long serialVersionUID = 1L;

    

    @Inject
    private TaxonomyService taxonomyService;
    @Inject
    private TissueService tissueService;
    

   

    /**
     * Description: The material is created without a Hazard. After that the
     * Hazard 'corrosive' is added and then at a later time the hazards
     * 'irritant' and 'unhealthy' are added and 'corrosive' removed, h and
     * p-statements are added.
     */
    @Test
    public void test01_BioMaterialDifferenceOperations() {
        createMaterialEditState();        
        checkCurrentState();
        
        //Go one step back (20.12.2000)
        instance.applyNextNegativeDifference();
        checkStateAt20001220();

        //Go one step back (20.10.2000)
        instance.applyNextNegativeDifference();
        checkStateAt20001020();
        //Go one step back (20.12.2000)
        instance.applyNextPositiveDifference();
        checkStateAt20001220();
        //Go one step back (now)
        instance.applyNextPositiveDifference();
        checkCurrentState();
    }

   

   
    private MaterialEditState createMaterialEditState() {
        mes = new MaterialEditState(
                new Project(),
                currentDate,
                biomaterial,
                biomaterial,
                new MaterialHazardBuilder(hazardService, MaterialType.BIOMATERIAL, true, new HashMap<>(), MessagePresenterMock.getInstance()));
        mes.setCurrentVersiondate(d_20001220);
        return mes;
    }

  

    @Override
    protected void checkCurrentState() {
        Taxonomy taxonomy = (Taxonomy) materialBeanMock.getTaxonomyController().getSelectedTaxonomy().getData();
        Assert.assertEquals(taxonomyService.loadRootTaxonomy().getId(), taxonomy.getId());

    }

    @Override
    protected void checkStateAt20001020() {
        Taxonomy taxonomy = (Taxonomy) materialBeanMock.getTaxonomyController().getSelectedTaxonomy().getData();
        Assert.assertEquals(mushroomsTaxonomy.getId(), taxonomy.getId());

    }

    @Override
    protected void checkStateAt20001220() {
        Taxonomy taxonomy = (Taxonomy) materialBeanMock.getTaxonomyController().getSelectedTaxonomy().getData();
        Assert.assertEquals(plantsTaxonomy.getId(), taxonomy.getId());
    }

    @Override
    protected BioMaterialDifference createDiffAt20001020() {
        BioMaterialDifference diff = new BioMaterialDifference();
        diff.initialise(0, publicUser.getId(), d_20001020);
        diff.addTaxonomyDiff(mushroomsTaxonomy.getId(), plantsTaxonomy.getId());
        return diff;
    }

    @Override
    protected MaterialDifference createDiffAt20001220() {
        BioMaterialDifference diff = new BioMaterialDifference();
        diff.addTaxonomyDiff(plantsTaxonomy.getId(), taxonomyService.loadRootTaxonomy().getId());
        diff.initialise(0, publicUser.getId(), d_20001220);
        return diff;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationBiomaterialTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
