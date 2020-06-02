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

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.container.service.ContainerService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerOverviewBean implements Serializable {
    
    public enum Mode {
        SHOW, EDIT, CREATE
    }
    
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    
    Logger logger = LogManager.getLogger(this.getClass().getName());
    
    private User currentUser;
    private List<Container> readableContainer = new ArrayList<>();
    
    @Inject
    private ContainerSearchMaskBean searchMask;
    
    @Inject
    private ContainerService containerService;
    
    @Inject
    private ContainerSearchMaskBean searchMaskBean;
    
    private Mode mode;
    
    public List<Container> getReadableContainer() {
        return readableContainer;
    }
    
    public void setReadableContainer(List<Container> readableContainer) {
        this.readableContainer = readableContainer;
    }
    
    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        readableContainer = containerService.loadContainers(currentUser);
        mode = Mode.SHOW;
    }
    
    public void actionCancel() {
        
    }
    
    public void actionSecondButtonClick() {
        if (mode == Mode.SHOW) {
            mode = Mode.CREATE;
        }
        else if (mode == Mode.CREATE) {
            mode = Mode.SHOW;
            saveNewContainer();
            
        }
        else if (mode == Mode.EDIT) {
            mode = Mode.SHOW;
            saveEditedContainer();
        }
        logger.info("Mode " + mode);
        
    }
    
    private void saveNewContainer() {
        actionStartFilteredSearch();
    }
    
    private void saveEditedContainer() {
        actionStartFilteredSearch();
    }
    
    public void actionStartFilteredSearch() {
        
        Map<String, Object> cmap = new HashMap<>();
        if (searchMaskBean.getContainerSearchName() != null && !searchMaskBean.getContainerSearchName().trim().isEmpty()) {
            cmap.put("label", searchMaskBean.getContainerSearchName());
        }
        if (searchMaskBean.getContainerSearchId() != null && !searchMaskBean.getContainerSearchId().trim().isEmpty()) {
            cmap.put("id", Integer.valueOf(searchMaskBean.getContainerSearchId()));
        }
        if (searchMaskBean.getSearchProject() != null && !searchMaskBean.getSearchProject().trim().isEmpty()) {
            cmap.put("project", searchMaskBean.getSearchProject());
        }
        if (searchMaskBean.getSearchLocation() != null && !searchMaskBean.getSearchLocation().trim().isEmpty()) {
            cmap.put("location", searchMaskBean.getSearchLocation());
        }
        readableContainer = containerService.loadContainers(currentUser, cmap);
    }
    
    public boolean isFirstButtonVisible() {
        return mode != Mode.SHOW;
    }
    
    public String getSecondButtonLabel() {
        if (mode == Mode.SHOW) {
            return Messages.getString(MESSAGE_BUNDLE, "container_button_create", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "container_button_save", null);
        }
    }
    
}
