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
package de.ipb_halle.lbac.exp;

/**
 * @author fmauz
 */
public class ExperimentCode {

    private String prefix;
    public final String seperator = "-";
    private String suffix;
    private final String numberPattern = "####";
    private String numberFormat = "%04d";

    private ExperimentCode() {

    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    public String generateNewExperimentCode(int expNumber) {
        return prefix.replace(numberPattern, String.format(numberFormat, expNumber)) + seperator + suffix;
    }

    public String generateExistingExperimentCode() {
        return prefix.toUpperCase() + seperator + suffix;
    }

    public static ExperimentCode createNewInstance(String userShortCut) {
        ExperimentCode code = new ExperimentCode();
        code.setPrefix(userShortCut + code.getNumberPattern());
        return code;
    }

    public static ExperimentCode createInstanceOfExistingExp(String expCode) {
        if (!expCode.contains("-")) {
            throw new RuntimeException("Experimentcode not valide. Must contain a '-'");
        }
        ExperimentCode code = new ExperimentCode();
        int indexOfFirstSeperator = expCode.indexOf(code.seperator);
        code.setPrefix(expCode.substring(0, indexOfFirstSeperator));
        code.setSuffix(expCode.substring(indexOfFirstSeperator + 1));
        return code;
    }

    public String getNumberPattern() {
        return numberPattern;
    }
}
