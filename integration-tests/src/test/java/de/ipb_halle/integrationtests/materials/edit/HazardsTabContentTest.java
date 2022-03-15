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

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.image;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.uiMessage;
import static de.ipb_halle.test.UniqueGenerators.uniqueProjectName;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeborne.selenide.junit5.SoftAssertsExtension;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialEditPage;
import de.ipb_halle.pageobjects.pages.materials.MaterialOverviewPage;
import de.ipb_halle.pageobjects.pages.materials.tabs.HazardsTab;
import de.ipb_halle.pageobjects.pages.materials.tabs.HazardsTab.BioSafetyData;
import de.ipb_halle.pageobjects.pages.materials.tabs.HazardsTab.GHSData;
import de.ipb_halle.pageobjects.pages.projects.ProjectModel;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.util.TestConstants;
import de.ipb_halle.test.ModelTools;
import de.ipb_halle.test.SelenideAllExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideAllExtension.class, SoftAssertsExtension.class })
@DisplayName("Contents of the hazards tab for different material types")
public class HazardsTabContentTest {
    private Locale locale = Locale.ENGLISH;
    private static String projectName;
    private static MaterialEditPage materialEditPage;
    private static HazardsTab hazardsTab;

    @BeforeAll
    public static void beforeAll() {
        projectName = uniqueProjectName();
        ProjectModel project = new ProjectModel().name(projectName).owner(TestConstants.ADMIN_NAME)
                .projectType("BIOCHEMICAL_PROJECT");

        LoginPage loginPage = open("/", LoginPage.class);
        ModelTools.createProject(project, loginPage);

        materialEditPage = loginPage.loginAsAdmin(SearchPage.class).navigateTo(MaterialOverviewPage.class).newMaterial()
                .selectProject(projectName);
        // open hazard tab only once, because it may become obscured in some situations
        hazardsTab = materialEditPage.selectMaterialType("STRUCTURE").openHazardsTab();
    }

    @AfterAll
    public static void afterAll() {
        LoginPage loginPage = materialEditPage.logout(LoginPage.class).navigateToLoginPage();
        ModelTools.deactivateProject(projectName, loginPage);
    }

    @Test
    @DisplayName("Contents for the material type STRUCTURE")
    public void test_contentsStructure() {
        materialEditPage.selectMaterialType("STRUCTURE");

        checkGHSData(hazardsTab.ghsData());
        checkHStatements(hazardsTab);
        checkPStatements(hazardsTab);
        checkRadioactive(hazardsTab);
        checkNoBioSafetyData(hazardsTab);
        checkNoGMO(hazardsTab);
        checkCustomRemarks(hazardsTab);
    }

    @Test
    @DisplayName("Contents for the material type BIOMATERIAL")
    public void test_contentsForBiomaterial() {
        materialEditPage.selectMaterialType("BIOMATERIAL");

        checkNoGHSData(hazardsTab);
        checkNoHStatements(hazardsTab);
        checkNoPStatements(hazardsTab);
        checkNoRadioactive(hazardsTab);
        checkBioSafetyData(hazardsTab.bioSafetyData());
        checkGMO(hazardsTab);
        checkCustomRemarks(hazardsTab);
    }

    @Test
    @DisplayName("Contents for the material type CONSUMABLE")
    public void test_contentsForConsumable() {
        materialEditPage.selectMaterialType("CONSUMABLE");

        checkNoGHSData(hazardsTab);
        checkNoHStatements(hazardsTab);
        checkNoPStatements(hazardsTab);
        checkNoRadioactive(hazardsTab);
        checkNoBioSafetyData(hazardsTab);
        checkNoGMO(hazardsTab);
        checkCustomRemarks(hazardsTab);
    }

    @Test
    @DisplayName("Contents for the material type COMPOSITION")
    public void test_contentsForComposition() {
        materialEditPage.selectMaterialType("COMPOSITION");

        checkGHSData(hazardsTab.ghsData());
        checkHStatements(hazardsTab);
        checkPStatements(hazardsTab);
        checkRadioactive(hazardsTab);
        checkBioSafetyData(hazardsTab.bioSafetyData());
        checkNoGMO(hazardsTab);
        checkCustomRemarks(hazardsTab);
    }

    private void checkGHSData(GHSData ghsData) {
        ghsData.ghsTable().shouldBe(visible);

        for (int i = 0; i <= 10; i++) {
            String messageKey = String.format("hazard_GHS%02d", i + 1);
            ghsData.label(i).shouldBe(visible).shouldBe(uiMessage(messageKey, locale));
            ghsData.image(i).shouldBe(visible).shouldBe(image);
        }
    }

    private void checkNoGHSData(HazardsTab hazardsTab) {
        hazardsTab.ghsData().ghsTable().shouldNot(exist);
    }

    private void checkPStatements(HazardsTab hazardsTab) {
        hazardsTab.pStatementsInput().shouldBe(visible);
    }

    private void checkNoPStatements(HazardsTab hazardsTab) {
        hazardsTab.pStatementsInput().shouldNot(exist);
    }

    private void checkHStatements(HazardsTab hazardsTab) {
        hazardsTab.hStatementsInput().shouldBe(visible);
    }

    private void checkNoHStatements(HazardsTab hazardsTab) {
        hazardsTab.hStatementsInput().shouldNot(exist);
    }

    private void checkRadioactive(HazardsTab hazardsTab) {
        hazardsTab.radioactiveLabel().shouldBe(visible).shouldBe(uiMessage("hazard_R1", locale));
        hazardsTab.radioactiveImage().shouldBe(visible).shouldBe(image);
    }

    private void checkNoRadioactive(HazardsTab hazardsTab) {
        hazardsTab.radioactiveLabel().shouldNot(exist);
        hazardsTab.radioactiveImage().shouldNot(exist);
    }

    private void checkBioSafetyData(BioSafetyData bioSafetyData) {
        bioSafetyData.biosafetyLevelTable().shouldBe(visible);

        for (int i = 0; i <= 4; i++) {
            /*
             * 'risk group 1' ("hazard_S1") comes first, 'unclassified' ("hazard_S0") last,
             * that's why this weird calculation.
             */
            bioSafetyData.label(i).shouldBe(visible).shouldBe(uiMessage("hazard_S" + (i + 1) % 5, locale));
            bioSafetyData.image(i).shouldBe(visible).shouldBe(image);
        }
    }

    private void checkNoBioSafetyData(HazardsTab hazardsTab) {
        hazardsTab.bioSafetyData().biosafetyLevelTable().shouldNot(exist);
    }

    private void checkGMO(HazardsTab hazardsTab) {
        hazardsTab.gmoCheckbox().label().shouldBe(visible).shouldBe(uiMessage("hazard_GMO", locale));
    }

    private void checkNoGMO(HazardsTab hazardsTab) {
        hazardsTab.gmoCheckbox().label().shouldNot(exist);
    }

    private void checkCustomRemarks(HazardsTab hazardsTab) {
        hazardsTab.customRemarksInput().shouldBe(visible);
    }
}