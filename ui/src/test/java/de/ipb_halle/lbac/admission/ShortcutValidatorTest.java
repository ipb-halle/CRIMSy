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
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.mock.CallBackControllerMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * 
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ShortcutValidatorTest extends TestBase {
    private FacesContext fc = null;
    private UIComponent comp = null;
    private MessagePresenterMock presenterMock = MessagePresenterMock.getInstance();

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ShortcutValidatorTest.war");
    }

    @Test
    public void test001_validate() {
        UserMgrBean userMgrBean = new UserMgrBean(nodeService, memberService,
                membershipService, presenterMock, new CallBackControllerMock());
        ShortcutValidator validator = new ShortcutValidator(memberService,
                userMgrBean, presenterMock);

        validator.validate(fc, comp, null);
        validator.validate(fc, comp, "");

        // Wrong patterns
        assertThrows(ValidatorException.class,
                () -> validator.validate(fc, comp, "A2B"));
        assertThrows(ValidatorException.class,
                () -> validator.validate(fc, comp, "A B"));

        // No relevant user in database, so this should work.
        validator.validate(fc, comp, "ghi");

        // Add a user.
        User user1 = createUser("myuser1", "My User 1");
        user1.setShortcut("gHI");
        user1 = memberService.save(user1);

        // This user is not the active user in userMgrBean.
        assertThrows(ValidatorException.class,
                () -> validator.validate(fc, comp, "ghI"));

        userMgrBean.setUser(user1);
        // Our user is now active user, so this should work.
        validator.validate(fc, comp, "Ghi");

        // Add a user without shortcut
        createUser("myuser2", "My User 2");

        validator.validate(fc, comp, "");

        // Add a user with empty shortcut.
        User user3 = createUser("myuser3", "My User 3");
        user3.setShortcut("");
        user3 = memberService.save(user3);

        validator.validate(fc, comp, "");
    }
}