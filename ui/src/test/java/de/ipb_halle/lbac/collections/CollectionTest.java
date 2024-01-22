package de.ipb_halle.lbac.collections;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author swittche
 */
public class CollectionTest {
    
    
    @Test
    public void getBaseFolder(){
        Collection c = new Collection();
        c.setName("MyCollection");
        c.setBaseDirectory("target/test-classes/collections/");
        Assert.assertEquals("target/test-classes/collections/MyCollection",c.getBaseFolder());
    }
}
