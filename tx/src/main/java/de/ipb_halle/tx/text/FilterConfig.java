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

import java.io.Serializable;

public class FilterConfig implements Serializable {
    
    private final static long serialVersionUID = 1L;

    private FilterConfig[][]    filterChains;
    private String              filterConfig;
    private String              filterType;

    /**
     * default constructor
     */
    public FilterConfig() {
    }

    public FilterConfig(String type, String config, FilterConfig[][] chains) {
        this.filterType = type;
        this.filterConfig = config;
        this.filterChains = chains;
    }

    public FilterConfig[][] getChains() {
        return this.filterChains;
    }

    public String getConfig() {
        return this.filterConfig;
    }

    public String getType() {
        return this.filterType;
    }

    public void setChains(FilterConfig[][] chains) {
        this.filterChains = chains;
    }

    public void setConfig(String config) {
        this.filterConfig = config;
    }

    public void setType(String type) {
        this.filterType = type;
    }
}
