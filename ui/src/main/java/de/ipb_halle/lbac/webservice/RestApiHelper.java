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
package de.ipb_halle.lbac.webservice;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;

/**
 * some rest api helper functions
 */
public class RestApiHelper {

    /**
     * Gets rest api path from its resource class
     *
     * @param clazz
     * @return String rest api path
     */
    public static String getRestApiPath(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
                .filter(a -> a instanceof Path )
                .map(Path.class::cast)
                .map(Path::value)
                .collect(Collectors.joining());
    }

    public static String getRestApiApplicationPath(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
                .filter(a -> a instanceof ApplicationPath)
                .map(ApplicationPath.class::cast)
                .map(ApplicationPath::value)
                .collect(Collectors.joining());
    }

    public static String getRestApiDefaultPath(Class<?> clazz){
        return getRestApiApplicationPath(ApplicationConfig.class) + getRestApiPath(clazz);
    }
}
