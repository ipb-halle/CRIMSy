package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author swittche
 */
public class CollectionTest {

    private final static int USER_ID = -9999;
    private final static int ACLIST_ID = -8888;
    private final static int COLLECTION_ID = -7777;
    private final static String COLLECTION_NAME = "MyCollection";
    private final static String COLLECTION_PATH = "must_match";
    private final static String COLLECTION_DESCRIPTION = "none";

    @Test
    public void testEntityCreation(){
        User owner = new User();
        owner.setId(USER_ID);
        owner.setName("Nobody");

        ACList acList = new ACList();
        acList.setId(ACLIST_ID);
        Collection c = new Collection();
        c.setId(COLLECTION_ID);
        c.setName(COLLECTION_NAME);
        c.setDescription(COLLECTION_DESCRIPTION);
        c.setStoragePath(COLLECTION_PATH);
        c.setOwner(owner);
        c.setACList(acList);

        CollectionEntity ce = c.createEntity();
        Assertions.assertEquals(COLLECTION_ID, ce.getId());
        Assertions.assertEquals(USER_ID, ce.getOwner());
        Assertions.assertEquals(ACLIST_ID, ce.getACList());
        Assertions.assertEquals(COLLECTION_DESCRIPTION, ce.getDescription());
        Assertions.assertEquals(COLLECTION_NAME, ce.getName());
        Assertions.assertEquals(COLLECTION_PATH, ce.getStoragePath());
    }
}
