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
package de.ipb_halle.lbac.material.common.bean;

import java.io.Serializable;

/**
 * Manages the presentation of chunks of the total possible results for a
 * datatable
 *
 * @author fmauz
 */
public class DataTableNavigationController implements Serializable{

    private final int CHUNK_SIZE = 10;
    private int firstResult;
    private int maxResultsAvailable;
    private final String noResultsFoundPattern;
    private final String resultsFoundPattern;
    private final TableController controller;

    /**
     * The controller parameter determines the loading proceduce
     *
     * @param controller
     * @param noResultsFoundPattern
     * @param resultsFoundPattern
     * @param maxResults
     */
    public DataTableNavigationController(
            TableController controller,
            String noResultsFoundPattern,
            String resultsFoundPattern,
            int maxResults) {
        this.controller = controller;
        this.noResultsFoundPattern = noResultsFoundPattern;
        this.resultsFoundPattern = resultsFoundPattern;
        this.maxResultsAvailable = maxResults;
        this.firstResult = 0;
    }

    /**
     * Sets the shown chunk to the first possible chunk
     */
    public void actionFirstResult() {
        firstResult = 0;
        controller.reloadDataTableItems();
    }

    /**
     * Sets the shown chunk to the last possible chunk
     */
    public void actionLastResult() {
        firstResult = maxResultsAvailable - CHUNK_SIZE;
        firstResult = Math.max(0, firstResult);
        controller.reloadDataTableItems();
    }

    /**
     * Sets the chunk to the next one
     */
    public void actionNextResults() {
        firstResult += CHUNK_SIZE;
        firstResult = Math.min(firstResult, maxResultsAvailable - CHUNK_SIZE);
        controller.reloadDataTableItems();
    }

    /**
     * Sets the chunk to the last one
     */
    public void actionPriorResults() {
        firstResult -= CHUNK_SIZE;
        firstResult = Math.max(0, firstResult);
        controller.reloadDataTableItems();
    }

    /**
     *
     * @return
     */
    public int getCHUNK_SIZE() {
        return CHUNK_SIZE;
    }

    /**
     * returns the index of the first object of the current chunk
     *
     * @return
     */
    public int getFirstResult() {
        return firstResult;
    }

    public void setMaxResults(int maxResultsAvailable) {
        this.maxResultsAvailable = maxResultsAvailable;
    }

    public String getNavigationInfos() {
        int leftBorder = firstResult + 1;
        int rightBorder = (int) Math.min(CHUNK_SIZE + firstResult, maxResultsAvailable);
        if (maxResultsAvailable > 0) {
            return String.format(resultsFoundPattern, leftBorder, rightBorder, maxResultsAvailable);
        } else {
            return noResultsFoundPattern;
        }
    }

    public boolean isNextButtonGroupDisabled() {
        return (maxResultsAvailable - firstResult) <= CHUNK_SIZE;
    }

    public boolean isPriorButtonGroupDisabled() {
        return firstResult == 0;
    }

}
