/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

/**
 * LBAC websocket for async simple search and display search results
 */

var send,
    disconnect,
    cloudIcon,
    isDataTableInit = false,
    server,
    wsocketProtokol;

function enableSpinner() {
    if (isDataTableInit) {
        cloudIcon.removeClass('fa-cloud').addClass('fa-spinner fa-spin');
    }
}

function disableSpinner() {
    if (isDataTableInit) {
        try {
            cloudIcon.removeClass('fa-spinner fa-spin').addClass('fa-cloud');
        } catch (error) {
        }
    }
}

function createWebSocketServer(wsUrl) {

    var wsUri;
    var wsServer = null;

    if (document.location.protocol === 'https:') {
        wsocketProtokol = 'wss://'
    } else {
        wsocketProtokol = 'ws://'
    }

    wsUri = wsocketProtokol + window.location.host + wsUrl;
    if ('WebSocket' in window) {
        wsServer = new WebSocket(wsUri);
    } else if ('MozWebSocket' in window) {
        wsServer = new MozWebSocket(wsUri);
    } else {
        alert('WebSocket is not supported by this browser.');
        return null;
    }
    return wsServer;
}

$(document).ready(function () {
    var searchResultTable = $('#defaultResultList').dataTable({
        "searching": false,
        "order": [[2, "desc"]]
    });
    var modalError = $(".modalError");
    var modalErrorBody = $(".modalErrorBody");
    isDataTableInit = typeof searchResultTable !== "undefined";

    //*** getting cloud icon id ***
    if (isDataTableInit) {
        var cloudIconID = $(".fa.fa-cloud").attr('id');
        //*** select cloud icon, jsf id: escaping jquery special char ':' to '\:'  ***
        if (typeof cloudIconID !== "undefined") {
            cloudIcon = $("#" + cloudIconID.replace(':', '\\:'));
        }
    }

    if (!("WebSocket" in window)) {
        modalErrorBody.text('WebSockets are not supported in this ' +
            'browser. Try Internet Explorer 10 or the latest ' +
            'versions of Mozilla Firefox or Google Chrome.');
        modalError.modal('show');
        return;
    }

    try {

        server = createWebSocketServer('/ui/websocket/search');
        server.binaryType = 'arraybuffer';

    } catch (error) {
        modalErrorBody.text(error);
        modalError.modal('show');
        return;
    }

    server.onopen = function (event) {
        console.log('websocket connection to server established.');
    };

    server.onclose = function (event) {
        if (server != null) console.log('websocket connection closed.');

        server = null;

        if (!event.wasClean || event.code === 1006) {
            server = createWebSocketServer('/ui/websocket/search');
            console.log('reconnect websocket connection. (event code: 1006)');
        } else if (!event.wasClean || (event.code !== 1000 && event.code !== 1001)) {
            modalErrorBody.text('Code ' + event.code + ': ' + event.reason);
            modalError.modal('show');
        }
    };
    server.onerror = function (event) {
        modalErrorBody.text(event.data);
        modalError.modal('show');
    };
    server.onmessage = function (event) {
        if (event.data instanceof ArrayBuffer) {
            var searchResultDoc = JSON.parse(String.fromCharCode.apply(null, new Uint8Array(event.data)));
            if (searchResultDoc.header === 'document') {
                var docBasename = searchResultDoc.body.path.split('/').reverse()[0];
                var originalName = searchResultDoc.body.originalName;
                if (!originalName) {
                    originalName = docBasename
                }
                var docExt = originalName.split('.').pop();
                var docSymbol;
                var docDownloadLink;
                var col0, col1, col2;

                switch (docExt.toLowerCase()) {
                    case 'pdf':
                        docSymbol = 'fa fa-file-pdf-o';
                        break;
                    case 'doc':
                    case 'docx':
                        docSymbol = 'fa fa-file-word-o';
                        break;
                    case 'xls':
                    case 'xlsx':
                        docSymbol = 'fa fa-file-excel-o';
                        break;
                    default:
                        docSymbol = 'fa fa-file-text-o';
                        break;
                }

                col0 = '<a target="_blank" download="' + originalName + '" href="' + searchResultDoc.body.link + '"><i class="' + docSymbol + '" aria-hidden="true"></i>' + '&nbsp;' + originalName + '</a>';
                col1 = searchResultDoc.body.collection.description + " (" + searchResultDoc.body.node.institution + ")";
                //*** toDo: calculate ranking ***
                col2 = Math.floor(Math.random() * 100);

                if (isDataTableInit) {
                    searchResultTable.fnAddData([
                        col0, col1, col2
                    ], redraw = true);
                }
            }
        } else {
            var s = event.data;
            if (s.toLowerCase() === 'done.') {
                console.log('end of search process.');
                disableSpinner();
            }
        }
    };
    send = function () {
        if (server == null) {
            modalErrorBody.text('no websocket connection established.');
            modalError.modal('show');
        } else {
            try {
                console.log('sReq: ' + $('.searchRequest').val());
                if ($('.searchRequest').val().length > 0) {
                    searchResultTable.fnClearTable();
                    server.send($('.searchRequest').val());
                    enableSpinner();
                }
            } catch (error) {
                disableSpinner();
                modalErrorBody.text(error);
                modalError.modal('show');
            }
        }
    };

    disconnect = function () {
        if (server != null) {
            console.log('websocket connection disconnected.');
            disableSpinner();
            server.close();
            server = null;
        }
    };

    window.onbeforeunload = disconnect;
});
