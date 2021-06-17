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

var openPanel = null;

window.onload = function () {
    addStateSaveBehavior();
};

function addStateSaveBehavior() {

    var panels = [
        '.hazardsPanelClass',
        '.materialNamesPanelClass',
        '.indicesPanelClass',
        '.storagePanelClass',
        '.structurePanelClass'];

    panels.forEach(function (panel) {
        $(panel).on('shown.bs.collapse', function () {
            openPanel = panel;
        });
        $(panel).on('hidden.bs.collapse', function () {
            openPanel = null;
        });
    });

}

function openDetailPanel() {
    if (openPanel !== null) {
        $(openPanel + ' .panel-collapse').attr("class", "panel-collapse collapse in");
        $(openPanel + ' .panel-collapse').attr("aria-expanded", true);
    }
}