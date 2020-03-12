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
package de.ipb_halle.lbac.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Embeddable;

/**
 * Id class for ACList / Member / Permission associations
 */
@Embeddable
public class ACEntryId implements Serializable {

    private final static long serialVersionUID = 1L;

    private UUID aclist_id;
    private UUID member_id;

    /**
     *
     */
    public ACEntryId() {
        this(null, null);
    }

    /**
     *
     * @param ai Id of the ACList
     * @param m Id of the Member
     */
    public ACEntryId(UUID ai, UUID m) {
        this.aclist_id = ai;
        this.member_id = m;
    }

    /**
     * Checks equality based on the acList Id and the memberid
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        assert (member_id != null);
        assert (aclist_id != null);
        if (o instanceof ACEntryId) {
            ACEntryId ai = (ACEntryId) o;
            if (ai.member_id.equals(this.member_id)
                    && ai.aclist_id.equals(this.aclist_id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the ACListid
     *
     * @return acList id
     */
    public UUID getAclId() {
        return this.aclist_id;
    }

    /**
     * Gets the member id
     *
     * @return memberid
     */
    public UUID getMemberId() {
        return this.member_id;
    }

    /**
     * Generates Hashcode based on the memberid and acList id
     *
     * @return
     */
    @Override
    public int hashCode() {
        assert (member_id != null);
        assert (aclist_id != null);

        return aclist_id.hashCode() + member_id.hashCode();
    }

    /**
     * Sets id of ACList
     *
     * @param ai id of AcList
     */
    public void setAclId(UUID ai) {
        this.aclist_id = ai;
    }

    /**
     * Sets Id of member
     *
     * @param m id of member
     */
    public void setMemberId(UUID m) {
        this.member_id = m;
    }

}
