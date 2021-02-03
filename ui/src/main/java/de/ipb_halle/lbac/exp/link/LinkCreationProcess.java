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

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.ItemHolder;
import de.ipb_halle.lbac.exp.LinkText;
import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.exp.LinkedDataType;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.exp.MaterialHolder;
import de.ipb_halle.lbac.items.Item;
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
public class LinkCreationProcess implements Serializable, MaterialHolder, ItemHolder {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private enum LinkType {
        MATERIAL,
        ITEM
    }
    @Inject
    protected MaterialAgent materialAgent;

    @Inject
    protected ItemAgent itemAgent;

    @Inject
    private ExperimentBean expBean;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Material material;
    private String linkText;
    private LinkType type;
    private Item item;
    private String errorMessage;

    public LinkCreationProcess() {
        
    }
    public LinkCreationProcess(
            MaterialAgent materialAgent,
            ItemAgent itemAgent,
            ExperimentBean experimentBean) {
        this.materialAgent = materialAgent;
        this.itemAgent = itemAgent;
        this.expBean=experimentBean;
    }

    @PostConstruct
    public void init() {
        materialAgent.setMaterialHolder(this);
        materialAgent.setShowMolEditor(true);
        itemAgent.setItemHolder(this);
    }

    public void startLinkCreation() {
        errorMessage = "";
        material = null;
        linkText = null;
        type = LinkType.MATERIAL;
    }

    public String onFlowProcess(FlowEvent e) {
        if (e.getNewStep().equals("step2")) {
            for (LinkedData data : expBean.getExpRecordController().getExpRecord().getLinkedData()) {
                if (data.getPayload() instanceof LinkText) {
                    if (((LinkText) data.getPayload()).getText().toLowerCase().equals(linkText.toLowerCase())) {
                        errorMessage = String.format("%s : %s",
                                linkText,
                                Messages.getString(MESSAGE_BUNDLE, "expAddRecord_addLink_nameDuplicate", null));
                        return e.getOldStep();
                    }
                }
            }
        }
        errorMessage = "";
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

    public boolean isMaterialViewEnabled() {
        return type == LinkType.MATERIAL;
    }

    public boolean isItemViewEnabled() {
        return type == LinkType.ITEM;
    }

    public ItemAgent getItemAgent() {
        return itemAgent;
    }

    public String getStepTwoHeader() {
        if (type == LinkType.MATERIAL) {
            return "choose material";
        }

        if (type == LinkType.ITEM) {
            return "choose item";
        }
        return "choose object";
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public void setItem(Item item) {
        this.item = item;
        LinkedData link = new LinkedData(
                expBean.getExpRecordController().getExpRecord(),
                LinkedDataType.LINK_ITEM,
                expBean.getExpRecordController().getExpRecord().getLinkedDataNextRank()
        );
        link.setPayload(new LinkText(linkText));
        link.setItem(item);
        expBean.getExpRecordController().getExpRecord().getLinkedData().add(link);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
