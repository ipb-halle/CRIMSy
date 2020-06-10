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
package de.ipb_halle.lbac.exp.assay;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.material.service.MaterialService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.faces.bean.ManagedProperty;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Bean for interacting with the ui to present and manipulate a experiments 
 *
 * @author fbroda
 */
@RequestScoped
@Named
public class MaterialAgent {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject 
    private MaterialService materialService;

    @ManagedProperty(value="#{materialHolder}")
    private MaterialHolder materialHolder;

    private int materialId;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialAgent() {
        this.materialId = 1;
    }

    public void actionSetMaterial() {
        this.logger.info("actionSetMaterial() materialId = {}", materialId);
        if ((this.materialId == 1) ||
          (this.materialId == 2)) {
            this.materialHolder.setMaterial(
                this.materialService.loadMaterialById(this.materialId));
            }
    }

    public int getMaterialId() {
        return this.materialId;
    }

    public void setMaterialHolder(MaterialHolder materialHolder) {
        this.materialHolder = materialHolder;
    }
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
}
