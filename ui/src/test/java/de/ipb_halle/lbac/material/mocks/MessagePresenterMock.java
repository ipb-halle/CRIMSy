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
package de.ipb_halle.lbac.material.mocks;

import de.ipb_halle.lbac.material.MessagePresenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;


/**
 *
 * @author fmauz
 */
@ApplicationScoped
@Any
public class MessagePresenterMock implements MessagePresenter, Serializable {

    private final static long serialVersionUID = 1L;

    private List<String> errorMessages = new ArrayList<>();
    private List<String> infoMessages = new ArrayList<>();

    private int instanceNumber = 0;
    private static int instanceCounter = 0;

    @Override
    public String presentMessage(String messageKey, Object... args) {
        return messageKey;
    }

    @Override
    public void error(String message, Object... args) {
        errorMessages.add(message);
    }

    @Override
    public void info(String message, Object... args) {
        infoMessages.add(message);
    }

    /**
     * 
     * @return the last error message or null if there were no error messages
     */
    public String getLastErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(errorMessages.size() - 1);
    }

    /**
     * 
     * @return the last info message or null if there were no info messages
     */
    public String getLastInfoMessage() {
        return infoMessages.isEmpty() ? null : infoMessages.get(infoMessages.size() - 1);
    }

    public void resetMessages() {
        errorMessages.clear();
        infoMessages.clear();
    }
}
