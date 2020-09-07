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
package de.ipb_halle.lbac.file.save;

import de.ipb_halle.lbac.file.StemmedWordOrigin;
import de.ipb_halle.lbac.file.TermVector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class FileAnalyser {

    public String getLanguage() {
        return "en";
    }
    
    public void analyseFile(String location){
        
    }

    public List<StemmedWordOrigin> getWordOrigins() {
        return new ArrayList<>();
    }

    public List<TermVector> getTermVector() {
        return new ArrayList<>();
    }
}
