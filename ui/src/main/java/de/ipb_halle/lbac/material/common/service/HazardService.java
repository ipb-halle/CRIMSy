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
package de.ipb_halle.lbac.material.common.service;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class HazardService implements Serializable {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;
    protected final Logger logger = LogManager.getLogger(this.getClass().getName());
    private Map<HazardType.Category, List<HazardType>> loadedHazardTypes = new HashMap<>();

    /**
     * Return a list of all hazards of the given category.
     *
     * @param cat
     * @return Empty list, if no hazard of category was found
     */
    public List<HazardType> getHazardOf(HazardType.Category cat) {
        if (loadedHazardTypes.containsKey(cat)) {
            return loadedHazardTypes.get(cat);
        } else {
            return new ArrayList<>();
        }
    }

    public List<HazardType> getAllHazardTypes() {
        List<HazardType> hazards = new ArrayList<>();
        for (HazardType.Category cat : HazardType.Category.values()) {
            hazards.addAll(getHazardOf(cat));
        }

        return hazards;
    }

    /**
     * Returns all possible hazardcategories for a given materialtype
     *
     * @param type
     * @return
     */
    public Set<HazardType.Category> getAllowedCatsOf(MaterialType type) {
        Set<HazardType.Category> categories = new HashSet<>();
        categories.add(HazardType.Category.CUSTOM);
        if (type == MaterialType.BIOMATERIAL) {
            categories.add(HazardType.Category.BSL);
            categories.add(HazardType.Category.GMO);
        }
        if (type == MaterialType.COMPOSITION) {
            categories.add(HazardType.Category.GHS);
            categories.add(HazardType.Category.RADIOACTIVITY);
            categories.add(HazardType.Category.BSL);
            categories.add(HazardType.Category.STATEMENTS);
        }
        if (type == MaterialType.STRUCTURE) {
            categories.add(HazardType.Category.GHS);
            categories.add(HazardType.Category.RADIOACTIVITY);
            categories.add(HazardType.Category.STATEMENTS);
        }
        if (type == MaterialType.TISSUE) {
            categories.add(HazardType.Category.BSL);
        }
        return categories;
    }

    @PostConstruct
    public void init() {
        List<HazardEntity> entities = loadHazardEntities();
        for (HazardEntity en : entities) {
            HazardType hazard = new HazardType(en.getId(), en.getHas_remarks(), en.getName(), en.getCategory());
            if (!loadedHazardTypes.containsKey(hazard.getCategory())) {
                loadedHazardTypes.put(hazard.getCategory(), new ArrayList<>());
            }
            loadedHazardTypes.get(hazard.getCategory()).add(hazard);
        }
    }

    public HazardType getHazardById(int id) {
        for (HazardType.Category cat : loadedHazardTypes.keySet()) {
            for (HazardType h : loadedHazardTypes.get(cat)) {
                if (h.getId() == id) {
                    return h;
                }
            }
        }
        throw new IllegalArgumentException("Could not load hazard with id " + id);
    }

    public HazardType getHazardByName(String name) {
        for (HazardType.Category cat : loadedHazardTypes.keySet()) {
            for (HazardType h : loadedHazardTypes.get(cat)) {
                if (h.getName().equals(name)) {
                    return h;
                }
            }
        }
        throw new IllegalArgumentException("Could not load hazard with name " + name);
    }

    private List<HazardEntity> loadHazardEntities() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HazardEntity> cq = cb.createQuery(HazardEntity.class);
        Root<HazardEntity> rootEntry = cq.from(HazardEntity.class);
        CriteriaQuery<HazardEntity> all = cq.select(rootEntry);
        TypedQuery<HazardEntity> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

}
