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
package de.ipb_halle.lbac.items.entity;

import de.ipb_halle.lbac.admission.ACObjectEntity;
import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "items")
public class ItemEntity extends ACObjectEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @AttributeTag(type = AttributeType.ID)
    @Id
    private Integer id;

    @Column
    private Integer materialid;

    @Column
    private Double amount;

    @Column
    private Integer articleid;

    @Column
    private Integer projectid;

    @Column
    private Double concentration;

    @Column
    private String concentrationunit;

    @Column
    private String unit;

    @Column
    private String purity;

    @Column
    private Integer solventid;

    @Column
    @AttributeTag(type = AttributeType.TEXT)
    private String description;

    @Column
    private Double containersize;

    @Column
    private Integer containerid;

    @Column
    private String containertype;

    @Column
    private Date ctime;

    @Column
    private Date expiry_date;

    @Column
    @AttributeTag(type = AttributeType.BARCODE)
    private String label;

    @Column
    private Integer parent_id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaterialid() {
        return materialid;
    }

    public void setMaterialid(Integer materialid) {
        this.materialid = materialid;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getArticleid() {
        return articleid;
    }

    public void setArticleid(Integer articleid) {
        this.articleid = articleid;
    }

    public Integer getProjectid() {
        return projectid;
    }

    public void setProjectid(Integer projectid) {
        this.projectid = projectid;
    }

    public Double getConcentration() {
        return concentration;
    }

    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public String getConcentrationUnit() {
        return concentrationunit;
    }

    public void setConcentrationUnit(String unit) {
        this.concentrationunit = unit;
    }

    public String getPurity() {
        return purity;
    }

    public void setPurity(String purity) {
        this.purity = purity;
    }

    public Integer getSolventid() {
        return solventid;
    }

    public void setSolventid(Integer solventid) {
        this.solventid = solventid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getContainersize() {
        return containersize;
    }

    public void setContainersize(Double containersize) {
        this.containersize = containersize;
    }

    public Integer getContainerid() {
        return containerid;
    }

    public void setContainerid(Integer containerid) {
        this.containerid = containerid;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getContainertype() {
        return containertype;
    }

    public void setContainertype(String containertype) {
        this.containertype = containertype;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(Date expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getParent_id() {
        return parent_id;
    }

    public void setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
    }

}
