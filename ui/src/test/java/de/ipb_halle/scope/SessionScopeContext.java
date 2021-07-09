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

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;

/**
 * This CDI context overrides the built-in session scope of JavaEE. It can be
 * used in Java SE CDI applications. A special feature of this class is that its
 * cached bean instances can be reset.
 * <p>
 * This implementation was inspired by chapter 4 of the book
 * Pro CDI 2 in Java EE 8: An In-Depth Guide to Context and Dependency Injection
 * by Jan Beernink and Arjan Tijms
 * 2019, Apress, Berkeley, CA
 * ISBN 978-1-4842-4362-6
 * 
 * @author flange
 */
public class SessionScopeContext implements Context {
    /*
     * This instance needs to be static (i.e. a singleton) because the CDI event
     * on {@link SessionScopeResetEvent} always creates a new object of this
     * class.
     */
    private static final ConcurrentHashMap<Contextual<?>, BeanInstance<?>> instances = new ConcurrentHashMap<>();

    @Override
    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual,
            CreationalContext<T> creationalContext) {
        return (T) instances.computeIfAbsent(contextual,
                k -> new BeanInstance<>(contextual.create(creationalContext),
                        contextual, creationalContext))
                .getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual) {
        BeanInstance<T> instance = (BeanInstance<T>) instances.get(contextual);
        return instance == null ? null : instance.getInstance();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * Upon receiving a {@link SessionScopeResetEvent}, this method resets the
     * context by destroying and forgetting all cached beans.
     * 
     * @param event
     */
    public void reset(@Observes SessionScopeResetEvent event) {
        reset();
    }

    /**
     * Resets the context by destroying and forgetting all cached beans.
     */
    public synchronized void reset() {
        instances.forEach((k, v) -> v.destroy());
        instances.clear();
    }
}