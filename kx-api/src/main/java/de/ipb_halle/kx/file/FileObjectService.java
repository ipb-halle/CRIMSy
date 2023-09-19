/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.file;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectEntity;
import java.io.Serializable;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class FileObjectService implements Serializable {

    private final static long serialVersionUID = 1L;
    
    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * get file entity by id
     *
     * @param id - id
     * @return - file entity
     */
    public FileObject loadFileObjectById(Integer id) {
        FileObjectEntity entity = this.em.find(FileObjectEntity.class, id);
        if (entity != null) {
            return new FileObject(entity);
        }
        return null;
    }

    public FileObject save(FileObject f) {
        return new FileObject(this.em.merge(f.createEntity()));
    }
}
