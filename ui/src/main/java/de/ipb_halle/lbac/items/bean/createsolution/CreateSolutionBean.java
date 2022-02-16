/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.createsolution;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * 
 * @author flange
 */
@SessionScoped
@Named
public class CreateSolutionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ItemService itemService;

    @Inject
    private transient MessagePresenter messagePresenter;

    private ConsumePartOfItemStrategyController consumePartOfItemStrategyController;

    @PostConstruct
    public void init() {
    }

    /*
     * Actions
     */
    public void actionStartCreateSolution(Item item) {
        consumePartOfItemStrategyController = new ConsumePartOfItemStrategyController(item, itemService, messagePresenter);
    }

    /*
     * Getters/setters
     */
    public ConsumePartOfItemStrategyController getConsumePartOfItemStrategyController() {
        return consumePartOfItemStrategyController;
    }
}