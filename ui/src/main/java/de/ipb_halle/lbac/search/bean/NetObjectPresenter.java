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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class NetObjectPresenter {

    private User user;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private MessagePresenter messagePresenter;

    public NetObjectPresenter(User user, MessagePresenter messagePresenter) {
        this.user = user;
        this.messagePresenter = messagePresenter;
    }

    public String getName(NetObject no) {
        return no.getNameToDisplay();
    }

    public String getNodeName(NetObject no) {
        return no.getNode().getInstitution();
    }

    public boolean isDownloadLinkVisible(NetObject no) {
        return no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT;
    }

    public boolean isInternalLinkVisible(NetObject no) {
        boolean noDoc = !(no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT);
        boolean local = no.getNode().getLocal();
        return noDoc && local && !user.isPublicAccount();
    }

    public boolean isExternalLinkVisible(NetObject no) {
        boolean noDoc = !(no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT);
        boolean local = no.getNode().getLocal();
        return noDoc && (!local || user.isPublicAccount());
    }

    public String getObjectType(NetObject no) {
        return messagePresenter.presentMessage(
                "search_category_" + no.getTypeToDisplay().getTypeName());
    }

    public String getToolTip(NetObject no) {
        return null;
    }

    public String getObjectRelevance(NetObject no) {
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
            Document d = (Document) no.getSearchable();
            return d.getFormatedRelevance();
        } else {
            return "";
        }

    }

}
