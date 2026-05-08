/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 * 
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.tx.dict;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fblocal
 */
public class BloomFilterTest {

    public final static String HELLO_WORLD = "Hello World!";
    public final static String HALLO_WELT = "Hallo Welt!";
    public final static String HAVE_A_NICE_DAY = "Have a nice day!";
    public final static String GOOD_BYE = "Good bye!";
    public final static String FILTER = "12, 3, {15, 357, 373, 1448, 2294, 2689}";

    @Test
    public void testFilter() {
        BloomFilter filter = BloomFilter.getBloomFilter(12, 3);
        Assertions.assertEquals("12, 3, {}", filter.toString());
        
        filter.addValue(HELLO_WORLD.getBytes());
        filter.addValue(HALLO_WELT.getBytes());
        Assertions.assertTrue(filter.checkValue(HELLO_WORLD));
        Assertions.assertTrue(filter.checkValue(HALLO_WELT));
        Assertions.assertFalse(filter.checkValue(HAVE_A_NICE_DAY));
        Assertions.assertFalse(filter.checkValue(GOOD_BYE));
        
        filter = BloomFilter.fromString(FILTER);
        Assertions.assertTrue(filter.checkValue(HELLO_WORLD));
        Assertions.assertTrue(filter.checkValue(HAVE_A_NICE_DAY));
        Assertions.assertFalse(filter.checkValue(HALLO_WELT));
        Assertions.assertFalse(filter.checkValue(GOOD_BYE));
    }
}
