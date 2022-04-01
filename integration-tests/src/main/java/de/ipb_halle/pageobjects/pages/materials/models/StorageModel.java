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
package de.ipb_halle.pageobjects.pages.materials.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Model class for input data in {@link StorageTab}.
 * 
 * @author flange
 */
public class StorageModel {
    private static final int FIRST = 1;
    private static final int LAST = 12;

    private Boolean storageClassActivated;
    private String storageClass;
    private String remarks;
    private Set<Integer> activatedStorageConditions = new HashSet<>();

    /*
     * Fluent setters
     */
    public StorageModel storageClassActivated(Boolean storageClassActivated) {
        this.storageClassActivated = storageClassActivated;
        return this;
    }

    public StorageModel storageClass(String storageClass) {
        this.storageClass = storageClass;
        return this;
    }

    public StorageModel remarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    /**
     * Activate the storage condition(s).
     * 
     * @param conditions number of the storage condition - valid range is 1 to 12
     *                   according to CRIMSy's {@link StorageCondition} class.
     * @return this
     */
    public StorageModel activateStorageConditions(int... conditions) {
        for (int num : conditions) {
            activatedStorageConditions.add(num);
        }
        return this;
    }

    /*
     * Getters
     */
    public Boolean getStorageClassActivated() {
        return storageClassActivated;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public String getRemarks() {
        return remarks;
    }

    public Set<Integer> getActivatedStorageConditions() {
        return activatedStorageConditions;
    }

    public Set<Integer> getDeactivatedStorageConditions() {
        Set<Integer> deactivated = new HashSet<>();
        for (int i = FIRST; i <= LAST; i++) {
            if (!activatedStorageConditions.contains(i)) {
                deactivated.add(i);
            }
        }
        return deactivated;
    }
}