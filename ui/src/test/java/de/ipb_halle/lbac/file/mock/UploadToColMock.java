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
package de.ipb_halle.lbac.file.mock;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.UploadToCol;
import de.ipb_halle.lbac.file.save.AttachmentHolder;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.InputStream;
import javax.servlet.AsyncContext;

/**
 *
 * @author fmauz
 */
public class UploadToColMock extends UploadToCol {

    protected String fileBaseFolder;

    public UploadToColMock(
            InputStream filterDefinition,
            FileEntityService fileEntityService,
            User user,
            AsyncContext asyncContext,
            CollectionService collectionService,
            TermVectorEntityService termVectorService,
            String fileBaseFolder) {
        super(filterDefinition, fileEntityService, user, asyncContext, collectionService, termVectorService);
        this.fileBaseFolder = fileBaseFolder;
    }

    @Override
    protected AttachmentHolder getAttachmentTarget() throws Exception {
        Collection holder = (Collection) super.getAttachmentTarget();
        holder.COLLECTIONS_BASE_FOLDER = fileBaseFolder;
        return holder;
    }

}
