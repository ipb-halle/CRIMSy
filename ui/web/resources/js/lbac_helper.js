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

/*
 * used by UIAugmentedText
 * determine the styleclass ('.dlgXxxx') and 
 * name ('dlg_Xxxx') of a dialog widget appropriate 
 * for a given link type.
 */
function getLinkDialog(linkType) {
    switch(linkType) {
        case "LINK_MATERIAL" : return "MaterialView";
        case "LINK_ITEM" : return "ItemView";
    }
    alert("Invalid link type");
    return null;
}

/*
 * used by UIAugmentedText to display link elements 
 * which open a modal dialog window
 */
function openLinkDialog(linkType, clientId, index) {
    var linkType = getLinkDialog(linkType);

    PrimeFaces.ab({
        source: clientId,
        params: [ {name: "linkedDataIndex", value: index}, ],
        process : clientId,
        update: "@(.dlg" + linkType + ")",
        oncomplete: function(xhr, status, args) {PF("dlg_" + linkType).show(); }
    });
    return false;
}

/*
 * encode font awesome symbol for file types 
 */
function encodeFontAwesomeDocSymbol(docExt) {
    switch (docExt.toLowerCase()) {
        case 'pdf':
            return 'fa fa-file-pdf-o';
        case 'doc':
        case 'docx':
            return 'fa fa-file-word-o';
        case 'xls':
        case 'xlsx':
            return 'fa fa-file-excel-o';
        default:
            return 'fa fa-file-text-o';
    }
}

/*
 * Adds the given linkText as tag to the text in the open Quill editor with the
 * given PrimeFaces widget var.
 */
function insertLinkTagIntoQuillEditor(linkText, widgetVar) {
    var linkTag = "#" + linkText + " ";
    var quill = PF(widgetVar).editor;

    // get cursor position
    var pos = quill.getLength() - 1;
    var selection = quill.getSelection(true);
    if (selection != null) {
        pos = selection.index;
    }

    // insert tag
    quill.insertText(pos, linkTag);
}
