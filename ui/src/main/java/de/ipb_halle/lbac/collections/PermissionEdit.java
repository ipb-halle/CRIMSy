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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.entity.ACEntry;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Group;
import java.util.List;

/**
 * Defines the api for the modal of editing an ac List of an ac object.
 *
 * @author fmauz
 */
public interface PermissionEdit {

    public String getTitleOfModal();

    public List<ACEntry> getACEntriesOfGroups();

    public List<Group> getGroupsNotInAcList();

    public boolean isEditOfAclAllowed(Collection col);

    public void addGroupToAcList(Group g);
}
