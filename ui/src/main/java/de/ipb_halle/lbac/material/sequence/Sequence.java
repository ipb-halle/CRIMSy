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

    private SequenceData sequenceData;

    public Sequence(
            Integer id,
            List<MaterialName> names,
            Integer projectId,
            HazardInformation hazards,
            StorageInformation storageInfos,
            SequenceData sequenceData) {
        super(id, names, projectId, hazards, storageInfos);
        this.type = MaterialType.SEQUENCE;

        this.sequenceData = sequenceData;
    }

    public static Sequence fromEntities(MaterialEntity materialEntity, SequenceEntity sequenceEntity) {
        Integer id = sequenceEntity.getId();
        List<MaterialName> names = new ArrayList<>();
        Integer projectId = materialEntity.getProjectid();
        HazardInformation hazards = new HazardInformation();
        StorageInformation storageInfos = new StorageInformation();

        SequenceData data = sequenceDatafromSequenceEntity(sequenceEntity);

        return new Sequence(id, names, projectId, hazards, storageInfos, data);
    }

    private static SequenceData sequenceDatafromSequenceEntity(SequenceEntity entity) {
        SequenceData.Builder builder = SequenceData.builder();
        builder.sequenceString(entity.getSequenceString());
        builder.circular(entity.isCircular());
        builder.annotations(entity.getAnnotations());

        String type = entity.getSequenceType();
        if (type != null) {
            builder.sequenceType(SequenceType.valueOf(type));
        }

        return builder.build();
    }

    @Override
    public Sequence copyMaterial() {
        Sequence copy = new Sequence(
                id,
                getCopiedNames(),
                projectId,
                hazards.copy(),
                storageInformation.copy(),
                SequenceData.builder(sequenceData).build());

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
        entity.setId(id);
        entity.setSequenceString(sequenceData.getSequenceString());
        entity.setSequenceType(sequenceData.getSequenceType().name());
        entity.setCircular(sequenceData.isCircular());
        entity.setAnnotations(sequenceData.getAnnotations());

        return entity;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Sequence)) {
            return false;
        }
        Sequence otherSequence = (Sequence) other;
        return Objects.equals(otherSequence.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.SEQUENCE);
    }

    public SequenceData getSequenceData() {
        return sequenceData;
    }

    public void setSequenceData(SequenceData data) {
        this.sequenceData = data;
    }
}