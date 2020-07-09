/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.material.common.MaterialName;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Functionalities for the manipulation of materialnames.
 *
 * @author fmauz
 */
public class NameListOperation implements Serializable {

    Logger logger = LogManager.getLogger(this.getClass().getName());
    private final String defaultLanguage;

    public NameListOperation(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public enum Button {
        HIGHEST,
        HIGHER,
        LOWER,
        LOWEST,
        DELETE,
        NEW
    }

    /**
     * Defines if a certain operation is allowed indipendent of userrights. The
     * checking if a user hast the right to edit a materialname is not done
     * here.
     *
     * @param mn Materialname to check for
     * @param b the operationType
     * @param materialNames list of all materialNames
     * @return is the button enabled
     */
    public boolean isEnabled(
            MaterialName mn,
            String b,
            List<MaterialName> materialNames) {

        try {
            if (materialNames.isEmpty()) {
                return false;
            }
            Button button = castStringToButton(b);

            if (button == Button.HIGHER || button == Button.HIGHEST) {
                return !materialNames.get(0).equals(mn);
            }
            if (button == Button.LOWER || button == Button.LOWEST) {
                return !materialNames.get(materialNames.size() - 1).equals(mn);
            }
            if (button == Button.DELETE) {
                return materialNames.size() > 1;
            }
            if (button == Button.NEW) {
                return true;
            }

        } catch (Exception e) {
            logger.error(e);
            return false;
        }
        return true;
    }

    public void deleteName(
            MaterialName mn,
            List<MaterialName> materialNames) {
        if (materialNames.size() > 1) {
            materialNames.remove(mn);
        }

    }

    public void setRankToLowest(MaterialName mn, List<MaterialName> materialNames) {
        int i = materialNames.indexOf(mn);
        Collections.swap(materialNames, materialNames.size() - 1, i);
    }

    public void setRankToHighest(MaterialName mn, List<MaterialName> materialNames) {
        int i = materialNames.indexOf(mn);
        Collections.swap(materialNames, 0, i);
    }

    public void addOneRank(MaterialName mn, List<MaterialName> materialNames) {
        int i = materialNames.indexOf(mn);
        Collections.swap(materialNames, i - 1, i);

    }

    public void substractOneRank(MaterialName mn, List<MaterialName> materialNames) {
        int i = materialNames.indexOf(mn);
        Collections.swap(materialNames, i + 1, i);
    }

    public void addNewEmptyName(List<MaterialName> materialNames) {
        if (materialNames.isEmpty()) {
            materialNames.add(new MaterialName("", defaultLanguage, 0));
        } else if (!materialNames.get(materialNames.size() - 1).getValue().isEmpty()) {
            materialNames.add(new MaterialName("", defaultLanguage, materialNames.size()));
        }
    }

    private Button castStringToButton(String s) throws Exception {
        try {
            return Button.valueOf(s);
        } catch (Exception e) {
            logger.error("Error at casting String " + s + " to a ButtonType", e);
            throw new Exception(e);
        }
    }
}
