package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.container.Container;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemTest {

    @Test
    public void test001_getNestedLocation() {
        Container room = new Container();
        room.setLabel("Room");
        Container shelf = new Container();
        shelf.getContainerHierarchy().add(room);
        shelf.setLabel("Shelf");
        Container bottle = new Container();
        bottle.setLabel("Bottle");
        bottle.getContainerHierarchy().add(shelf);
        bottle.getContainerHierarchy().add(room);
        Item item = new Item();
        item.setContainer(bottle);
        item.getNestedContainer().add(shelf);
        item.getNestedContainer().add(room);
        item.getNestedLocation();

        Assert.assertEquals("Room->Shelf->Bottle", item.getNestedLocation());
    }

}
