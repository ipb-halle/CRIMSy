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
package de.ipb_halle.lbac.admission;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toMap;

/**
 * Defines the different subsystems available for user, group and permission
 * management and authentication / authorization.
 *
 * DO NOT CHANGE THE ORDER OF ENUM VALUES AS THIS WOULD BREAK EXISTING
 * INSTALLATIONS. APPENDING ADDITIONAL VALUES IS OKAY THOUGH.
 *
 * BUILTIN is for internal or anonymous entities which should not be accessible
 * to managment (public account, owner account, ...).
 *
 * LOCAL is for all accounts, groups, etc. managed on the local node
 *
 * LDAP is for all accounts or groups managed by LDAP
 *
 * LBAC_REMOTE is for all accounts or groups managed by a remote instance of
 * Cloud Resource & Information Management System (CRIMSy)
 */
public enum AdmissionSubSystemType implements Serializable {

    BUILTIN(0),
    LOCAL(1),
    LDAP(2),
    LBAC_REMOTE(3);

    private final int index;
    private static final Map<Integer, AdmissionSubSystemType> int2Enum = Stream.of(values()).collect(toMap(e -> e.index, e -> e));
    private static final Map<String, AdmissionSubSystemType> string2Enum = Stream.of(values()).collect(toMap(Object::toString, e -> e));

    /**
     * constructor This enum is persisted by an index value
     */
    AdmissionSubSystemType(int i) {
        this.index = i;
    }

    /**
     * @return the SubSystemType by it's id
     */
    public static AdmissionSubSystemType byId(int id) {
        return int2Enum.get(id);
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return an instance of the respective AdmissionSubSystem
     */
    public IAdmissionSubSystem getInstance() {
        switch (this) {
            case BUILTIN:
                return new BuiltinAdmissionSubSystem();
            case LOCAL:
                return new LocalAdmissionSubSystem();
            case LDAP:
                return new LdapAdmissionSubSystem(new LdapHelper());
            case LBAC_REMOTE:
                return new LbacRemoteAdmissionSubSystem();
        }
        throw new AssertionError("Unknown AdmissionSubSystemType.");
    }

}
