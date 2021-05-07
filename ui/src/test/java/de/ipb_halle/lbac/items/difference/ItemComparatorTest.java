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
package de.ipb_halle.lbac.items.difference;

import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.bean.history.ItemComparator;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.project.Project;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemComparatorTest {

    @Test
    public void test001_calculateDifference() {
        User user1 = new User();
        user1.setId(1);
        User user2 = new User();
        user2.setId(2);
        Project project1 = new Project();
        Project project2 = new Project();
        project2.setId(2);

        Item item1 = new Item();
        item1.setAmount(22d);
        item1.setConcentration(33d);
        item1.setDescription("Description-Item");
        item1.setId(1);
        item1.setOwner(user1);
        item1.setProject(project1);
        item1.setPurity("pure");
        item1.setcTime(new Date());

        Item item2 = new Item();
        item2.setAmount(22d);
        item2.setConcentration(33d);
        item2.setDescription("Description-Item");
        item2.setId(2);
        item2.setOwner(user1);
        item2.setProject(project1);
        item2.setPurity("pure");
        item2.setcTime(new Date());

        ItemComparator comparator = new ItemComparator();
        ItemHistory history = comparator.compareItems(item1, item2, user1);
        Assert.assertNull("test001: same items must have no difference", history);

        //Change description
        item2.setDescription("Description-Item edited");
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 description was changed", history);
        Assert.assertEquals("test001 old Description of must be 'Description-Item'", "Description-Item", history.getDescriptionOld());
        Assert.assertEquals("test001 new Description of must be 'Description-Item edited'", "Description-Item edited", history.getDescriptionNew());

        // Change Amount
        item2.setDescription("Description-Item");
        item2.setAmount(01d);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 amount was changed", history);
        Assert.assertEquals("test001 old amount of must be 22", 22d, (double) history.getAmountOld(), 0);
        Assert.assertEquals("test001 new amount of must be 1", 1d, (double) history.getAmountNew(), 0);

        // Change Concentration
        item2.setAmount(22d);
        item2.setConcentration(null);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 amount was changed", history);
        Assert.assertEquals("test001 old concentration of must be 33", 33d, (double) history.getConcentrationOld(), 0);
        Assert.assertNull(history.getConcentrationNew());

        item1.setConcentration(null);
        item2.setConcentration(33d);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 amount was changed", history);
        Assert.assertEquals("test001 old concentration of must be 33", 33d, (double) history.getConcentrationNew(), 0);
        Assert.assertNull(history.getConcentrationOld());
        
        item1.setConcentration(33d);
        item2.setConcentration(17d);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 concentration was changed", history);
        Assert.assertEquals("test001 old concentration of must be 33", 33d, (double) history.getConcentrationOld(), 0);
        Assert.assertEquals("test001 new concentration of must be 17", 17d, (double) history.getConcentrationNew(), 0);

        //Change Owner
        item2.setConcentration(33d);
        item2.setOwner(user2);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 user was changed", history);
        Assert.assertEquals("test001 old user  must be user1", user1.getId(), history.getOwnerOld().getId());
        Assert.assertEquals("test001 new user must be user2", user2.getId(), history.getOwnerNew().getId());

        //Change project
        item2.setOwner(user1);
        item2.setProject(project2);
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 project was changed", history);
        Assert.assertEquals("test001 old project  must be user1", project1.getId(), history.getProjectOld().getId());
        Assert.assertEquals("test001 new project must be user2", project2.getId(), history.getProjectNew().getId());

        //Change Purity
        item2.setProject(project1);
        item2.setPurity("purest");
        history = comparator.compareItems(item1, item2, user1);
        Assert.assertNotNull("test001 project was changed", history);
        Assert.assertEquals("test001 old purity  must be 'pure'", "purest", history.getPurityNew());
        Assert.assertEquals("test001 new purity must be 'purest'", "pure", history.getPurityOld());
    }
}
