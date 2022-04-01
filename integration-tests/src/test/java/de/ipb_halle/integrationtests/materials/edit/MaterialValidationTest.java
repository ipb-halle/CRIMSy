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
package de.ipb_halle.integrationtests.materials.edit;

import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.uiMessage;
import static de.ipb_halle.test.UniqueGenerators.uniqueProjectName;

import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.codeborne.selenide.junit5.SoftAssertsExtension;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialEditPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialOverviewPage;
import de.ipb_halle.pageobjects.pages.projects.ProjectModel;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.util.I18n;
import de.ipb_halle.pageobjects.util.TestConstants;
import de.ipb_halle.test.ModelTools;
import de.ipb_halle.test.SelenideAllExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideAllExtension.class, SoftAssertsExtension.class })
@DisplayName("Validation errors when saving invalid materials")
public class MaterialValidationTest {
    private Locale locale = Locale.ENGLISH;
    private static String projectName;
    private static MaterialEditPage materialEditPage;

    @BeforeAll
    public static void beforeAll() {
        projectName = uniqueProjectName();
        ProjectModel project = new ProjectModel().name(projectName).owner(TestConstants.ADMIN_NAME)
                .projectType("BIOCHEMICAL_PROJECT");

        LoginPage loginPage = open("/", LoginPage.class);
        ModelTools.createProject(project, loginPage);

        materialEditPage = loginPage.loginAsAdmin(SearchPage.class).navigateTo(MaterialOverviewPage.class).newMaterial()
                .selectProject(projectName);
    }

    @AfterAll
    public static void afterAll() {
        LoginPage loginPage = materialEditPage.logout(LoginPage.class).navigateToLoginPage();
        ModelTools.deactivateProject(projectName, loginPage);
    }

    @DisplayName("After trying to save a material without names, there should be a validation error.")
    @ParameterizedTest
    @ValueSource(strings = { "STRUCTURE", "BIOMATERIAL", "CONSUMABLE", "COMPOSITION" })
    public void test_saveMaterial_withoutNames(String materialType) {
        materialEditPage.selectMaterialType(materialType).save(MaterialEditPage.class).errorMessages()
                .shouldBe(uiMessage("materialCreation_error_EMPTY_MATERIAL_NAME", locale));
    }

    @DisplayName("After trying to save a SEQUENCE without names and sequence type, there should be validation errors.")
    @Test
    public void test_saveSequence_withoutNames_and_withoutSequenceType() {
        String expectedError = I18n.getUIMessage("materialCreation_error_EMPTY_MATERIAL_NAME", locale) + "\n"
                + I18n.getUIMessage("materialCreation_error_NO_SEQUENCETYPE_CHOSEN", locale);
        materialEditPage.selectMaterialType("SEQUENCE").save(MaterialEditPage.class).errorMessages()
                .shouldBe(exactTextCaseSensitive(expectedError));
    }
}