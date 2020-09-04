/*
 * Text eXtractor
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
package de.ipb_halle.tx.text;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.*;

public class StemmerFactory {

    /**
     * @param lanCode ISO639 language code; currently limited to (en, de, fr, es, pt, it)
     * @return a stemmer according to the language code or null if no stemmer is available
     */
    public static SnowballStemmer getStemmer(String langCode) {
        switch(langCode) {
            case "en" : return new englishStemmer();
            case "de" : return new germanStemmer();
            case "fr" : return new frenchStemmer();
            case "es" : return new spanishStemmer();
            case "pt" : return new portugueseStemmer();
            case "it" : return new italianStemmer();
        }
        return null;
    }
}
