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
package de.ipb_halle.lbac.exp.link;

import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.LinkText;
import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.exp.LinkedDataType;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.exp.MaterialHolder;
import de.ipb_halle.lbac.exp.Payload;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FlowEvent;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class LinkCreationProcess implements Serializable, MaterialHolder {

    public enum LinkType {
        STRUCTURE,
        BIOMATERIAL,
        ITEM
    }
    @Inject
    protected MaterialAgent materialAgent;

    @Inject
    private ExperimentBean expBean;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Material material;
    private String linkText;
    private LinkType type;

    @PostConstruct
    public void init() {
        materialAgent.setMaterialHolder(this);
    }

    public void startLinkCreation() {
        material = null;
        linkText = null;
    }

    public String onFlowProcess(FlowEvent e) {
        return e.getNewStep();
    }

    public MaterialAgent getMaterialAgent() {
        return materialAgent;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public List<MaterialType> getMaterialTypes() {
        return Arrays.asList(MaterialType.STRUCTURE);
    }

    @Override
    public void setMaterial(Material material) {
        this.material = material;
        LinkedData link = new LinkedData(
                expBean.getExpRecordController().getExpRecord(),
                LinkedDataType.LINK_MATERIAL,
                expBean.getExpRecordController().getExpRecord().getLinkedDataNextRank()
        );
        link.setPayload(new LinkText(linkText));
        link.setMaterial(material);
        expBean.getExpRecordController().getExpRecord().getLinkedData().add(link);
        logger.info("new Link set");
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public LinkType[] getTypes() {
        return LinkType.values();
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

}
