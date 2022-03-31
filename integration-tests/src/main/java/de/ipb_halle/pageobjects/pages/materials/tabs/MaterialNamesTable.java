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
import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Selenide.$$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.List;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.materials.models.MaterialNamesModel;
import de.ipb_halle.pageobjects.pages.materials.models.MaterialNamesModel.MaterialName;

/**
 * Page object for the material names table in
 * /ui/web/WEB-INF/templates/material/components/materialNames.xhtml
 * 
 * @author flange
 */
public class MaterialNamesTable extends DataTable<MaterialNamesTable> {
    private static final String NAME_INPUT = testId("materialNamesTab:materialNamesTable:name");
    private static final String LANGUAGE_SELECTION = testId("select", "materialNamesTab:materialNamesTable:language");
    private static final String TO_TOP_BUTTON = testId("materialNamesTab:materialNamesTable:toTop");
    private static final String UP_BUTTON = testId("materialNamesTab:materialNamesTable:up");
    private static final String DOWN_BUTTON = testId("materialNamesTab:materialNamesTable:down");
    private static final String TO_BUTTOM_BUTTON = testId("materialNamesTab:materialNamesTable:toButtom");
    private static final String DELETE_BUTTON = testId("materialNamesTab:materialNamesTable:delete");
    private static final String ADD_BUTTON = testId("materialNamesTab:materialNamesTable:add");

    public MaterialNamesTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    /**
     * Deletes all entries in the table and applies the given material name model.
     * 
     * @param model
     * @return this
     */
    public MaterialNamesTable applyModel(MaterialNamesModel model) {
        clear();

        int size = model.getNames().size();
        for (int i = 0; i < size; i++) {
            applyName(model.getNames().get(i), i);
            if (i < size - 1) {
                add(0);
            }
        }

        return this;
    }

    private void clear() {
        // delete all existing name entries until there is only one left
        while ($$(NAME_INPUT).size() > 1) {
            delete(1);
        }

        // reset remaining entry
        nameInput(0).setValue(null);
        languageSelection(0).selectOption(0);
    }

    private void applyName(MaterialName name, int index) {
        nameInput(index).setValue(name.getName());
        languageSelection(index).selectOption(name.getLanguage());
    }

    public MaterialNamesTable toTop(int rowIndex) {
        getCell(2, rowIndex).$(TO_TOP_BUTTON).click();
        return this;
    }

    public MaterialNamesTable up(int rowIndex) {
        getCell(2, rowIndex).$(UP_BUTTON).click();
        return this;
    }

    public MaterialNamesTable down(int rowIndex) {
        getCell(2, rowIndex).$(DOWN_BUTTON).click();
        return this;
    }

    public MaterialNamesTable toButtom(int rowIndex) {
        getCell(2, rowIndex).$(TO_BUTTOM_BUTTON).click();
        return this;
    }

    public MaterialNamesTable delete(int rowIndex) {
        getCell(2, rowIndex).$(DELETE_BUTTON).click();
        return this;
    }

    public MaterialNamesTable add(int rowIndex) {
        getCell(2, rowIndex).$(ADD_BUTTON).click();
        return this;
    }

    /*
     * Fluent assertions
     */
    public MaterialNamesTable shouldHave(MaterialNamesModel model) {
        List<MaterialName> names = model.getNames();
        $$(NAME_INPUT).shouldHave(size(names.size()));

        for (int i = 0; i < names.size(); i++) {
            MaterialName name = names.get(i);
            $$(NAME_INPUT).get(i).shouldHave(exactValue(name.getName()));
            $$(LANGUAGE_SELECTION).get(i).shouldHave(exactValue(name.getLanguage()));
        }

        return this;
    }

    /*
     * Getters
     */
    public SelenideElement nameInput(int rowIndex) {
        return getCell(0, rowIndex).$(NAME_INPUT);
    }

    public SelenideElement languageSelection(int rowIndex) {
        return getCell(1, rowIndex).$(LANGUAGE_SELECTION);
    }
}