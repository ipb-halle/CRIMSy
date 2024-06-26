/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

/**
 * Utility class for coping with resource files.
 * 
 * @author flange
 */
public class ResourceUtils {
    private ResourceUtils() {
    }

    public static String readResourceFile(String filename) throws IOException {
        return IOUtils.toString(inputStreamForResourceFile(filename));
    }

    public static Reader readerForResourceFile(String filename) {
        return new InputStreamReader(inputStreamForResourceFile(filename));
    }

    public static InputStream inputStreamForResourceFile(String filename) {
        return ResourceUtils.class.getClassLoader().getResourceAsStream(filename);
    }
}
