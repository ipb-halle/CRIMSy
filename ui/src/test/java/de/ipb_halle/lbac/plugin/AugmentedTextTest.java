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
package de.ipb_halle.lbac.plugin;

import de.ipb_halle.lbac.plugin.UIAugmentedText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class AugmentedTextTest {
    
    
    @Test
    public void test001_regex(){
         Pattern pattern = Pattern.compile(UIAugmentedText.LINKTEXT_PATTERN);
         
         String text="Das ist ein Test #Benzol. #wasserflasche ";
         String[] results = {"#Benzol.", "#wasserflasche "};
        int start = 0;
        int end = 0;
        int i = 0;
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            Assert.assertEquals(results[i], text.substring(start, end));
            i++;
         }
        Assert.assertEquals(2, i);
        
        
    }
}
