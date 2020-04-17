/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemHistory;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemStateTest {

    @Test
    public void test001_getPreviousAndFollowingKeyTest() {
        Calendar cal = Calendar.getInstance();
        ItemState state = new ItemState();
        Item item = new Item();
        item.getHistory().put(createHistory(2020).getMdate(), createHistory(2020));
        item.getHistory().put(createHistory(2021).getMdate(), createHistory(2021));
        item.getHistory().put(createHistory(2022).getMdate(), createHistory(2022));
        item.getHistory().put(createHistory(2023).getMdate(), createHistory(2023));
        state.setEditedItem(item);

        Assert.assertNull(state.getCurrentHistoryDate());
        cal.setTime(state.getPreviousKey(null));
        Assert.assertEquals(2023, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2022, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2021, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2020, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2021, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2022, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2023, cal.get(1));

        Assert.assertNull(state.getFollowingKey(cal.getTime()));

    }

    private ItemHistory createHistory(int year) {
        Calendar cal = Calendar.getInstance();
        ItemHistory history = new ItemHistory();
        cal.set(year, 4, 0);
        history.setMdate(cal.getTime());
        return history;
    }
}
