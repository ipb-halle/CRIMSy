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
package de.ipb_halle.lbac.globals.health;

import java.util.HashMap;
import java.util.Map;

/**
 * Datacontainer for the represantation of the health state of the programm
 *
 * @author fmauz
 */
public class HealthState {

    public String dbVersion;
    public State localNodeDbState = State.UNCHECKED;
    public State publicCollectionDbState = State.UNCHECKED;
    public State publicCollectionFileState = State.UNCHECKED;
    public State publicCollectionSolrSyncState = State.UNCHECKED;
    public State publicCollectionFileSyncState = State.UNCHECKED;
    public State publicCollectionSolrState = State.UNCHECKED;

    public Map<String, State> collectionFileSyncList = new HashMap<>();
    public Map<String, State> collectionSolrSyncList = new HashMap<>();

    public enum State {
        UNCHECKED,
        OK,
        FAILED
    }
}
