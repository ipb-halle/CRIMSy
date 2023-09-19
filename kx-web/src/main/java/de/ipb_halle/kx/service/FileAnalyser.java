/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.service;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.tx.text.LanguageDetectorFilter;
import de.ipb_halle.tx.text.ParseTool;
import de.ipb_halle.tx.text.TermVectorFilter;
import de.ipb_halle.tx.text.properties.Language;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class FileAnalyser {

    protected ParseTool parseTool = new ParseTool();
    protected InputStream filterDefinition;
    private Logger logger = LogManager.getLogger(this.getClass());

    public FileAnalyser(InputStream filterDefinition) {
        this.filterDefinition = filterDefinition;
    }

    private String getLanguage() {
        @SuppressWarnings("unchecked")
        SortedSet<Language> languages = (SortedSet) this.parseTool
                .getFilterData()
                .getValue(LanguageDetectorFilter.LANGUAGE_PROP);
        Map<String, Integer> countMap = new HashMap<>();
        int maxCount = 0;
        String maxLang = null;
        for (Language lang : languages) {
            String langString = lang.getLanguage();
            int count = lang.getEnd() - lang.getStart();
            Integer totalCount = countMap.get(langString);
            if (totalCount != null) {
                totalCount += count;
            } else {
                totalCount = count;
            }
            if (totalCount > maxCount) {
                maxCount = totalCount;
                maxLang = langString;
            }
            countMap.put(langString, totalCount);
        }

        return (maxLang != null) ? maxLang : "undefined";
    }

    protected void analyseFile(InputStream file){
        parseTool.setFilterDefinition(filterDefinition);
        parseTool.setInputStream(file);
        parseTool.initFilter();
        parseTool.parse();
    }

    protected List<StemmedWordOrigin> getWordOrigins() {
        List<StemmedWordOrigin> wordOrigins = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Set<String>> map = (Map) parseTool.getFilterData().getValue(TermVectorFilter.STEM_DICT);
        for (String s : map.keySet()) {
            wordOrigins.add(new StemmedWordOrigin(s, map.get(s)));
        }
        return wordOrigins;
    }

    protected List<TermVector> getTermVector(Integer fileId) {
        List<TermVector> termVectors = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Integer> termvectorMap = (Map) parseTool.getFilterData().getValue(TermVectorFilter.TERM_VECTOR);
        for (String tv : termvectorMap.keySet()) {
            termVectors.add(new TermVector(tv, fileId, termvectorMap.get(tv)));
        }
        return termVectors;
    }

    public void processFile(Integer fileId) {
        try {
            FileObject fileObj = fileObjectService.loadFileObjectById(fileId);
            FileInputStream is = new FileInputStream(fileObj.getFileLocation());
            analyseFile(is);
            saveResults(fileObj);
        } catch (Exception e) {
            
        }
    }

    private void saveResults(FileObject fileObj) {
        saveTermVector();
        saveWordOrigins();
        updateLanguage(fileObj);
    }
    
    private void saveTermVector() {
        
    }

    private void saveWordOrigins() {
        
    }

    private void updateLanguage(FileObject fileObj) {
        fileObj.setDocumentLanguage(getLanguage());
        fileObjectService.save(fileObj);
    }
}
