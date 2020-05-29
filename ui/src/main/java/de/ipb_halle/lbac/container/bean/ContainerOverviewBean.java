/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.container.bean;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.container.service.ContainerService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerOverviewBean implements Serializable {

    private User currentUser;
    private List<Container> readableContainer = new ArrayList<>();

    @Inject
    private ContainerSearchMaskBean searchMask;

    @Inject
    private ContainerService containerService;

    public void reloadContainer(){
        
    }
    
    public List<Container> getReadableContainer() {
        return readableContainer;
    }

    public void setReadableContainer(List<Container> readableContainer) {
        this.readableContainer = readableContainer;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        readableContainer = containerService.loadContainers(currentUser);
    }

    public void actionCancel() {

    }

    public void actionStartCreation() {

    }

}
