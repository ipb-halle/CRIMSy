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
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GroupMgrBeanTest extends TestBase {

    private GroupMgrBean groupMgrBean;

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("GroupMgrBeanTest.war"));
    }

    @Test
    public void test01_createNewGroup() {
        MessagePresenterMock presenterMock = new MessagePresenterMock();
        groupMgrBean = new GroupMgrBean(
                nodeService,
                memberService,
                membershipService,
                presenterMock);

        int groupsBeforeCreation = memberService.loadGroups(new HashMap()).size();
        //Try to create group with invalide name
        groupMgrBean.actionCreate();
        Assert.assertEquals("groupMgr_no_valide_name", presenterMock.getLastErrorMessage());
        presenterMock.reseLastMessages();

        //Try to create group with valide name
        groupMgrBean.getGroup().setName(("GroupMgrBeanTest:test01_createNewGroup"));
        groupMgrBean.actionCreate();
        Group g = loadGroupByName("GroupMgrBeanTest:test01_createNewGroup");
        Assert.assertNotNull(g);
        Assert.assertEquals(groupsBeforeCreation + 1, memberService.loadGroups(new HashMap()).size());

        //Try to create the same group again -' should not create the group
        groupMgrBean.getGroup().setName(("GroupMgrBeanTest:test01_createNewGroup"));
        groupMgrBean.actionCreate();
        Assert.assertEquals("groupMgr_no_valide_name", presenterMock.getLastErrorMessage());
        Assert.assertEquals(groupsBeforeCreation + 1, memberService.loadGroups(new HashMap()).size());

        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id=" + g.getId());

    }

    private Group loadGroupByName(String groupName) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", groupName);
        List<Group> loadedGroup = memberService.loadGroups(cmap);
        if (!loadedGroup.isEmpty()) {
            return loadedGroup.get(0);
        } else {
            return null;
        }
    }

}
