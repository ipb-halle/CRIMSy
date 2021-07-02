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

import static org.junit.Assert.assertThrows;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.mock.CallBackControllerMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;

/**
 * 
 * @author flange
 */
@RunWith(Arquillian.class)
public class AccountValidatorTest extends TestBase {
    private FacesContext fc = null;
    private UIComponent comp = null;
    private MessagePresenterMock presenterMock = new MessagePresenterMock();

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("AccountValidatorTest.war");
    }

    @Test
    public void test001_validate() {
        UserMgrBean userMgrBean = new UserMgrBean(nodeService, memberService,
                membershipService, presenterMock, new CallBackControllerMock());
        AccountValidator validator = new AccountValidator(memberService,
                userMgrBean, presenterMock);

        validator.validate(fc, comp, null);
        validator.validate(fc, comp, "");

        // No relevant user in database, so this should work.
        validator.validate(fc, comp, "myuser");

        // Add a user.
        User user = createUser("myuser", "My User");

        // This user is not the active user in userMgrBean.
        assertThrows(ValidatorException.class,
                () -> validator.validate(fc, comp, "myuser"));

        userMgrBean.setUser(user);
        // Our user is now active user, so this should work.
        validator.validate(fc, comp, "myuser");
    }
}