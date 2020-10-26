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

import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.service.NodeService;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author fmauz
 */
public class NetObjectPresenter {

    private NodeService nodeService;

    public String getName(NetObject no) {
        return no.getNameToDisplay();
    }

    public String getNodeName(NetObject no) {
        return no.getNode().getInstitution();
    }

    public String getObjectType(NetObject no) {
        return no.getTypeToDisplay().getTypeName();
    }

    public String getLink(NetObject no) throws UnsupportedEncodingException {
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
            Document d = (Document) no.getSearchable();
            return d.getLink();
        } else {
            return "-";
        }
    }

    public boolean isNavLinkDisabled(NetObject no) {
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
            return false;
        } else {
            return true;
        }
    }

    public String getToolTip(NetObject no) {
        return "This is the tooltip for " + no.toString();
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
