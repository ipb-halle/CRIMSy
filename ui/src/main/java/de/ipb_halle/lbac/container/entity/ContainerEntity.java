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
package de.ipb_halle.lbac.container.entity;

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
 *
 * @author fmauz
 */
@Entity
@Table(name = "containers")
public class ContainerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private Integer parentcontainer;

    @Column
    @AttributeTag(type=AttributeType.BARCODE)
    private String label;

    @Column
    private Integer projectid;

    @Column
    private String dimension;

    @Column
    private String type;

    @Column
    private String firearea;

    @Column
    private String gmosafetylevel;

    @Column
    private String barcode;

    @Column
    private boolean deactivated;

    @Column
    private boolean swapdimensions;

    @Column
    private boolean zerobased;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentcontainer() {
        return parentcontainer;
    }

    public void setParentcontainer(Integer parentcontainer) {
        this.parentcontainer = parentcontainer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getProjectid() {
        return projectid;
    }

    public void setProjectid(Integer projectid) {
        this.projectid = projectid;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFireArea() {
        return firearea;
    }

    public void setFireArea(String firearea) {
        this.firearea = firearea;
    }

    public String getGmoSafetyLevel() {
        return gmosafetylevel;
    }

    public void setGmoSafetyLevel(String level) {
        this.gmosafetylevel = level;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public boolean isSwapDimensions() {
        return swapdimensions;
    }

    public void setSwapDimensions(boolean sd) {
        this.swapdimensions = sd;
    }

    public boolean isZeroBased() {
        return zerobased;
    }

    public void setZeroBased(boolean zb) {
        this.zerobased = zb;
    }
}
