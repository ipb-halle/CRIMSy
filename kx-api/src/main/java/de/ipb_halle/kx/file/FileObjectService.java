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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class FileObjectService implements Serializable {

    private final static long serialVersionUID = 1L;

    private final String DELETE_COLLECTION_FILES = "DELETE FROM files WHERE collection_id = :c";
    private final String COLLECTION_FILE_COUNT = "SELECT count(*) FROM files WHERE collection_id = :c";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * delete file entity
     *
     * @param fileObject - entity to delete
     */
    public void delete(FileObject fileObject) {
        FileObjectEntity foe = this.em.find(FileObjectEntity.class, fileObject.getId());
        if (foe != null) {
            this.em.remove(foe);
        }
    }

    /**
     * delete all files of a collection
     *
     * @param collectionId collection Id
     */
    @Deprecated
    public void deleteCollectionFiles(Integer collectionId) {
        this.em.createNativeQuery(DELETE_COLLECTION_FILES)
                .setParameter("c", collectionId)
                .executeUpdate();
    }

    /**
     * get all file entities in collection
     *
     * @param collectionId - collection identifier
     * @return - list of file entities
     */
    @Deprecated
    public List<FileObject> getAllFilesInCollection(Integer collectionId) {
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<FileObjectEntity> criteriaQuery = builder.createQuery(FileObjectEntity.class);
        Root<FileObjectEntity> fileRoot = criteriaQuery.from(FileObjectEntity.class);
        criteriaQuery.select(fileRoot);
        criteriaQuery.where(builder.equal(fileRoot.get("collectionId"), collectionId));

        List<FileObjectEntity> entities = this.em.createQuery(criteriaQuery).getResultList();
        List<FileObject> results = new ArrayList<>();

        for (FileObjectEntity entity : entities) {
            results.add(new FileObject(entity));
        }
        return results;
    }

    /**
     * file count for collection
     *
     * @param collectionId - collection id
     * @return number of documents or -1
     */
    public long getDocumentCount(Integer collectionId) {
        try {
            Long cnt = (Long) this.em.createNativeQuery(COLLECTION_FILE_COUNT)
                    .setParameter("c", collectionId)
                    .getSingleResult();
            return cnt;
        } catch (Exception e) {
            this.logger.warn("getDocumentCount() caught an exception", e);
        }
        return -1;
    }

    /**
     * Convert String to lower case and add SQL wildcard padding to it. This is
     * necessary as JPA2 does not provide means to create an ilike predicate.
     *
     * @param st input string
     * @return "%" + st.toLowerCase() + "%"
     */
    private String iWildcard(String st) {
        return "%" + st.toLowerCase() + "%";
    }

    /**
     * select data with params.
     *
     * @param cmap allowed id, name, filename, hash
     * @return - list of file entities
     */
    public List<FileObject> load(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<FileObjectEntity> criteriaQuery = builder.createQuery(FileObjectEntity.class);
        Root<FileObjectEntity> fileObjectRoot = criteriaQuery.from(FileObjectEntity.class);
        EntityType<FileObjectEntity> fileObjectType = em.getMetamodel().entity(FileObjectEntity.class);

        criteriaQuery.select(fileObjectRoot);
        List<Predicate> predicates = new ArrayList<>();

        if (cmap == null) {
            cmap = new HashMap<>();
        }
        if (cmap.get("id") != null) {
            predicates.add(builder.equal(fileObjectRoot.get("id"), cmap.get("id")));
        }
        if (cmap.get("name") != null) {
            predicates.add(builder.like(builder.lower(
                    fileObjectRoot.get(fileObjectType.getDeclaredSingularAttribute("name", String.class))),
                    iWildcard((String) cmap.get("description"))));
        }
        if (cmap.get("filename") != null) {
            predicates.add(builder.like(builder.lower(
                    fileObjectRoot.get(fileObjectType.getDeclaredSingularAttribute("filename", String.class))),
                    iWildcard((String) cmap.get("name"))));
        }
        if (cmap.get("hash") != null) {
            predicates.add(builder.equal(fileObjectRoot.get("hash"), cmap.get("hash")));
        }
        if (cmap.get("collection_id") != null) {
            predicates.add(builder.equal(fileObjectRoot.get("collectionId"), cmap.get("collection_id")));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));
        List<FileObject> results = new ArrayList<>();
        for (FileObjectEntity entity : this.em.createQuery(criteriaQuery).getResultList()) {
            results.add(new FileObject(entity));
        }
        return results;
    }

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
