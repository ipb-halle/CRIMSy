/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.globals;

import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import de.ipb_halle.lbac.util.WebXml;
import de.ipb_halle.lbac.util.WebXmlImpl;

/**
 * This bean class provides information on global locale settings.
 * 
 * @author flange
 */
@Named
@ApplicationScoped
public class GlobalLocaleBean {
    public static final String WEBXML_NUMBERCONVERTER_LOCALE = "de.ipb_halle.lbac.NumberConverterLocale";

    private WebXml webXml = new WebXmlImpl();

    private Locale numberConverterLocale;

    /**
     * default constructor
     */
    public GlobalLocaleBean() {
    }

    /**
     * test constructor with dependency injection
     * 
     * @param webXml
     */
    protected GlobalLocaleBean(WebXml webXml) {
        this.webXml = webXml;
    }

    @PostConstruct
    public void init() {
        initNumberConverterLocale();
    }

    private void initNumberConverterLocale() {
        numberConverterLocale = new Locale(
                webXml.getContextParam(WEBXML_NUMBERCONVERTER_LOCALE, "en"));
    }

    public Locale getNumberConverterLocale() {
        return numberConverterLocale;
    }
}