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

import java.util.Map;
import java.util.HashMap;

public class FilterData {

    private Map<String, Object> data;
    
    public FilterData() {
        this.data = new HashMap<String, Object> ();
    }
    
    /**
     * obtain data
     */
    public Object getValue(String key) {
        return this.data.get(key);
    }

    /**
     * @param cfg config
     */
    public void setValue(String key, Object value) {
        this.data.put(key, value);
    }
}
