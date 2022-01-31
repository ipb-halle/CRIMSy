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

/**
 * Adds the given linkText as tag to the text in the open Quill editor with the
 * given PrimeFaces widgetVar.
 */
function insertLinkTagIntoQuillEditor(linkText, widgetVar) {
    let quill = PF(widgetVar).editor;
    let pos = _getCursorPosition(quill);
    let linkTag = _getLinkTagWithSpaces(linkText, quill.getText(), pos);
    quill.insertText(pos, linkTag);
}

function _getCursorPosition(quill) {
    let pos = quill.getLength() - 1;
    let selection = quill.getSelection(true);
    if (selection != null) {
        pos = selection.index;
    }
    return pos;
}

function _getLinkTagWithSpaces(linkText, textInQuill, pos) {
    let linkTag = "#" + linkText;

    /*
     * Note:
     * The text in Quill always ends with '\n'. Thus, textInQuill.length is always
     * greater than 0.
     */
    // text in Quill is empty
    if (textInQuill.length <= 1) {
        return linkTag;
    }
    // insert at start of text and other text follows
    else if ((pos == 0) && (textInQuill.length > 1)) {
        let charAfter = textInQuill.charAt(pos);
        if (charAfter != ' ') {
            return linkTag + " ";
        }
        return linkTag;
    }
    // insert at end of text
    else if (pos >= textInQuill.length - 1) {
        let charBefore = textInQuill.charAt(pos - 1);
        if (charBefore != ' ') {
            return " " + linkTag;
        }
        return linkTag;
    }
    // insert in between
    else {
        let charBefore = textInQuill.charAt(pos - 1);
        let charAfter = textInQuill.charAt(pos);
        let result = linkTag;
        if (charBefore != ' ') {
            result = " " + result;
        }
        if (charAfter != ' ') {
            result = result + " ";
        }
        return result;
    }
}
