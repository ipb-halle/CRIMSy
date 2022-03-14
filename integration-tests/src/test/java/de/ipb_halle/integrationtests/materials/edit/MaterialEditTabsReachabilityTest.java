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

import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.disabledTab;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.enabledTab;
import static de.ipb_halle.test.UniqueGenerators.uniqueProjectName;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeborne.selenide.junit5.SoftAssertsExtension;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialEditPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialOverviewPage;
import de.ipb_halle.pageobjects.pages.projects.ProjectModel;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.util.TestConstants;
import de.ipb_halle.test.ModelTools;
import de.ipb_halle.test.SelenideAllExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideAllExtension.class, SoftAssertsExtension.class })
@DisplayName("Test reachability of tabs on the material edit page for different material types")
public class MaterialEditTabsReachabilityTest {
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

    @Test
    @DisplayName("Reachability of tabs for the material type STRUCTURE.")
    public void test_tabReachabilityForStructure() {
        materialEditPage.selectMaterialType("STRUCTURE");

        materialEditPage.materialNamesTab().shouldBe(enabledTab);
        materialEditPage.indicesTab().shouldBe(enabledTab);
        materialEditPage.structureInfosTab().shouldBe(enabledTab);
        materialEditPage.sequenceInfosTab().shouldBe(disabledTab);
        materialEditPage.harzardsTab().shouldBe(enabledTab);
        materialEditPage.storageTab().shouldBe(enabledTab);
        materialEditPage.biodataTab().shouldBe(disabledTab);
        materialEditPage.compositionTab().shouldBe(disabledTab);
    }

    @Test
    @DisplayName("Reachability of tabs for the material type BIOMATERIAL.")
    public void test_tabReachabilityForBiomaterial() {
        materialEditPage.selectMaterialType("BIOMATERIAL");

        materialEditPage.materialNamesTab().shouldBe(enabledTab);
        materialEditPage.indicesTab().shouldBe(disabledTab);
        materialEditPage.structureInfosTab().shouldBe(disabledTab);
        materialEditPage.sequenceInfosTab().shouldBe(disabledTab);
        materialEditPage.harzardsTab().shouldBe(enabledTab);
        materialEditPage.storageTab().shouldBe(disabledTab);
        materialEditPage.biodataTab().shouldBe(enabledTab);
        materialEditPage.compositionTab().shouldBe(disabledTab);
    }

    @Test
    @DisplayName("Reachability of tabs for the material type CONSUMABLE.")
    public void test_tabReachabilityForConsumable() {
        materialEditPage.selectMaterialType("CONSUMABLE");

        materialEditPage.materialNamesTab().shouldBe(enabledTab);
        materialEditPage.indicesTab().shouldBe(disabledTab);
        materialEditPage.structureInfosTab().shouldBe(disabledTab);
        materialEditPage.sequenceInfosTab().shouldBe(disabledTab);
        materialEditPage.harzardsTab().shouldBe(enabledTab);
        materialEditPage.storageTab().shouldBe(disabledTab);
        materialEditPage.biodataTab().shouldBe(disabledTab);
        materialEditPage.compositionTab().shouldBe(disabledTab);
    }

    @Test
    @DisplayName("Reachability of tabs for the material type COMPOSITION.")
    public void test_tabReachabilityForComposition() {
        materialEditPage.selectMaterialType("COMPOSITION");

        materialEditPage.materialNamesTab().shouldBe(enabledTab);
        materialEditPage.indicesTab().shouldBe(enabledTab);
        materialEditPage.structureInfosTab().shouldBe(disabledTab);
        materialEditPage.sequenceInfosTab().shouldBe(disabledTab);
        materialEditPage.harzardsTab().shouldBe(enabledTab);
        materialEditPage.storageTab().shouldBe(enabledTab);
        materialEditPage.biodataTab().shouldBe(disabledTab);
        materialEditPage.compositionTab().shouldBe(enabledTab);
    }

    @Test
    @DisplayName("Reachability of tabs for the material type SEQUENCE.")
    public void test_tabReachabilityForSequence() {
        materialEditPage.selectMaterialType("SEQUENCE");

        materialEditPage.materialNamesTab().shouldBe(enabledTab);
        materialEditPage.indicesTab().shouldBe(enabledTab);
        materialEditPage.structureInfosTab().shouldBe(disabledTab);
        materialEditPage.sequenceInfosTab().shouldBe(enabledTab);
        materialEditPage.harzardsTab().shouldBe(disabledTab);
        materialEditPage.storageTab().shouldBe(disabledTab);
        materialEditPage.biodataTab().shouldBe(disabledTab);
        materialEditPage.compositionTab().shouldBe(disabledTab);
    }
}