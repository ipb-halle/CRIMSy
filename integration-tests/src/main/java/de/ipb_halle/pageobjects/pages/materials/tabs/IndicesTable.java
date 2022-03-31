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

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Selenide.$$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.List;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.materials.models.IndicesModel;
import de.ipb_halle.pageobjects.pages.materials.models.IndicesModel.Index;

/**
 * Page object for the index table in
 * /ui/web/WEB-INF/templates/material/components/indices.xhtml
 * 
 * @author flange
 */
public class IndicesTable extends DataTable<IndicesTable> {
    private static final String INDEX_VALUE_INPUT = testId("input", "indicesTab:indicesTable:indexValue");
    private static final String DELETE_BUTTON = testId("indicesTab:indicesTable:delete");

    public IndicesTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public IndicesTable delete(int rowIndex) {
        getCell(2, rowIndex).$(DELETE_BUTTON).click();
        return this;
    }

    /**
     * Deletes all index entries.
     * 
     * @return this
     */
    public IndicesTable clear() {
        while ($$(DELETE_BUTTON).size() > 0) {
            delete(0);
        }
        return this;
    }

    /*
     * Fluent assertions
     */
    public IndicesTable shouldHave(IndicesModel model) {
        List<Index> indices = model.getIndices();
        $$(INDEX_VALUE_INPUT).shouldHave(size(indices.size()));

        for (int i = 0; i < indices.size(); i++) {
            Index index = indices.get(i);
            indexName(i).shouldHave(exactTextCaseSensitive(index.getCategory()));
            $$(INDEX_VALUE_INPUT).get(i).shouldHave(exactValue(index.getValue()));
        }

        return this;
    }

    /*
     * Getters
     */
    public SelenideElement indexName(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement indexValueInput(int rowIndex) {
        return getCell(1, rowIndex).$(INDEX_VALUE_INPUT);
    }
}