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

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.service.FileService;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author fmauz
 */
public class FileServiceMock extends FileService {

    private boolean createSuccess = true;
    private boolean deleteSuccess = true;

    @Override
    public long countFilesInDir(String dirPath) {
        return 0L;
    }

    @Override
    public boolean createDir(AttachmentHolder holder) {
        return createSuccess;
    }

    @Override
    public boolean deleteDir(AttachmentHolder holder) {
        return deleteSuccess;
    }

    @Override
    public boolean deleteFile(String path) {
        return deleteSuccess;
    }

    @Override
    public boolean deleteFile(AttachmentHolder collection, String filename) {
        return deleteSuccess;
    }

    @Override
    public boolean deleteFile(Collection collection, String filename) {
        return deleteSuccess;
    }

    @Override
    public String getStoragePath(AttachmentHolder holder) {
        return holder.getBaseFolder();
    }

    @Override
    public Path getUploadPath(AttachmentHolder holder) {
        return Paths.get(holder.getBaseFolder());
    }

    @Override
    public boolean storagePathExists(AttachmentHolder attachment) {
        return true;
    }

    public void setCreateSuccess(boolean createSuccess) {
        this.createSuccess = createSuccess;
    }

    public void setDeleteSuccess(boolean deleteSuccess) {
        this.deleteSuccess = deleteSuccess;
    }

}
