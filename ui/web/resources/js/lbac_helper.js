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


var isInitModalError = false,
    modalError,
    modalErrorBody;

/** calc offset between server and client time and save it to cookie **/
function calcOffset() {
    var serverTime = $.cookie('serverTime');
    serverTime = serverTime == null ? null : Math.abs(serverTime);
    var clientTimeOffset = (new Date()).getTime() - serverTime;
    $.cookie('clientTimeOffset', clientTimeOffset);
}

/** check session time out every 10 seconds **/
function checkSession() {
    var sessionExpiry = Math.abs($.cookie('sessionExpiry'));
    var timeOffset = Math.abs($.cookie('clientTimeOffset'));
    var localTime = (new Date()).getTime();

    if ((localTime - timeOffset) > (sessionExpiry + 15000)) {
        console.log('Session TimeOut!');
        if (isInitModalError) {
            $("h4#modalError_Label").text('Session TimeOut');
            modalErrorBody.html(
                '<b>Web-Session Zeitüberschreitung.</b>' +
                '<p>Bitte laden Sie die Seite neu.</p>' +
                '<b>Session timeout.</b>' +
                '<p>Please reload page.</p>');
            modalError.modal('show');
        } else {
            alert('Session timeout. please reload page.')
        }
    } else {
        setTimeout('checkSession()', 10000);
    }
}

//*** encode font awesome symbol for file types ***
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

//*** create download link with font awesome symbol: <a ..><i class=".." ..> LinkText </a>
function getDocumentDownloadLink(resultDoc) {
    if (resultDoc === undefined) return "";
    var originalName = (resultDoc.body.originalName ? resultDoc.body.originalName : resultDoc.body.path.split('/').reverse()[0]);
    var docSymbol = encodeFontAwesomeDocSymbol(originalName.split('.').pop());
    var $i = $("<i>", {class: docSymbol, 'aria-hidden': "true"}).append('&nbsp;' + originalName);
    return $("<a>", {target: "_blank", download: originalName, href: resultDoc.body.link, html: $i}).prop('outerHTML');
}

//*** show/hide search in progress ***
function enableSpinner() {
    if (isDataTableInit) {
        cloudIcon.removeClass('fa-cloud').addClass('fa-spinner fa-spin');
    }
}

function disableSpinner() {
    if (isDataTableInit) {
        cloudIcon.removeClass('fa-spinner fa-spin').addClass('fa-cloud');
    }
}

//*** check browser canvas support ***
function isCanvasSupported() {
    var elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
}

$(function () {

    modalError = $(".modalError");
    modalErrorBody = $(".modalErrorBody");
    isInitModalError = typeof modalError !== "undefined";

    calcOffset();
    checkSession();
});

