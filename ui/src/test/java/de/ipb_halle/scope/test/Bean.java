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
package de.ipb_halle.scope.test;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@SessionScoped
public class Bean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String hello;

    @Inject
    private OtherBean bean;

    @Inject
    private ApplicationScopedBean applicationScopedBean;

    @PostConstruct
    public void init() {
        hello = "Hello World!";
    }

    @PreDestroy
    public void done() {
    }

    public String getHello() {
        return hello;
    }

    public OtherBean getBean() {
        return bean;
    }

    public ApplicationScopedBean getApplicationScopedBean() {
        return applicationScopedBean;
    }
}
