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
package de.ipb_halle.lbac.material;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.i18n.UIMessage;

/**
 *
 * @author fmauz
 */
public class JsfMessagePresenter implements MessagePresenter {

    private static JsfMessagePresenter instance;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private JsfMessagePresenter() {

    }

    @Override
    public void error(String message, Object... args) {
        UIMessage.error(null, MESSAGE_BUNDLE, message, args);
    }

    @Override
    public void info(String message, Object... args) {
        UIMessage.info(null, MESSAGE_BUNDLE, message, args);
    }

    public static synchronized MessagePresenter getInstance() {
        if (instance == null) {
            instance = new JsfMessagePresenter();
        }
        return instance;
    }

    @Override
    public String presentMessage(String messageKey, Object... args) {
          return Messages.getString(MESSAGE_BUNDLE, messageKey, args);
    }
}
