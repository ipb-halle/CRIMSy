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
package de.ipb_halle.pageobjects.pages.materials.tabs;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for
 * /ui/web/WEB-INF/templates/material/components/materialNames.xhtml
 * 
 * @author flange
 */
public class MaterialNamesTab extends AbstractPage<MaterialNamesTab> implements MaterialEditTab {
    private static final SelenideElement MATERIAL_NAMES_TABLE = $(
            testId("materialNamesTab:materialNamesTable"));

    /*
     * Getters
     */
    public DataTable getMaterialNamesTable() {
        return DataTable.extract(MATERIAL_NAMES_TABLE);
    }
}