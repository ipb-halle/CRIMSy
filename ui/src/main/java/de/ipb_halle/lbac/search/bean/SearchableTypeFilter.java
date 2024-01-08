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
package de.ipb_halle.lbac.search.bean;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class SearchableTypeFilter {

    private boolean materials;
    private boolean items;
    private boolean experiments;
    private boolean projects;
    private boolean documents;
    
    List<String> types=new ArrayList<>();
    List<String> selectedTypes=new ArrayList<>();
    
    public SearchableTypeFilter(){
        types.add("materials");
        types.add("items");
        types.add("experiments");
        types.add("documents");
        types.add("projects");
    }
    
    public List<String> getTypes(){
        return types;
    }
    public void setTypes(List<String> types){
        this.types=types;
    }

    public List<String> getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(List<String> selectedTypes) {
        this.selectedTypes = selectedTypes;
    }
}
