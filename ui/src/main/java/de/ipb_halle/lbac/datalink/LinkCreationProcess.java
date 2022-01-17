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

import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.ItemHolder;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.exp.MaterialHolder;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
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
    private static final long serialVersionUID = 1L;

    private static final Pattern LINKTEXT_PATTERN = Pattern.compile("[\\w]+");
    private static final List<MaterialType> ALLOWED_MATERIAL_TYPES = Arrays.asList(MaterialType.STRUCTURE,
            MaterialType.COMPOSITION, MaterialType.SEQUENCE);

    private enum LinkType {
        MATERIAL,
        ITEM
    }

    @Inject
    private transient MessagePresenter messagePresenter;

    @Inject
    protected MaterialAgent materialAgent;

    @Inject
    protected ItemAgent itemAgent;

    @Inject
    private ExperimentBean expBean;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Material material;
    private String linkText = "";
    private LinkType linkType;
    private MaterialType materialType;
    private Item item;
    private String errorMessage;

    public LinkCreationProcess() {
    }

    /**
     * Constructor for test purposes to inject the dependencies
     * @param materialAgent
     * @param itemAgent
     * @param experimentBean 
     */
    public LinkCreationProcess(
            MaterialAgent materialAgent,
            ItemAgent itemAgent,
            ExperimentBean experimentBean) {
        this.materialAgent = materialAgent;
        this.itemAgent = itemAgent;
        this.expBean = experimentBean;
    }

    @PostConstruct
    public void init() {
        linkText = "";
        materialAgent.setMaterialHolder(this);
        materialAgent.setShowMolEditor(true);
        itemAgent.setItemHolder(this);
    }

    public void startLinkCreation() {
        errorMessage = "";
        material = null;
        item = null;
        linkText = "";
        linkType = LinkType.MATERIAL;
        materialType = ALLOWED_MATERIAL_TYPES.get(0);
        materialAgent.clearAgent();
        itemAgent.clearAgent();
    }

    public String onFlowProcess(FlowEvent e) {
        if ("step2".equals(e.getNewStep())) {
            if (!checkLinkTextValidity()) {
                errorMessage = messagePresenter.presentMessage("expAddRecord_addLink_invalidLinkText");
                return e.getOldStep();
            }
            if (!checkDuplicateLinkTextInSameExpRecord()) {
                errorMessage = String.format("%s : %s", linkText,
                        messagePresenter.presentMessage("expAddRecord_addLink_nameDuplicate"));
                return e.getOldStep();
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
        return Arrays.asList(materialType);
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

    public LinkType[] getLinkTypes() {
        return LinkType.values();
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType type) {
        this.linkType = type;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public List<MaterialType> getChoosableMaterialTypes() {
        return ALLOWED_MATERIAL_TYPES;
    }

    public boolean isMaterialViewEnabled() {
        return linkType == LinkType.MATERIAL;
    }

    public boolean isItemViewEnabled() {
        return linkType == LinkType.ITEM;
    }

    public ItemAgent getItemAgent() {
        return itemAgent;
    }

    public String getStepTwoHeader() {
        if (linkType == LinkType.MATERIAL) {
            return messagePresenter.presentMessage("materialCreation_step1_chooseMaterial");
        }
        if (linkType == LinkType.ITEM) {
            return messagePresenter.presentMessage("materialCreation_step1_chooseItem");
        }
        return messagePresenter.presentMessage("materialCreation_step1_chooseObject");
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

    private boolean checkLinkTextValidity() {
        return LINKTEXT_PATTERN.matcher(linkText).matches();
    }

    private boolean checkDuplicateLinkTextInSameExpRecord() {
        for (LinkedData data : expBean.getExpRecordController().getExpRecord().getLinkedData()) {
            if (data.getPayload() instanceof LinkText) {
                if (((LinkText) data.getPayload()).getText().equalsIgnoreCase(linkText)) {
                    return false;
                }
            }
        }
        return true;
    }
}
