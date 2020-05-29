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
package de.ipb_halle.lbac.items.bean;

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

    @Inject
    private ContainerService service;

    @Inject
    private UserBean userBean;

    private List<Container> container = new ArrayList<>();

    private List<ContainerType> blackList = new ArrayList<>();

    public List<Container> getContainer() {
        container = service.loadContainers(userBean.getCurrentAccount());
        for (int i = container.size() - 1; i >= 0; i--) {
            if (blackList.contains(container.get(i).getType())) {
                container.remove(i);
            }
        }
        return container;
    }

    public String getDimensionString(Container c) {
        if (c.getDimension() == null) {
            return "-";
        } else {
            String[] d = c.getDimension().split(";");

            if (d[2].equals("1")) {
                return d[0] + " x " + d[1];
            } else {
                return d[0] + " x " + d[1] + " x " + d[2];
            }
        }

    }
}
