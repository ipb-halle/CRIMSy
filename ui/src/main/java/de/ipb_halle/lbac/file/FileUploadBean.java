package de.ipb_halle.lbac.file;

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.service.TextWebRequestType;
import de.ipb_halle.kx.service.TextWebStatus;
import de.ipb_halle.lbac.admission.UserBean;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.save.FileSaver;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
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
@SessionScoped
@Named
public class FileUploadBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int UPLOAD_TIMEOUT = 60;

    private Integer fileId;
    private FileSaver fileSaver;
    private String fileName;
    private UploadedFile uploadedFile;
    private InputStream inputStream;
    private AttachmentHolder holder;
    private AnalyseClient analyseClient;

    private UploadedFile file;
    private UploadedFiles files;
    private String dropZoneText = "Drop zone p:inputTextarea demo.";
    private final Logger logger = LogManager.getLogger(FileUploadBean.class);

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    private Collection selectedCollection;
    @Inject
    private UserBean userBean;

    List<Collection> values;

    public List<Collection> getPossibleValues() {
        return values;
    }

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

    public void upload() {
        if (file != null) {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void uploadMultiple() {
        if (files != null) {
            for (UploadedFile f : files.getFiles()) {
                FacesMessage message = new FacesMessage("Successful", f.getFileName() + " is uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public UploadedFiles getFiles() {
        return files;
    }

    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    public String getDropZoneText() {
        return dropZoneText;
    }

    public void setDropZoneText(String dropZoneText) {
        this.dropZoneText = dropZoneText;
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
            }
            if (status != TextWebStatus.DONE) {
                throw new Exception("Analysis returned an unexpected status code: " + status.toString());
            }
            FacesMessage message = new FacesMessage("File " + fileName + " was successfully uploaded.");
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
