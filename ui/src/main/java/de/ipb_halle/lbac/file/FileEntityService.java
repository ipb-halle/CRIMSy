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
package de.ipb_halle.lbac.file;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.admission.MemberService;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class FileEntityService implements Serializable {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private CollectionService collectionService;

    @Inject
    private MemberService memberService;

    private Logger logger;

    public FileEntityService() {
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * check file exists in collection
     *
     * @param hash - md5 hash
     * @param collection - collection (id)
     * @throws java.lang.Exception
     */
    public void checkIfFileAlreadyExists(String hash, Collection collection) throws Exception {
        boolean fileExists = ((BigInteger) this.em.createNativeQuery("SELECT count(*) FROM files WHERE hash = :hash and collection_id = :c")
                .setParameter("hash", hash)
                .setParameter("c", collection.getId())
                .getSingleResult()).longValue() > 0;
        if (fileExists) {
            throw new Exception("fileupload_error_duplicate_file");
        }
    }

    /**
     * delete file entity
     *
     * @param fileObject - entity to delete
     */
    public void delete(FileObject fileObject) {
        this.em.createNativeQuery("DELETE FROM files WHERE id=:id")
                .setParameter("id", fileObject.getId())
                .executeUpdate();
    }

    /**
     * delete all files owned by collection
     *
     * @param collection - collection
     */
    public void delete(Collection collection) {
        this.em.createNativeQuery("DELETE FROM files WHERE collection_id = :c")
                .setParameter("c", collection.getId())
                .executeUpdate();
    }

    /**
     * get all file entities in collection
     *
     * @param collection - collection
     * @return - list of file entities
     */
    public List<FileObject> getAllFilesInCollection(Collection collection) {
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<FileObjectEntity> criteriaQuery = builder.createQuery(FileObjectEntity.class);
        Root<FileObjectEntity> fileRoot = criteriaQuery.from(FileObjectEntity.class);
        criteriaQuery.select(fileRoot);
        criteriaQuery.where(builder.equal(fileRoot.get("collection"), collection.getId()));

        List<FileObjectEntity> entities = this.em.createQuery(criteriaQuery).getResultList();
        List<FileObject> results = new ArrayList<>();

        for (FileObjectEntity entity : entities) {
            results.add(new FileObject(entity, collection, memberService.loadUserById(entity.getUser())));
        }
        return results;
    }

    /**
     * file count for collection
     *
     * @param collection - collection (id)
     * @return number of documents or -1
     */
    public long getDocumentCount(Collection collection) {
        try {
            BigInteger cnt = (BigInteger) this.em.createNativeQuery("SELECT count(*) FROM files WHERE collection_id = :c")
                    .setParameter("c", collection.getId())
                    .getSingleResult();
            return cnt.longValue();
        } catch (Exception e) {
            this.logger.warn("getDocumentCount() caught an exception", e);
        }
        return -1;
    }

    /**
     * get file entity by id
     *
     * @param id - id
     * @return - file entity
     */
    public FileObject getFileEntity(Integer id) {
        FileObjectEntity entity = this.em.find(FileObjectEntity.class, id);
        if (entity != null) {
            return new FileObject(
                    entity,
                    collectionService.loadById(entity.getCollection()),
                    memberService.loadUserById(entity.getUser()));
        }
        return null;
    }

    /**
     * Convert String to lower case and add SQL wildcard padding to it. This is
     * necessary as JPA2 does not provide means to createCollection an ilike
     * predicate.
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
            predicates.add(builder.equal(fileObjectRoot.get("collection"), cmap.get("collection_id")));
        }
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));
        List<FileObject> results = new ArrayList<>();
        for (FileObjectEntity entity : this.em.createQuery(criteriaQuery).getResultList()) {
            results.add(
                    new FileObject(
                            entity,
                            collectionService.loadById(entity.getCollection()),
                            memberService.loadUserById(entity.getUser())));
        }
        return results;
    }

    /**
     * save entity
     *
     * @param fileObject - save entity
     * @return 
     */
    public FileObject save(FileObject fileObject) {
        FileObjectEntity foe = this.em.merge(fileObject.createEntity());
        fileObject.setId(foe.getId());
        return fileObject;
    }

    /**
     *
     * @param vector
     */
    public void saveTermVectors(List<TermVector> vector) {
        try {
            for (TermVector tv : vector) {
                em.merge(tv.createEntity());
            }
        } catch (Exception e) {
            logger.error("saveTermVectors() caught an exception:", (Throwable) e);
        }
    }
}
