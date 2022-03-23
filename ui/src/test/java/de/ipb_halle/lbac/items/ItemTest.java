package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

/**
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

        assertEquals("Room->Shelf->Bottle", item.getNestedLocation());
    }

    @Test
    public void test_isEqualTo() {
        Item item1 = new Item();
        item1.setId(42);

        assertFalse(item1.isEqualTo(null));
        assertFalse(item1.isEqualTo("not an Item object"));

        Item item2 = new Item();
        assertFalse(item1.isEqualTo(item2));
        assertFalse(item2.isEqualTo(item1));

        item2.setId(1);
        assertFalse(item1.isEqualTo(item2));
        assertFalse(item2.isEqualTo(item1));

        item2.setId(42);
        assertTrue(item1.isEqualTo(item2));
        assertTrue(item2.isEqualTo(item1));
    }

    @Test
    public void test_getAmountAsQuantity() {
        Item i = new Item();
        assertNull(i.getAmountAsQuantity());

        i.setAmount(42d);
        i.setUnit(null);
        assertNull(i.getAmountAsQuantity());

        i.setAmount(null);
        i.setUnit(Unit.getUnit("g"));
        assertNull(i.getAmountAsQuantity());

        i.setAmount(42d);
        i.setUnit(Unit.getUnit("g"));
        Quantity amount = i.getAmountAsQuantity();
        assertEquals(42d, amount.getValue(), 1e-3);
        assertEquals("g", amount.getUnit().toString());
    }
}
