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

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * NOTE: This test runs NOT within an Arquillian context and therefore ignores
 * the @PostConstruct annotation in class LdapProperties (which otherwise would
 * override the LDAP_GROUP_FILTER_DN setting unless it is saved here).
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class LdapHelperTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private InfoObjectService infoService;

    @Inject
    private LdapProperties properties;

    private LdapHelper instance;
    public static final String PROPERTY_DSF = "ldap.dsf";
    public static final String DSF_FILE = "file";
    public static final String DEFAULT_DSF = DSF_FILE;

    @BeforeEach
    public final void init() {
        saveInfo("LDAP_ENABLE", "True");
        saveInfo("LDAP_CONTEXT_PROVIDER_URL", "ldap://localhost:10389");
        saveInfo("LDAP_CONTEXT_SECURITY_CREDENTIALS", "certificatePassword");
        saveInfo("LDAP_BASE_DN", "dc=keycloak,dc=org");
        saveInfo("LDAP_CONTEXT_SECURITY_PRINCIPAL", "ldap.saslPrincipal");

        InfoObject ie = new InfoObject(
                "LDAP_GROUP_FILTER_DN",
                "OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de");
        HashMap<String, Integer> map = new HashMap<>();
        map.put("LDAP_GROUP_FILTER_DN", 0);
        List<InfoObject> ieList = new ArrayList<>();
        ieList.add(ie);

        LdapProperties props = new LdapProperties(ieList, map);
        instance = new LdapHelper();
        instance.setLdapProperties(props);
    }

    @Test
    public void filterGroupTest() {
        boolean b = instance.filterGroup(
                "CN=LBAC_User,OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de");
        Assert.assertTrue(b);

        b = instance.filterGroup(
                "CN=Irrelevant_Group,OU=SomeOU,DC=ipb-halle,DC=de");
        Assert.assertFalse(b);
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("LdapHelperTest.war")
                        .addClass(InfoObjectService.class));
    }

    private void saveInfo(String key, String value) {
        InfoObject ie = new InfoObject(
                key,
                value);
        ie.setACList(context.getOwnerAllPermACL());
        ie.setOwner(adminUser);
        infoService.save(ie);
    }
}
