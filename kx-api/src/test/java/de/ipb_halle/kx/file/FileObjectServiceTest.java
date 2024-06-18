/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.kx.file;

import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fblocal
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileObjectServiceTest {

    private final int testId = 98765;
    private final static String FILE_DELETE = "DELETE FROM files WHERE id=98765";
    
    // table constraints are relaxed for testing: collection_id is not required
    private final static String FILE_CREATE = "INSERT INTO files "
        + "(id, name, filename) VALUES "
        + " (98765, 'test_file.txt', '/tmp/no_such_file/98765')";
    
    @Inject
    private FileObjectService fileObjectService;
    
    @Inject 
    private EntityManagerService ems;
    
    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "FileObjectServiceTest.war")
                .addClass(FileObjectService.class)
                .addClass(EntityManagerService.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }
    
    
    @BeforeEach
    public void init() {
        ems.getEntityManager().createNativeQuery(FILE_CREATE).executeUpdate();
    }
    
    @AfterEach
    public void after() {
        ems.getEntityManager().createNativeQuery(FILE_DELETE).executeUpdate();
    }
    
    @Test
    @Transactional
    public void test_loadByID() {
        FileObject fo = fileObjectService.loadFileObjectById(testId);
        assertEquals("/tmp/no_such_file/98765", fo.getFileLocation()); 
    }
}
