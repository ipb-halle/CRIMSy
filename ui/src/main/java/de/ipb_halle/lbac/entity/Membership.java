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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Class for membership associations
 */
public class Membership implements Serializable, DTO {

    private final static long serialVersionUID = 1L;

    private UUID id;

    private boolean nested;

    private Member group;

    private Member member;

    private Set<NestingPath> nestingPathSet;

    /**
     * Constructor by dbentity and members
     *
     * @param entity
     * @param group
     * @param member
     * @param npSet
     */
    @SuppressWarnings("unchecked")
    public Membership(
            MembershipEntity entity,
            Member group,
            Member member,
            java.util.Collection<NestingPath> npSet) {

        this.id = entity.getId();
        this.nested = entity.getNested();
        this.group = group;
        this.member = member;
        this.nestingPathSet = new HashSet(npSet);
    }

    /**
     * default constructor
     */
    public Membership() {
        this(null, null, false);
    }

    /**
     * constructor
     *
     * @param g the group
     * @param m the member of the group
     * @param n true if it is a nested membership
     */
    public Membership(Member g, Member m, boolean n) {
        this.id = UUID.randomUUID();
        setGroup(g);
        setMember(m);
        setNested(n);
        this.nestingPathSet = new HashSet<NestingPath>();
    }

    @Override
    public MembershipEntity createEntity() {
        MembershipEntity entity = new MembershipEntity();
        entity.setGroup(this.group.getId());
        entity.setMember(this.member.getId());
        entity.setId(this.id);
        entity.setNested(this.nested);
        return entity;
    }

    public void dump(Logger logger) {
        String psize = "n.a.";
        try {
            psize = Integer.toString(this.nestingPathSet.size());
        } catch (Exception e) {
            // size of lazy loaded nestingPathSet may be unavailable 
            // outside of transaction /  session
        }
        logger.info(String.format("Membership.dump(): group=%s member=%s (%s:%s) npSetSize=%s %s",
                this.group.getId().toString(), this.member.getId().toString(),
                this.group.getName(), this.member.getName(), psize,
                this.nested ? "nested" : "direct"));
        Iterator<NestingPath> iter = this.nestingPathSet.iterator();

        logger.info(" ");
    }

    /**
     * equals only depends on groupId and memberId this is necessary e.g.for
     * nestingPath maintenance
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof Membership)) {
            Membership ms = (Membership) o;
            return getGroupId().equals(ms.getGroupId())
                    && getMemberId().equals(ms.getMemberId());
        }
        return false;
    }

    public UUID getId() {
        return this.id;
    }

    public Member getGroup() {
        return this.group;
    }

    public UUID getGroupId() {
        return this.group.getId();
    }

    public Member getMember() {
        return this.member;
    }

    public UUID getMemberId() {
        return this.member.getId();
    }

    public boolean getNested() {
        return this.nested;
    }

    public Set<NestingPath> getNestingPathSet() {
        return this.nestingPathSet;
    }

    /**
     * hashCode only depends on groupId and memberId this is necessary e.g. for
     * nestingPath maintenance
     */
    @Override
    public int hashCode() {
        return this.group.getId().hashCode() + this.member.getId().hashCode();
    }

    public void setGroup(Member m) {
        this.group = m;
    }

    public void setMember(Member m) {
        this.member = m;
    }

    public void setId(UUID i) {
        this.id = i;
    }

    public void setNested(boolean b) {
        this.nested = b;
    }

}
