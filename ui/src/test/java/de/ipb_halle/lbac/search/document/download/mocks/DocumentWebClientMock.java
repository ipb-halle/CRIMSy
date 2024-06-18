/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document.download.mocks;

import java.io.InputStream;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.download.DocumentWebClient;

@ApplicationScoped
public class DocumentWebClientMock extends DocumentWebClient {
    private Supplier<InputStream> behaviour;

    public void setBehaviour(Supplier<InputStream> behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public InputStream downloadDocument(CloudNode cn, User user, Document document) {
        return behaviour.get();
    }
}
