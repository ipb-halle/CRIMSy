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

public class ImageController extends ExpRecordController {
    public ImageController(ExperimentBean bean) {
        super(bean);
    }

    @Override
    public ExpRecord getNewRecord() {
        ExpRecord rec = new Image("", "", getExperimentBean().getCurrentUser(),
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

    public String getPreviewImage() {
        return ((Image) getExpRecord()).getPreview();
    }

    public void setPreviewImage(String preview) {
        ((Image) getExpRecord()).setPreview(preview);
    }

    public String getJsonImage() {
        return ((Image) getExpRecord()).getImage();
    }

    public void setJsonImage(String jsonImage) {
        /*
         * Nothing done here, because jsonImage should be filled via the file upload.
         */
    }

    /*
     * This additional property is needed because jsonImage is already used by
     * the inputHidden component. actionSaveRecord() copies it around.
     */
    private String jsonFile;

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }
}