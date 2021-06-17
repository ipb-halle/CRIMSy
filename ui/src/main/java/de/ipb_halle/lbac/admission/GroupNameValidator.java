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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class GroupNameValidator {

    private MemberService memberService;

    public GroupNameValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    public boolean isGroupNameValide(String groupName) {
        if (groupName == null
                || groupName.toLowerCase().equals("public group")
                || groupName.toLowerCase().equals("admin group")) {
            return false;
        }
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", groupName);
        List<Group> loadedGroup = memberService.loadGroups(cmap);
        if (loadedGroup.isEmpty()) {
            return true;
        }
        return loadedGroup.get(0).getSubSystemType()
                != AdmissionSubSystemType.LOCAL;
    }
}
