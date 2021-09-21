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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class Sequence extends Material {
    private static final long serialVersionUID = 1L;

    private SequenceData data;

    public Sequence(
            Integer id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageInformation storageInfos,
            SequenceData sequenceData) {
        super(id, names, projectId, hazards, storageInfos);
        this.type = MaterialType.SEQUENCE;

        data = sequenceData;
    }

    public static Sequence fromEntities(MaterialEntity materialEntity, SequenceEntity sequenceEntity) {
        Integer id = sequenceEntity.getId();
        List<MaterialName> names = new ArrayList<>();
        int projectId = materialEntity.getProjectid();
        HazardInformation hazards = new HazardInformation();
        StorageInformation storageInfos = new StorageInformation();

        SequenceData data = sequenceDatafromSequenceEntity(sequenceEntity);

        return new Sequence(id, names, projectId, hazards, storageInfos, data);
    }

    private static SequenceData sequenceDatafromSequenceEntity(SequenceEntity entity) {
        SequenceData result = new SequenceData();
        result.setSequenceString(entity.getSequenceString());
        result.setSequenceType(SequenceType.valueOf(entity.getSequenceType()));
        result.setCircular(entity.isCircular());
        result.setAnnotations(entity.getAnnotations());

        return result;
    }

    @Override
    public Sequence copyMaterial() {
        Sequence copy = new Sequence(
                id,
                getCopiedNames(),
                projectId,
                hazards.copy(),
                storageInformation.copy(),
                new SequenceData(data));

        copy.setIndices(getCopiedIndices());
        copy.setNames(getCopiedNames());
        copy.setDetailRights(getCopiedDetailRights());
        copy.setOwner(getOwner());
        copy.setACList(getACList());
        copy.setCreationTime(creationTime);
        copy.setHistory(history);

        return copy;
    }

    @Override
    public SequenceEntity createEntity() {
        SequenceEntity entity = new SequenceEntity();
        entity.setId(this.getId());
        entity.setSequenceString(data.getSequenceString());
        entity.setSequenceType(data.getSequenceType().name());
        entity.setCircular(data.isCircular());
        entity.setAnnotations(data.getAnnotations());

        return entity;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Sequence)) {
            return false;
        }
        Sequence otherUser = (Sequence) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.SEQUENCE);
    }

    public SequenceData getData() {
        return data;
    }

    public void setData(SequenceData data) {
        this.data = data;
    }
}
