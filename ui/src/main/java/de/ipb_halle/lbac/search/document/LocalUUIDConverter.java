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
package de.ipb_halle.lbac.search.document;

import java.util.UUID;

import org.apache.johnzon.mapper.Converter;

/**
 * implements a JSON converter for UUID class (johnzon mapper )
 * used in all entity classes with data type UUID
 * annotation
 * example: @JohnzonConverter(LocalUUIDConverter.class)
 *          private UUID ...;
 */

public class LocalUUIDConverter implements Converter<UUID> {
    @Override
    public String toString(UUID uuid) {
        return uuid.toString();
    }

    @Override
    public UUID fromString(String s) {
        return UUID.fromString(s);
    }
}
