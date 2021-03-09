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
package de.ipb_halle.lbac.admission.mock;

import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;

/**
 * A mock class for {@link FacesContext}.
 * 
 * @author flange
 */
public class FacesContextMock extends FacesContextWrapper {
    private final FacesContext mockedFacesContext;

    /**
     * Use this constructor to inject a mocked object of {@link FacesContext}
     * that drives the behaviour of this {@link FacesContext} instance.
     * 
     * @param mockedFacesContext
     */
    public FacesContextMock(FacesContext mockedFacesContext) {
        this.mockedFacesContext = mockedFacesContext;
    }

    @Override
    public FacesContext getWrapped() {
        return mockedFacesContext;
    }

    /**
     * Set the {@link FacesContext} instance for the current thread. This static
     * method is needed because
     * {@link FacesContext#setCurrentInstance(FacesContext)} is protected.
     * 
     * @param context {@link FacesContext} instance for the current thread or
     *                null.
     */
    public static void setCurrentInstance(FacesContext context) {
        FacesContext.setCurrentInstance(context);
    }
}