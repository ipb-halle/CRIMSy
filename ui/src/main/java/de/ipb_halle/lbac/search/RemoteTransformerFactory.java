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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.RemoteExperimentTransformer;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.RemoteItemTransformer;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.RemoteMaterialTransformer;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.RemoteDocumentTransformer;

/**
 *
 * @author fmauz
 */
public class RemoteTransformerFactory {

    public RemoteTransformer createSpecificTransformer(Searchable searchable) {
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.MATERIAL) {
            return new RemoteMaterialTransformer((Material) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.ITEM) {
            return new RemoteItemTransformer((Item) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
            return new RemoteDocumentTransformer((Document) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.EXPERIMENT) {
            return new RemoteExperimentTransformer((Experiment) searchable);
        }

        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.CONTAINER) {
            throw new UnsupportedOperationException("Remote transformer for CONTAINER not yet implemented ");
        }

        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.PROJECT) {
            throw new UnsupportedOperationException("Remote transformer for PROJECT not yet implemented ");
        }

        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.USER) {
            throw new UnsupportedOperationException("Remote transformer for USER not yet implemented ");
        }
        return null;
    }

}
