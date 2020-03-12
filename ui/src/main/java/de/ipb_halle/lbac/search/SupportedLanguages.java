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
package de.ipb_halle.lbac.search;

/**
 *
 * @author fmauz
 */
public enum SupportedLanguages {
    GERMAN("de"),
    ENGLISH("en"),
    ESPANIOL("es"),
    PORTUGUESE("pt"),
    GALISISH("gl"),
    FRENCH("fr");

    private final String langKey;

    private SupportedLanguages(final String langKey) {
        this.langKey = langKey;
    }

    public String toString() {
        return langKey;
    }
}
