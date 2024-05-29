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
import de.ipb_halle.lbac.service.FileService;

/**
 * @author fmauz
 */
public class FileServiceMock extends FileService {

    private boolean createSuccess = true;

    @Override
    public long countFilesInDir(String dirPath) {
        return 0L;
    }

    @Override
    public boolean createDir(AttachmentHolder holder) {
        return createSuccess;
    }

    @Override
    public void deleteDir(AttachmentHolder holder) {
    }

    @Override
    public void deleteFile(String path) {
    }

    @Override
    public boolean storagePathExists(AttachmentHolder attachment) {
        return true;
    }
}
