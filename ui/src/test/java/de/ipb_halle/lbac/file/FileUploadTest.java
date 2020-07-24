/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.file;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrUpdate;
import de.ipb_halle.lbac.collections.CollectionBean;

import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.CollectionServiceMock;
import de.ipb_halle.lbac.file.mock.FileEntityServiceMock;
import de.ipb_halle.lbac.file.mock.FileServiceMock;
import de.ipb_halle.lbac.file.mock.SolrSearcherMock;
import de.ipb_halle.lbac.file.mock.SolrTermVectorSearchMock;
import de.ipb_halle.lbac.file.mock.SolrUpdateMock;
import de.ipb_halle.lbac.file.mock.TermVectorEntityServiceMock;
import de.ipb_halle.lbac.file.mock.TermVectorParserMock;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.servlet.AsyncContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class FileUploadTest extends TestBase {

    private FileUpload instance;

    @Test
    public void fileUploadTest() throws Exception {
        File f = new File(TEST_ROOT + "/exampledocs/WordCloud_Document1.pdf");
        AsyncContext asyncMock = new AsyncContextMock(f);
        CollectionService collServMock = new CollectionServiceMock();
        String directory = new File(TEST_ROOT).getAbsolutePath() + "/fileUpload";
        if (new File(directory).exists()) {
            Files.walk(Paths.get(directory))
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        }
        FileService fileServiceMock = new FileServiceMock(directory);
        SolrTermVectorSearch solrTVMock = new SolrTermVectorSearchMock();
        FileEntityService fileEntityServiceMock = new FileEntityServiceMock();
        SolrUpdate solrUpdate = new SolrUpdateMock();

        Collection coll1 = new Collection();
        coll1.setCountDocs(1L);
        coll1.setId(UUID.randomUUID());
        coll1.setName("Coll1");
        Collection coll2 = new Collection();
        coll2.setCountDocs(1L);
        coll2.setId(UUID.randomUUID());
        coll2.setName("Coll2");

        CollectionBean collBean = new CollectionBean();
        collBean.getCollectionSearchState().addCollections(Arrays.asList(coll1, coll2));

        SolrSearcher solSearcherMock = new SolrSearcherMock();

        ResourceBundle mybundle = ResourceBundle.getBundle("de.ipb_halle.lbac.i18n.messages", Locale.ENGLISH);
        User user = new User();
        instance = new FileUpload(
                asyncMock,
                collServMock,
                fileServiceMock,
                solrTVMock,
                fileEntityServiceMock,
                collBean,
                solSearcherMock,
                solrUpdate,
                mybundle,
                new TermVectorEntityServiceMock(),
                user
        );
        instance.setTermVectorParser(new TermVectorParserMock());
        instance.run();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("FileUploadTest.war");
    }

}
