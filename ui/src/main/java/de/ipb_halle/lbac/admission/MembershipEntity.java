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

import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class for membership associations
 */
@Entity
@Table(name = "MEMBERSHIPS")
@AttributeTag(type = AttributeType.MEMBERSHIP)
public class MembershipEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private boolean nested;

    @Column(name = "group_id")
    private Integer group;

    @Column(name = "member_id")
    @AttributeTag(type = AttributeType.MEMBER)
    private Integer member;

    public MembershipEntity() {
    }

    /**
     * equals only depends on groupId and memberId this is necessary e.g.for
     * nestingPath maintenance
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof MembershipEntity)) {
            MembershipEntity ms = (MembershipEntity) o;
            return getGroup().equals(ms.getGroup())
                    && getMember().equals(ms.getMember());
        }
        return false;
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getGroup() {
        return this.group;
    }

    public Integer getMember() {
        return this.member;
    }

    public boolean getNested() {
        return this.nested;
    }

    /**
     * hashCode only depends on groupId and memberId this is necessary e.g. for
     * nestingPath maintenance
     */
    @Override
    public int hashCode() {
        return this.group.hashCode() + this.member.hashCode();
    }

    public void setGroup(Integer m) {
        this.group = m;
    }

    public void setMember(Integer m) {
        this.member = m;
    }

    public void setId(Integer i) {
        this.id = i;
    }

    public void setNested(boolean b) {
        this.nested = b;
    }

}
