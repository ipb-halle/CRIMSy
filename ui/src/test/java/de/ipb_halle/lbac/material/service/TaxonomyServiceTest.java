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
package de.ipb_halle.lbac.material.service;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.component.HazardInformation;
import de.ipb_halle.lbac.material.component.MaterialName;
import de.ipb_halle.lbac.material.component.StorageClassInformation;
import de.ipb_halle.lbac.material.subtype.Taxonomy;
import de.ipb_halle.lbac.material.subtype.TaxonomyLevel;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.ArrayList;
import java.util.List;
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
public class TaxonomyServiceTest extends TestBase {

    @Inject
    private TaxonomyService service;
    
    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    private CreationTools creationTools;

    @Before
    public void init() {

    }

   

    @Test
    public void test001_loadTaxonomyLevels() {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        Assert.assertEquals("test001: 8 levels must be found", 8, levels.size());
    }

    
     @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TaxonomyServiceTest.war")
                .addClass(ProjectService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyService.class);
    }
}
