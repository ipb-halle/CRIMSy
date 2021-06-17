package org.tartarus.snowball;

import org.junit.Assert;
import org.junit.Test;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.germanStemmer;

public class SnowballStemmerTest {

    @Test
    public void testStem() {
        SnowballStemmer snowballStemmer = new germanStemmer();
        snowballStemmer.setCurrent("Korrektheit".toLowerCase());
        snowballStemmer.stem();
        String result = snowballStemmer.getCurrent();

        Assert.assertEquals("korrekt", result);
    }
}
