/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.exp.images;

import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.util.WebXml;
import de.ipb_halle.lbac.util.WebXmlImpl;
import java.io.Serializable;

/**
 * This class is the experimental record controller for the image editor/viewer.
 * It controls the view components in the composite component <crimsy:image>,
 * which is implemented in image.xhtml.
 *
 * @author flange
 */
public class ImageController extends ExpRecordController
        implements Serializable {
    /**
     * Name of the web.xml context-param that specifies the maximum image upload
     * file size.
     */
    protected static final String WEBXML_MAXUPLOADFILESIZE = "de.ipb_halle.lbac.exp.images.MaxUploadFileSize";

    private WebXml webXml = new WebXmlImpl();

    public ImageController(ExperimentBean bean) {
        super(bean);
    }

    /**
     * Test constructor with dependency injection.
     * 
     * @param bean   mock of {@link ExperimentBean}
     * @param webXml mock implementation of the {@link WebXml} interface
     */
    protected ImageController(ExperimentBean bean, WebXml webXml) {
        super(bean);
        this.webXml = webXml;
    }

    @Override
    public ExpRecord getNewRecord() {
        ExpRecord rec = new Image("", "", "",
                getExperimentBean().getCurrentUser(),
                getExperimentBean().getExperiment().getACList());
        rec.setEdit(true);
        return rec;
    }

    @Override
    public String getOnClick() {
        return "crimsyImageEditor.saveJson('miniPaint', $('.inputJsonFilePseudoClass').attr('id'));"
                + "crimsyImageEditor.savePreview('miniPaint', $('.inputPreviewFilePseudoClass').attr('id'));";
    }

    @Override
    public void actionSaveRecord() {
        ((Image) getExpRecord()).setImage(jsonFile);

        super.actionSaveRecord();
    }

    /**
     * Returns the maximum image upload size defined in web.xml.
     * 
     * @return maximum image upload size in bytes or 0 if nothing was defined in
     *         web.xml.
     */
    public long getMaxUploadFileSize() {
        return Long.parseLong(
                webXml.getContextParam(WEBXML_MAXUPLOADFILESIZE, "0"));
    }

    /**
     * Returns the image in JSON format. This data is rendered by <h:inputHidden
     * id="inputJsonImage" ... />.
     *
     * @return image in JSON format
     */
    public String getJsonImage() {
        return ((Image) getExpRecord()).getImage();
    }

    /**
     * This setter does nothing, because {@code jsonImage} is filled from
     * {@code jsonFile} after the file upload. This process is executed by
     * {@link #actionSaveRecord()}.
     *
     * @param jsonImage
     */
    public void setJsonImage(String jsonImage) {
    }

    /**
     * This additional property is used for the file upload. It is needed
     * because jsonImage is already used by the <h:inputHidden
     * id="inputJsonImage" ... /> component. {@link #actionSaveRecord()} copies
     * it around.
     */
    private String jsonFile;

    /**
     * Returns an empty string. This getter is used in the <nwc:inputFile
     * id="inputJsonFileId" ... /> component, but this component has no outputs.
     *
     * @return empty string
     */
    public String getJsonFile() {
        return "";
    }

    /**
     * This setter receives the image in JSON format from the file upload. This
     * data comes from <nwc:inputFile id="inputJsonFileId" ... />.
     *
     * @param jsonFile
     */
    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }
}
