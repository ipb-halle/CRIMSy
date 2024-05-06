package de.ipb_halle.lbac.file;

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.service.TextWebRequestType;
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.lbac.admission.UserBean;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.save.FileSaver;
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

    private AnalyseClient analyseClient;
    private AttachmentHolder holder;
    private Collection selectedCollection;
    private FileSaver fileSaver;
    private InputStream inputStream;
    private Integer fileId;
    private List<Collection> values;
    private String fileName;
    private UploadedFile uploadedFile;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private UserBean userBean;

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

            fileSaver = new FileSaver(fileObjectService, userBean.getCurrentAccount());
            fileId = fileSaver.saveFile(holder, fileName, inputStream);

            analyseClient = new AnalyseClient();
            TextWebStatus status = analyseClient.analyseFile(fileId, TextWebRequestType.SUBMIT);
            while (status == TextWebStatus.BUSY) {
                status = analyseClient.analyseFile(fileId, TextWebRequestType.QUERY);
                Thread.sleep(1000);
            }
            if (status != TextWebStatus.DONE) {
                //throw new Exception("Analysis returned an unexpected status code: " + status.toString());
                FacesMessage message = new FacesMessage("File ", fileName + " upload of file " + fileName + " was not successfull");
                //Vernüftige Fehlermessage zum UI hin
                //Aufräumen des "leeren" File aus Zeile 99
            }
            FacesMessage message = new FacesMessage("File ", fileName + " was successfully uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);

        } catch (IOException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler!", "Beim Hochladen von " + fileName + " ist ein Fehler aufgetreten.");
            FacesContext.getCurrentInstance().addMessage(null, message);
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
}
