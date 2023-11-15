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
package de.ipb_halle.lbac.collections.mock;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.admission.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class FileObjectServiceMock extends FileObjectService {

    private boolean deleteSuccess = true;
    private boolean fileAvailable = true;

    public Boolean delete(User user) {
        return deleteSuccess;
    }

    @Override
    public FileObject loadFileObjectById(Integer id) {
        return new FileObject();
    }

    public Boolean isFileAvailable(String hash) {
        return fileAvailable;
    }

    @Override
    public List<FileObject> load(Map<String, Object> cmap) {
        return new ArrayList<>();
    }

    @Override
    public FileObject save(FileObject fileEntity) {
        return null;
    }

    @Override
    public void deleteCollectionFiles(Integer collectionId) {

    }
}
