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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.service.MemberService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@RequestScoped
@Named
public class GroupAgent {

    private Logger logger = LogManager.getLogger(GroupAgent.class);
    @Inject
    private MemberService memberService;
    private String name;
    private String institute;

    
    public void clear(){
        name=null;
        institute=null;
    }
    public List<Group> loadGroups() {
        Map<String, Object> cmap = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) {
            cmap.put("NAME", "%" + name + "%");
        }
        if (institute != null && !institute.trim().isEmpty()) {
            cmap.put("INSTITUTE", "%" + institute + "%");
        }

        return memberService.loadGroupsFuzzy(cmap);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

}
