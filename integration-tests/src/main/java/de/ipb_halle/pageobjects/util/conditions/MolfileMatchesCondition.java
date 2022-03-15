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
package de.ipb_halle.pageobjects.util.conditions;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebElement;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.AtomContainerComparator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;

/**
 * Condition that checks if a Molfile from an element's attribute value
 * represents the same molecule from a given Molfile.
 * 
 * @author flange
 */
public class MolfileMatchesCondition extends Condition {
    private final String attributeName;
    private final String expectedMolfile;

    /**
     * @param attributeName
     * @param expectedMolfile unescaped V2000 Molfile
     */
    public MolfileMatchesCondition(String attributeName, String expectedMolfile) {
        super(String.format("value of attribute \"%s\" matches molfile \"%s\"", attributeName, expectedMolfile));
        this.attributeName = attributeName;
        this.expectedMolfile = expectedMolfile;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        String molfileFromElement = element.getAttribute(attributeName);

        try {
            return new CheckResult(molfilesMatch(molfileFromElement, expectedMolfile),
                    String.format("%s=\"%s\"", attributeName, molfileFromElement));
        } catch (CDKException | IOException e) {
            return new CheckResult(false, ExceptionUtils.getStackTrace(e));
        }
    }

    boolean molfilesMatch(String actualMolfile, String expectedMolfile) throws CDKException, IOException {
        if ((actualMolfile == null) || (expectedMolfile == null)) {
            return false;
        }

        AtomContainer actual;
        AtomContainer expected;

        try (MDLV2000Reader reader = new MDLV2000Reader()) {
            reader.setReader(new StringReader(actualMolfile));
            actual = reader.read(new AtomContainer());

            reader.setReader(new StringReader(expectedMolfile));
            expected = reader.read(new AtomContainer());
        }

        AtomContainerManipulator.suppressHydrogens(actual);
        AtomContainerManipulator.suppressHydrogens(expected);

        return new AtomContainerComparator().compare(actual, expected) == 0;
    }
}