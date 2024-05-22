package de.ipb_halle.lbac.file;

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.service.TextWebRequestType;
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.service.FileService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;

import java.io.Serializable;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FileUploadBean Klasse für den Dateiupload.
 *
 * @author swittche
 */
@RequestScoped
@Named
public class FileUploadBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int UPLOAD_TIMEOUT = 60;
    private final Logger logger = LogManager.getLogger(FileUploadBean.class);

    private AnalyseClient analyseClient = new AnalyseClient();
    private AttachmentHolder holder;
    private Collection selectedCollection;
    private InputStream inputStream;
    private List<Collection> values;
    private String fileName;

    private FileObject fileObject;
    private UploadedFile uploadedFile;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private UserBean userBean;

    @Inject
    private FileService fileService;

    @Inject
    private MessagePresenter messagePresenter;

    @PostConstruct
    public void initCollection() {
        selectedCollection = new Collection();
        values = new ArrayList<>();
        for (Collection c : collectionBean.getCreatableLocalCollections()) {
            values.add(c);
        }
    }

    //GETTER
    public Collection getSelectedCollection() {
        return selectedCollection;
    }

    //SETTER
    public void setSelectedCollection(Collection selectedCollection) {
        this.selectedCollection = selectedCollection;
    }

    public List<Collection> getPossibleValues() {
        return values;
    }

    public void handleFileUpload(FileUploadEvent event) throws Exception {
        try {
            uploadedFile = event.getFile();

            fileName = uploadedFile.getFileName();
            inputStream = uploadedFile.getInputStream();
            holder = getAttachmentTarget();

            fileObject = fileService.saveFile(holder, fileName, inputStream, userBean.getCurrentAccount());
            logger.info("handleFileUpload() got fileId=" + fileObject.getId());

            TextWebStatus status = analyseClient.analyseFile(fileObject.getId(), TextWebRequestType.SUBMIT);
            while (status == TextWebStatus.BUSY) {
                status = analyseClient.analyseFile(fileObject.getId(), TextWebRequestType.QUERY);
                Thread.sleep(1000);
            }
            FacesMessage message=null;
            if (status != TextWebStatus.DONE) {
                fileService.deleteFile(fileObject.getFileLocation());
                fileObjectService.delete(fileObject);

                // xxxxx I18N for messagePresenter
                messagePresenter.error("Upload of file " + fileName + " was not successfull");
            } else {
                messagePresenter.info("Upload of file " + fileName + " was successfull");
            }
        } catch (IOException e) {
            messagePresenter.error("Upload of file " + fileName + " was not successfull");
            // logger.warn("handleFileUpload() caught IOException", (Throwable) e);
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.getLocalizedMessage();
                }
            }
        }
    }

    private AttachmentHolder getAttachmentTarget() throws Exception {
        final String collectionName = selectedCollection.getName();
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", collectionName);
        cmap.put("local", true);
        List<Collection> collection = collectionService.load(cmap);
        if (collection.isEmpty()) {
            throw new Exception("Could not find collection with name " + collectionName);
        }
        return collection.get(0);
    }

    public void setAnalyseClient(final AnalyseClient analyseClient) {
        this.analyseClient = analyseClient;
    }

    /**
     * Make fileObject accessible for test purposes
     */
    protected FileObject getFileObject() {
        return fileObject;
    }
}
