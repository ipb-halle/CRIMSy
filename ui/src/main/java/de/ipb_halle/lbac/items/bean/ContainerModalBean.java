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
package de.ipb_halle.lbac.items.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerModalBean implements Serializable {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    @Inject
    private ContainerService service;

    @Inject
    private UserBean userBean;

    private List<Container> container = new ArrayList<>();

    private List<ContainerType> blackList = new ArrayList<>();

    public List<Container> getContainer() {
        container = service.loadContainersWithoutItems(userBean.getCurrentAccount());

        for (int i = container.size() - 1; i >= 0; i--) {
            if (blackList.contains(container.get(i).getType())) {
                container.remove(i);
            }
        }

        for (Container c : container) {
            c.getType().setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + c.getType().getName(), null));
            for (Container c2 : c.getContainerHierarchy()) {
                c2.getType().setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + c2.getType().getName(), null));
            }
        }
        return container;
    }

    public String getDimensionString(Container c) {
        if (c.getItems() != null) {
            return String.format("%d x %d", c.getItems().length, c.getItems()[0].length);
        }
        return "-";
    }
}
