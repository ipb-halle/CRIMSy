/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.termvector;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fbroda
 */
public class StemmedWordOriginTest {

    @Test
    public void test001_equals() {
        StemmedWordOrigin foo = new StemmedWordOrigin("foo", "foo");
        StemmedWordOrigin bar = new StemmedWordOrigin("bar", "bar");
        StemmedWordOrigin foobar = new StemmedWordOrigin("foobar", "foo");
        StemmedWordOrigin barfoo = new StemmedWordOrigin("foo", "foobar");
        StemmedWordOrigin foofoo = new StemmedWordOrigin("fo" + "o", "f" + "oo");
        Assert.assertFalse(foo.equals(null));
        Assert.assertFalse(foo.equals("foobar"));
        Assert.assertFalse(foo.equals(bar));
        Assert.assertFalse(foo.equals(foobar));
        Assert.assertFalse(foo.equals(barfoo));
        Assert.assertTrue(foo.equals(foofoo));

        Assert.assertFalse(foo.hashCode() == bar.hashCode());
        Assert.assertTrue(foo.hashCode() == foofoo.hashCode());
    }
}
