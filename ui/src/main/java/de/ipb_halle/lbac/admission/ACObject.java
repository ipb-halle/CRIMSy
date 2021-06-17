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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.Obfuscatable;

/**
 * Abstract base class for an access controlled objects.
 */
public abstract class ACObject implements Obfuscatable {

    private ACList aclist;
    private User owner;


    /**
     * Gets the owner
     *
     * @return owner
     */
    public User getOwner() {
        return this.owner;
    }

    /**
     * Gets the ACList
     *
     * @return ACList
     */
    public ACList getACList() {
        return this.aclist;
    }

    /**
     * Obfuscates the ACObject by obfuscating the internal ACList
     */
    @Override
    public void obfuscate() {
        this.owner.obfuscate();
        this.aclist.obfuscate();
    }

    /**
     * Sets the ACList
     *
     * @param acl ACList
     * @return ACObject for chaining
     */
    public ACObject setACList(ACList acl) {
        this.aclist = acl;
        return this;
    }

    /**
     * Sets the owner of the ACObject
     *
     * @param u User who will be owner
     * @return ACObject for chaining
     */
    public ACObject setOwner(User u) {
        this.owner = u;
        return this;
    }

}
