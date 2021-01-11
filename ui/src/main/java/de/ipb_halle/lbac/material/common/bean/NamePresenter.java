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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.Material;

/**
 * Formats materialnames as a html format with a linebreak between the names.
 *
 * @author fmauz
 */
public class NamePresenter {

    public String getFormatedNames(Material material, int maxNamesShown) {
        String back = "";
        for (int i = 0; i < Math.min(material.getNames().size(), maxNamesShown); i++) {
            back += material.getNames().get(i).getValue() + "<br>";
        }
        if (material.getNames().size() > maxNamesShown) {
            back += "...";
        }

        if (back.endsWith("<br>")) {
            back = back.substring(0, back.length() - "<br>".length());
        }
        return back;
    }

}
