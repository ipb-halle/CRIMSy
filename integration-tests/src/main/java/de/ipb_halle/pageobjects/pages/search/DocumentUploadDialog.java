/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.pageobjects.pages.search;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.io.File;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for the document upload dialog in
 * /ui/web/WEB-INF/templates/fileupload.xhtml and
 * /ui/web/resources/tmpl/fineUploaderMultiTemplate.xhtml.
 * 
 * @author flange
 */
public class DocumentUploadDialog extends PrimeFacesDialog {
    private static final SelenideElement COLLECTION_SELECTION = $(testId("select", "documentUpload:collection"));
    private static final SelenideElement CLEAR_BUTTON = $(testId("documentUpload:clear"));
    private static final SelenideElement CLOSE_BUTTON = $(testId("documentUpload:close"));
    private static final SelenideElement SELECT_FILES = $(testId("documentUpload:selectFiles") + " > input");
    private static final SelenideElement UPLOAD_BUTTON = $(testId("documentUpload:upload"));
    private static final ElementsCollection CANCEL_UPLOAD_BUTTONS = $$(testId("documentUpload:cancelUpload"));

    /*
     * Actions
     */
    public DocumentUploadDialog selectCollection(String collection) {
        COLLECTION_SELECTION.selectOption(collection);
        return this;
    }

    public DocumentUploadDialog clear() {
        CLEAR_BUTTON.click();
        return this;
    }

    public SearchPage close() {
        CLOSE_BUTTON.click();
        return page(SearchPage.class);
    }

    public DocumentUploadDialog submitUpload() {
        UPLOAD_BUTTON.click();
        return this;
    }

    public DocumentUploadDialog cancelUpload(int index) {
        CANCEL_UPLOAD_BUTTONS.get(index).click();
        return this;
    }

    public DocumentUploadDialog uploadFiles(File... files) {
        SELECT_FILES.uploadFile(files);
        submitUpload();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement fileUpload() {
        return SELECT_FILES;
    }
}