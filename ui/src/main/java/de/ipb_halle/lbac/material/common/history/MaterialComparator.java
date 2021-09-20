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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.Material;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialComparator implements Serializable {

    private static final long serialVersionUID = 1L;
    Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Compares two materials and calculates the differences.
     *
     * @param originalMat
     * @param editedMat
     * @return List with all differences.
     * @throws Exception
     */
    public List<MaterialDifference> compareMaterial(
            Material originalMat,
            Material editedMat) throws Exception {

        List<MaterialDifference> differences = new ArrayList<>();
        if (originalMat.getType() != editedMat.getType()) {
            throw new Exception("Materials not comparable: ORIG - " + originalMat.getType() + " EDIT - " + editedMat.getType());
        }
        originalMat.getType().getFactory().createComparator().compareDifferences(differences, originalMat, editedMat);

        return differences;

    }

    /**
     * Helpermethod to fetch a concrete type of the MaterialDifference interface
     * from the list of differences.
     *
     * @param <T>
     * @param diffs
     * @param T
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getDifferenceOfType(List<MaterialDifference> diffs, Class T) {
        for (MaterialDifference sd : diffs) {
            if (sd.getClass() == T) {
                return (T) sd;
            }
        }
        return null;
    }
}
