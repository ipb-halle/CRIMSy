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
package de.ipb_halle.lbac.admission.group.mock;

import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.group.DeactivateGroupWebClient;
import de.ipb_halle.lbac.entity.CloudNode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class DeactivateGroupWebClientMock extends DeactivateGroupWebClient {

    @Override
    public void deactivateGroupAtRemoteNodes(
            User u,
            CloudNode cloudNode,
            Group groupToDeactivate,
            UUID localNodeId,
            PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    }
}
