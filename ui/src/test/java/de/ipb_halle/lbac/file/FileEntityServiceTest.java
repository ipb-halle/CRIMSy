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
package de.ipb_halle.lbac.file;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class FileEntityServiceTest extends TestBase {

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ForumServiceTest.war")
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(FileObject.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class);

    }

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Test
    public void testSave() {

        User u = createUser(
                "testuser",
                "testuser");

        ACList acl = new ACList();
        acl.setName("test");
        acl.addACE(u, ACPermission.values());

        Collection col = new Collection();
        col.setNode(this.nodeService.getLocalNode());
        col.setName("Test_Collection1");
        col.setDescription("Test_Collection1_Description");
        col.setIndexPath("/doc/test.pdf");
        col.setACList(acl);
        col.setOwner(u);

        col=collectionService.save(col);

        FileObject fE = new FileObject();
        fE.setCollection(col);
        fE.setCreated(new Date());
        fE.setDocument_language("en");
        fE.setFilename("testFile.pdf");
        fE.setHash("testHash");
        fE.setName("testFile");
        fE.setUser(u);

        fE=fileEntityService.save(fE);

        TermVector tv = new TermVector("testWord", fE.getId(), 3);
        fileEntityService.saveTermVectors(Arrays.asList(tv));
        List<String> ids = new ArrayList<>();
        ids.add(fE.getId().toString());
        termVectorEntityService.getTermVector(ids, 10);

        int sumOfWords = termVectorEntityService.getSumOfAllWordsFromAllDocs();

        List<FileObject> lfo = fileEntityService.getAllFilesInCollection(col);
        assertEquals("Found one file", 1, lfo.size());
        assertEquals("Filename of file matches", "testFile.pdf", lfo.get(0).getFilename());
        assertEquals("Document count matches", 1, fileEntityService.getDocumentCount(col));
    }
}
