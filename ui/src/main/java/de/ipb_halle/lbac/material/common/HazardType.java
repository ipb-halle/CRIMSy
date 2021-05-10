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
package de.ipb_halle.lbac.material.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a hazard wit a category and a name. Also stores the information,
 * if remarks can be added to the hazard
 *
 * @author fmauz
 */
public class HazardType {

    final private int id;
    final private boolean hasRemarks;
    final private String name;
    final private Category category;

    public HazardType(int id, boolean hasRemarks, String name, int categoryId) {
        this.id = id;
        this.hasRemarks = hasRemarks;
        this.name = name;
        List<Category> cats = Arrays.stream(Category.values())
                .filter(c -> c.id == categoryId)
                .collect(Collectors.toList());
        if (cats.size() == 1) {
            category = cats.get(0);
        } else {
            throw new RuntimeException(
                    String.format("Hazard with category %d could not be initialized", categoryId));
        }
    }

    public boolean isHasRemarks() {
        return hasRemarks;
    }

    public Category getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HazardType other = (HazardType) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public enum Category {
        GHS(1), STATEMENTS(2), BSL(3), RADIOACTIVITY(4), CUSTOM(5);
        public final int id;

        Category(int id) {
            this.id = id;
        }
    }

}
