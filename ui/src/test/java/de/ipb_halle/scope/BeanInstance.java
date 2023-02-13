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

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

/**
 * Wrapper class that holds the bean instance and its creational context.
 * <p>
 * This implementation was inspired by chapter 4 of the book
 * Pro CDI 2 in Java EE 8: An In-Depth Guide to Context and Dependency Injection
 * by Jan Beernink and Arjan Tijms
 * 2019, Apress, Berkeley, CA
 * ISBN 978-1-4842-4362-6
 * 
 * @author flange
 * @param <T> bean type
 */
public class BeanInstance<T> {
    private final T instance;
    private final Contextual<T> contextual;
    private final CreationalContext<T> creationalContext;

    public BeanInstance(T instance, Contextual<T> contextual,
            CreationalContext<T> creationalContext) {
        this.instance = instance;
        this.contextual = contextual;
        this.creationalContext = creationalContext;
    }

    /**
     * Returns the wrapped bean instance.
     * 
     * @return bean instance
     */
    public T getInstance() {
        return instance;
    }

    /**
     * Destroys the wrapped bean instance.
     */
    public void destroy() {
        contextual.destroy(instance, creationalContext);
    }
}
