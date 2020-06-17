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
package de.ipb_halle.lbac.container;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.container.entity.ContainerNestingEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingId;
import java.io.Serializable;

/**
 *
 * @author fmauz
 */
public class ContainerNesting implements DTO, Serializable {

    protected boolean nested;
    protected int source;
    protected int target;

    public ContainerNesting(ContainerNestingEntity dbentity) {
        this.source = dbentity.getId().getSourceid();
        this.target = dbentity.getId().getTargetid();
        this.nested = dbentity.isNested();
    }

    public ContainerNesting(int source, int target, boolean nested) {
        this.source = source;
        this.target = target;
        this.nested = nested;
    }

    @Override
    public ContainerNestingEntity createEntity() {
        ContainerNestingEntity dbentity = new ContainerNestingEntity();
        dbentity.setId(new ContainerNestingId(source, target));
        dbentity.setNested(nested);
        return dbentity;
    }

    public boolean isNested() {
        return nested;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

}
