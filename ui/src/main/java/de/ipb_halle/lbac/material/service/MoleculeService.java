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
package de.ipb_halle.lbac.material.service;

import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Uses the function of the postgress plugIn pgchem for molecule specific
 * calculations
 *
 * @author fmauz
 */
@Stateless
public class MoleculeService implements Serializable {

    private final String SQL_CALCULATE_EXACT_MASS = "select exactmass(cast(:mol as molecule))";
    private final String SQL_CALCULATE_MASS = "select molweight(cast(:mol as molecule))";
    private final String SQL_MOL_FORMULA = "select molformula(cast(:mol as molecule))";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    /**
     * Calculates the exact molar mass of the molecule
     *
     * @param molecule
     * @return
     */
    public double getExactMolarMassOfMolecule(String molecule) {
        Object o = em.createNativeQuery(SQL_CALCULATE_EXACT_MASS)
                .setParameter("mol", molecule)
                .getSingleResult();
        return (double) o;
    }

    /**
     * Calculates the average mass of the molecule
     *
     * @param molecule
     * @return
     */
    public double getMolarMassOfMolecule(String molecule) {
        Object o = em.createNativeQuery(SQL_CALCULATE_MASS)
                .setParameter("mol", molecule)
                .getSingleResult();
        return (double) o;
    }

    /**
     * Calculates the plain formula
     *
     * @param molecule
     * @return
     */
    public String getMolFormulaOfMolecule(String molecule) {
        Object o = em.createNativeQuery(SQL_MOL_FORMULA)
                .setParameter("mol", molecule)
                .getSingleResult();

        return (String) o;
    }
}
