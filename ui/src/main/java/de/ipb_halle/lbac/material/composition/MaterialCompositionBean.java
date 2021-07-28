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
package de.ipb_halle.lbac.material.composition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialCompositionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    private CompositionType choosenType = CompositionType.EXTRACT;

    public List<CompositionType> getCompositionTypes() {
        logger.info("got values");
        return Arrays.asList(CompositionType.values());
    }

    public CompositionType getChoosenType() {
        return choosenType;
    }

    public void setChoosenType(CompositionType choosenType) {
        this.choosenType = choosenType;
    }

}
