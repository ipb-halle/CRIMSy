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
/*
 * These functions have been adopted from miniPaint's open-edit-save example.
 * See https://github.com/viliusle/miniPaint/blob/4eb75b3b26c57d2960a898c4ea643bda73dad6a2/examples/open-edit-save.html
 */
"use strict";

// Namespace registration
var crimsyImageEditor = crimsyImageEditor || {};

/**
 * Exports an image in miniPaint's JSON format and attaches it as blob to an
 * <input type="file"> element for file upload.
 */
crimsyImageEditor.saveJson = function(frameId, inputFileId) {
	let miniPaint = document.getElementById(frameId).contentWindow;
	let miniPaintFileSave = miniPaint.FileSave;

	// export JSON from miniPaint + minify it further
	let dataJson = JSON.stringify(JSON.parse(miniPaintFileSave.export_as_json()));

	// attach the data to the inputFile component
	crimsyImageEditor.attachBlob(inputFileId, dataJson, "image.json", "application/json");
}

/**
 * Exports a preview image as DataURI-encoded png and attaches it as blob to an
 * <input type="file"> element for file upload.
 */
crimsyImageEditor.savePreview = function(frameId, inputFileId) {
	let layers = document.getElementById(frameId).contentWindow.Layers;
	let tempCanvas = document.createElement("canvas");
	let tempCtx = tempCanvas.getContext("2d");
	let dim = layers.get_dimensions();
	tempCanvas.width = dim.width;
	tempCanvas.height = dim.height;
	layers.convert_layers_to_canvas(tempCtx);

	let data = tempCanvas.toDataURL("image/png");

	// attach the data to the inputFile component
	crimsyImageEditor.attachBlob(inputFileId, data, "image.png", "application/dataurl");
}

/**
 * Loads an image in miniPaint's JSON format from a <input> element's value
 * into a miniPaint editor instance.
 */
crimsyImageEditor.openJson = function(frameId, inputJsonId) {
	let miniPaint = document.getElementById(frameId).contentWindow;
	let miniPaintFileOpen = miniPaint.FileOpen;

	let data = document.getElementById(inputJsonId).value;

	if (data !== null && data !== '') {
		miniPaintFileOpen.load_json(data, false);

		// clear <input> where got the data from, so it is not resent
		document.getElementById(inputJsonId).value = "";
	}
}

/**
 * Attaches data as blob to an <input type="file"> element as file upload.
 * This is a snippet from https://stackoverflow.com/a/66466544
 */
crimsyImageEditor.attachBlob = function(inputFileId, data, filename, dataType) {
	let fileInputElement = document.getElementById(inputFileId);
	let blob = new Blob([data]);
	let file = new File([blob], filename, { type: dataType, lastModified: new Date().getTime() });
	let container = new DataTransfer();
	container.items.add(file);
	fileInputElement.files = container.files;
}