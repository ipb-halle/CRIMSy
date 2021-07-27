/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class UserBeanTest extends TestBase {

    private static final long serialVersionUID = 1L;
    @Inject
    private InfoObjectService infoService;
    private static final String LOGIN_CUSTOM_TEXT = "SETTING_LOGIN_CUSTOM_TEXT";

    @Test
    public void test001_getCustomLogInInfo() {
        UserBean bean = new UserBean();
        bean.infoObjectService = infoService;

        Assert.assertEquals("", bean.getCustomLogInInfo());
        InfoObject infoObject = new InfoObject(LOGIN_CUSTOM_TEXT, "A custom text at login");
        infoObject.setACList(acListReadable);
        infoObject.setOwner(adminUser);
        infoService.save(infoObject);
        Assert.assertEquals("A custom text at login", bean.getCustomLogInInfo());

        entityManagerService.doSqlUpdate("DELETE FROM info WHERE key='" + LOGIN_CUSTOM_TEXT + "'");
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("UserBeanTest.war"));
    }

}
