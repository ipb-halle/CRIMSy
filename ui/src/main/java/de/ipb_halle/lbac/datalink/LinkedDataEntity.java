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
package de.ipb_halle.lbac.datalink;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author fbroda
 */
@Entity
@Table(name = "linked_data")
public class LinkedDataEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long recordid;

    @Column
    private Long exprecordid;

    @Column
    private Integer itemid;

    @Column
    private Integer materialid;
    
    @Column
    private Integer fileid;

    @Column
    private int rank;

    @Column
    private LinkedDataType type;

    @Column
    private String payload;

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public Integer getFileid() {
        return fileid;
    }
    
    public Integer getItemId() {
        return this.itemid;
    }

    public Integer getMaterialId() {
        return this.materialid;
    }

    public String getPayload() {
        return this.payload;
    }

    public int getRank() {
        return this.rank;
    }

    public Long getRecordId() {
        return this.recordid;
    }

    public LinkedDataType getType() {
        return type;
    }

    public LinkedDataEntity setExpRecordId(Long exprecordid) {
        this.exprecordid = exprecordid;
        return this;
    }

    public void setFileid(Integer fileid) {
        this.fileid = fileid;
    }

    public LinkedDataEntity setItemId(Integer itemid) {
        this.itemid = itemid;
        return this;
    }

    public LinkedDataEntity setMaterialId(Integer materialid) {
        this.materialid = materialid;
        return this;
    }

    public LinkedDataEntity setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public LinkedDataEntity setRank(int rank) {
        this.rank = rank;
        return this;
    }

    public LinkedDataEntity setRecordId(Long recordid) {
        this.recordid = recordid;
        return this;
    }

    public LinkedDataEntity setType(LinkedDataType type) {
        this.type = type;
        return this;
    }

}
