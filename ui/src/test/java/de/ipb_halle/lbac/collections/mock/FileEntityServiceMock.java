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

import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.FileObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class FileEntityServiceMock extends FileEntityService {

    private boolean deleteSuccess = true;
    private boolean fileAvailable = true;

    public void FileEntityServiceInit() {

    }

    public Boolean delete(User user) {
        return deleteSuccess;
    }

    @Override
    public List<FileObject> getAllFilesInCollection(Collection collection) {
        return new ArrayList<>();
    }

    @Override
    public FileObject getFileEntity(Integer id) {
        return new FileObject();
    }

    public Boolean isFileAvailable(String hash) {
        return fileAvailable;
    }

    @Override
    public void checkIfFileAlreadyExists(String hash, Collection collection) throws Exception {

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
    public void delete(Collection collection) {

    }

}
