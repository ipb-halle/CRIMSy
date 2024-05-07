package de.ipb_halle.lbac.file;


import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.*;
import de.ipb_halle.lbac.file.mock.AnalyseClientMock;
import de.ipb_halle.lbac.file.mock.FileSaverMock;
import de.ipb_halle.lbac.file.mock.UploadedFileMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.FileUploadEvent;
import org.testng.Assert;

import java.util.Arrays;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class FileUploadBeanTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private FileUploadBean fileUploadBean;

    @Inject
    private UserBeanMock userBean;

    @Inject
    private MessagePresenterMock messagePresenter;


    @Test
    public void handleFileUpload_whenAnalysingFails_emptyFileWasDeleted() throws Exception {
        AnalyseClient analyseClient = new AnalyseClientMock(Arrays.asList(TextWebStatus.PROCESSING_ERROR));
        fileUploadBean.setAnalyseClient(analyseClient);
        userBean.setCurrentAccount(publicUser);
        Collection collection = createLocalCollections(
                createAcList(publicUser, true),
                nodeService.getLocalNode(),
                publicUser,
                "collection1",
                "collection1",
                collectionService
        ).get(0);

        fileUploadBean.setSelectedCollection(collection);
        fileUploadBean.setFileSaver(new FileSaverMock(fileObjectService));

        FileUploadEvent uploadedFile = new FileUploadEvent(new UIViewRoot(), new UploadedFileMock());
        fileUploadBean.handleFileUpload(uploadedFile);

        Assert.assertEquals("Upload of file DummyFile was not successfull",messagePresenter.getLastErrorMessage());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("FileUploadBeanTest.war");
        deployment.addClass(KeyManager.class);
        deployment.addClass(CollectionService.class);
        deployment.addClass(CollectionOrchestrator.class);
        deployment.addClass(CollectionWebClient.class);
        deployment.addClass(Updater.class);
        deployment.addClass(CollectionSearchState.class);
        deployment.addClass(ACListService.class);
        deployment.addClass(FileObjectService.class);
        deployment.addClass(FileService.class);
        deployment.addClass(CollectionBean.class);
        deployment.addClass(DocumentSearchService.class);
        deployment.addClass(TermVectorService.class);
        deployment.addClass(UserBeanMock.class);
        deployment.addClass(MembershipOrchestrator.class);
        return UserBeanDeployment.add(deployment.addClass(FileUploadBean.class));
    }
}
