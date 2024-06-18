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
package de.ipb_halle.scope;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * This class registers a {@link SessionScopeContext} instance as context
 * object for the {@link SessionScoped} scope. This {@link Extension} has to be
 * registered via its fully qualified class name in
 * META-INF/services/jakarta.enterprise.inject.spi.Extension.
 * <p>
 * This implementation was inspired by chapter 4 of the book
 * Pro CDI 2 in Java EE 8: An In-Depth Guide to Context and Dependency Injection
 * by Jan Beernink and Arjan Tijms
 * 2019, Apress, Berkeley, CA
 * ISBN 978-1-4842-4362-6
 * 
 * @author flange
 */
public class SessionScopeExtension implements Extension {
    /**
     * Registers the scope {@link SessionScoped} before bean discovery starts.
     * 
     * @param beforeBeanDiscovery
     */
    public void addScope(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        beforeBeanDiscovery.addScope(SessionScoped.class, true, false);
    }

    /**
     * Adds an instance of {@link SessionScopeContext} as custom CDI context
     * after bean discovery finishes.
     * 
     * @param afterBeanDiscovery
     */
    public void registerContext(
            @Observes AfterBeanDiscovery afterBeanDiscovery) {
        afterBeanDiscovery.addContext(new SessionScopeContext());
    }
}
