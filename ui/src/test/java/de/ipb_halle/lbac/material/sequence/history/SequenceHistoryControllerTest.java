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
package de.ipb_halle.lbac.material.sequence.history;

import de.ipb_halle.lbac.material.common.bean.history.*;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;
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
public class SequenceHistoryControllerTest extends HistoryOperationTest {

    private static final long serialVersionUID = 1L;

    @Before
    @Override
    public void init() {
        super.init();
        SequenceData data = SequenceData.builder()
                .circular(true)
                .annotations("MyAnnotation")
                .sequenceString("AAA")
                .sequenceType(SequenceType.DNA).build();
        materialBeanMock.getSequenceInfos().setSequenceData(data);
    }

    /**
     * {empty} -> {DNA,!circular,'TTT', no annotations} -> {DNA,circular,'AAA',
     * annotation:myAnnotation}
     */
    @Test
    public void test01_checkHistoryOperation() {
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

    @Override
    protected void checkCurrentState() {
        Assert.assertEquals(SequenceType.DNA, materialBeanMock.getSequenceInfos().getSequenceType());
        Assert.assertEquals("MyAnnotation", materialBeanMock.getSequenceInfos().getSequenceData().getAnnotations());
        Assert.assertTrue(materialBeanMock.getSequenceInfos().getSequenceData().isCircular());
        Assert.assertEquals("AAA", materialBeanMock.getSequenceInfos().getSequenceData().getSequenceString());
    }

    @Override
    protected void checkStateAt20001220() {
        Assert.assertEquals(SequenceType.DNA, materialBeanMock.getSequenceInfos().getSequenceType());
        Assert.assertNull(materialBeanMock.getSequenceInfos().getSequenceData().getAnnotations());
        Assert.assertFalse(materialBeanMock.getSequenceInfos().getSequenceData().isCircular());
        Assert.assertEquals("TTT", materialBeanMock.getSequenceInfos().getSequenceData().getSequenceString());
    }

    @Override
    protected void checkStateAt20001020() {
        Assert.assertEquals(SequenceType.DNA, materialBeanMock.getSequenceInfos().getSequenceType());
        Assert.assertNull(materialBeanMock.getSequenceInfos().getSequenceData().getAnnotations());
        Assert.assertNull(materialBeanMock.getSequenceInfos().getSequenceData().isCircular());
        Assert.assertNull(materialBeanMock.getSequenceInfos().getSequenceData().getSequenceString());
    }

    @Override
    protected SequenceDifference createDiffAt20001020() {
        SequenceDifference diff = new SequenceDifference();

        SequenceData newData = SequenceData.builder()
                .circular(false)
                .sequenceString("TTT").build();
        diff.setNewSequenceData(newData);

        SequenceData oldData = SequenceData.builder()
                .build();
        diff.setOldSequenceData(oldData);

        diff.initialise(0, publicUser.getId(), d_20001020);

        return diff;
    }

    @Override
    protected MaterialDifference createDiffAt20001220() {
        SequenceDifference diff = new SequenceDifference();
        SequenceData newData = SequenceData.builder()
                .circular(true)
                .annotations("MyAnnotation")
                .sequenceString("AAA").build();
        diff.setNewSequenceData(newData);
        SequenceData oldData = SequenceData.builder()
                .circular(false)
                .sequenceString("TTT").build();
        diff.setOldSequenceData(oldData);
        diff.initialise(0, publicUser.getId(), d_20001220);

        return diff;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SequenceHistoryControllerTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);

        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
