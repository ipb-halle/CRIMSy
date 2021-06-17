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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nestingpathsets")
public class NestingPathSetEntity implements Serializable {

    /**
     * This class associates a nesting path (i.e. a collection 
     * of nesting path elements) to a nested membership. The nested 
     * <code>Membership</code> is specified by the <code>membership_id</code>
     * field. However, this class does NOT contain the single path elements, 
     * as this part is covered by <code>NestingPathEntity</code>.
     *
     * A nested membership can be constituted by more than one 
     * nesting path. 
     */
    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(name = "membership_id")
    private Integer membership_id;

    public NestingPathSetEntity() {
    }

    public NestingPathSetEntity(Integer id, Integer membershipId) {
        this.id = id;
        this.membership_id = membershipId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMembership_id() {
        return membership_id;
    }

    public void setMembership_id(Integer membership_id) {
        this.membership_id = membership_id;
    }

}
