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
package de.ipb_halle.lbac.file.save;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class FileSaverTest extends TestBase {
    
    @Inject
    private CollectionService collectionService;
    
    @Inject
    private FileEntityService fileEntityService;
    
    private String exampleDocsRootFolder = "target/test-classes/exampledocs/";
    
    private User publicUser;
    private Collection col;
    
    @Before
    @Override
    public void setUp() {
        super.setUp();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }
    
    @After
    public void cleanUp() {
        Files.delete(Paths.get(col.getBaseFolder()).toFile());
        entityManagerService.doSqlUpdate("DELETE FROM collections WHERE id=" + col.getId());
    }
    
    @Test
    public void test001_saveDocumentToCollection() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
        FileSaver fileSaver = new FileSaver(fileEntityService, publicUser);
        
        col = new Collection();
        col.setACList(GlobalAdmissionContext.getPublicReadACL());
        col.setDescription("test001_saveDocumentToCollection()");
        col.setIndexPath("/");
        col.setName("test-coll");
        col.setNode(nodeService.getLocalNode());
        col.setOwner(publicUser);
        col.setStoragePath("/");
        col = collectionService.save(col);
        col.COLLECTIONS_BASE_FOLDER = "target/test-classes/collections";
        
        File f = new File(exampleDocsRootFolder + "Document1.pdf");
        FileInputStream stream = new FileInputStream(f);
        Integer id = fileSaver.saveFile(col, "Document1.pdf", stream);
        
        Assert.assertEquals(1, fileEntityService.getDocumentCount(col));
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", id);
        FileObject fo = fileEntityService.load(cmap).get(0);
        Assert.assertEquals(col.getId(), fo.getCollection().getId());
        Assert.assertEquals("en", fo.getDocument_language());
        Assert.assertEquals("Document1.pdf", fo.getName());
        Assert.assertEquals(publicUser.getId(), fo.getUser().getId());
        
        f = new File(exampleDocsRootFolder + "DocumentX.docx");
        stream = new FileInputStream(f);
        id = fileSaver.saveFile(col, "DocumentX.docx", stream);
        Assert.assertEquals(2, fileEntityService.getDocumentCount(col));
        cmap.put("id", id);
        fo = fileEntityService.load(cmap).get(0);
        Assert.assertEquals(col.getId(), fo.getCollection().getId());
        Assert.assertEquals("en", fo.getDocument_language());
        Assert.assertEquals("DocumentX.docx", fo.getName());
        Assert.assertEquals(publicUser.getId(), fo.getUser().getId());
        
        f = new File(exampleDocsRootFolder + "TestTabelle.xlsx");
        stream = new FileInputStream(f);
        id = fileSaver.saveFile(col, "TestTabelle.xlsx", stream);
        Assert.assertEquals(3, fileEntityService.getDocumentCount(col));
        cmap.put("id", id);
        fo = fileEntityService.load(cmap).get(0);
        Assert.assertEquals(col.getId(), fo.getCollection().getId());
        Assert.assertEquals("en", fo.getDocument_language());
        Assert.assertEquals("TestTabelle.xlsx", fo.getName());
        Assert.assertEquals(publicUser.getId(), fo.getUser().getId());
        
        f = new File(exampleDocsRootFolder + "TestTabelle.xlsx");
        stream = new FileInputStream(f);
        id = fileSaver.saveFile(col, "TestTabelle.xlsx", stream);
        fileSaver.updateLanguageOfFile("de");
        Assert.assertEquals(4, fileEntityService.getDocumentCount(col));
        cmap.put("id", id);
        fo = fileEntityService.load(cmap).get(0);
        Assert.assertEquals(col.getId(), fo.getCollection().getId());
        Assert.assertEquals("de", fo.getDocument_language());
        Assert.assertEquals("a9eed28584c7e6df1d061c77884820524a7d2b4c6644ef5d13b0c2daedaf4d10d040b7c7380df448f91a28eb7fba94cf0b4a964ae141032c63a0b571aeaa5ccf", fo.getHash());
        Assert.assertEquals("TestTabelle.xlsx", fo.getName());
        Assert.assertEquals(publicUser.getId(), fo.getUser().getId());
        // Assert.assertEquals(fileSaver.getFileLocation(), Paths.get(col.getBaseFolder(), "0", "0", fo.getFilename()).toString());

    }
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("FileSaverTest.war")
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }
    
}
