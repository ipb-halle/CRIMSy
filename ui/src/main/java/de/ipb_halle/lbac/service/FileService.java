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
package de.ipb_halle.lbac.service;

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;

import de.ipb_halle.lbac.util.HexUtil;
import jakarta.ejb.Stateless;

import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * implements functions for file handling in a repository
 */
@Stateless
public class FileService implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    public final static Integer MAX_FILES_IN_FOLDER = 100;
    public static final String HASH_ALGO = "SHA-512";

    @Inject
    private FileObjectService fileObjectService;

    private Logger logger;

    public FileService() {
        logger = LogManager.getLogger(FileService.class);
    }

    /**
     * for testing purposes
     *
     * @param fo FileObjectService for use in test cases
     */
    public FileService(FileObjectService fo) {
        fileObjectService = fo;
        logger = LogManager.getLogger(FileService.class);
    }

    public String getCollectionBasePath() {
        return Paths.get(System.getProperty(
                        "de.ipb_halle.lbac.cloud.servlet.FileUploadExec.Path",
                        "/data/ui/"), "collections")
                .toString();
    }

    public Path getCollectionPath(AttachmentHolder attachmentHolder) {
        return Paths.get(getCollectionBasePath(),
                attachmentHolder.getName());
    }

    /**
     * check if storage path exists see:
     * https://docs.oracle.com/javase/tutorial/essential/io/check.html
     * attention: java nio: (!Files.exists) is NOT EQUAL to (Files.notExists)
     *
     * @param attachmentHolder
     * @return true if storagePath of collection exists
     */
    public boolean storagePathExists(AttachmentHolder attachmentHolder) {
        if (Files.exists(getCollectionPath(attachmentHolder))) {
            return true;
        } else if (Files.notExists(getCollectionPath(attachmentHolder))) {
            return false;
        } else {
            logger.error(String.format("status of filesystem %s is unknown.", getCollectionPath(attachmentHolder)));
            return false;
        }
    }

    /**
     * create new sub directory in root path
     *
     * @param collection - collection to create directory for
     * @return true if collection storage path has been created successfully.
     */
    public boolean createDir(AttachmentHolder collection) {
        if (collection != null) {
            Path rootPath = getCollectionPath(collection);
            try {

                Files.createDirectories(rootPath);
                return true;
            } catch (IOException e) {
                logger.error("create dir " + rootPath + " failed.", e);
                return false;
            }
        }
        return false;
    }

    /**
     * delete all files recursively in a sub dir
     *
     * @param holder the collection
     * @return boolean
     */
    public void deleteDir(AttachmentHolder holder) {
        if (holder != null) {
            logger.error("Upload path now " + holder.getStoragePath());
            try {
                Path rootPath = getCollectionPath(holder);
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            } catch (IOException e) {
                logger.error("deleting dir " + holder.getStoragePath() + " |  failed.");
            }
        }
    }

    /**
     * delete file with absolute path
     *
     * @param path
     */
    public void deleteFile(String path) {
        if (path != null) {
            Path filePath = Paths.get(path);
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                this.logger.warn(String.format("deleteFile(%s) failed.", path));
            }
        }
    }

    /**
     * count physical files recursively in a sub directory
     *
     * @param dirPath input Path
     * @return long
     */
    public long countFilesInDir(String dirPath) {
        if (dirPath != null && dirPath.length() > 0) {
            try {
                Path rootPath = Paths.get(dirPath);
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    return walk.parallel()
                            .filter(p -> !p.toFile().isDirectory())
                            .count();
                }
            } catch (IOException e) {
                logger.error("count files in dir " + dirPath + " failed.", e);
                return -1L;
            }
        }
        return -1L;
    }

    public FileObject saveFile(
            AttachmentHolder objectOfAttachedFile,
            String fileName,
            InputStream fileStream,
            User user) throws NoSuchAlgorithmException, IOException {

        //a new file object will be in db created
        FileObject fileObject = saveFileInDB(objectOfAttachedFile, fileName, user);

        //calculation of file path in file system, with the help of file object information (Id)
        Path fileLocation = calculateFileLocation(objectOfAttachedFile, fileObject.getId());

        //save file in to system
        String FileSaveHash = saveFileToFileSystem(fileStream, fileLocation);

        //updates file object in db
        fileObject = updateFileInDB(fileObject, fileLocation, FileSaveHash);

        return fileObject;
    }

    protected FileObject saveFileInDB(AttachmentHolder objectOfAttachedFile, String fileName, User user) {
        //create new file object
        FileObject fileObject = new FileObject();

        //set the values
        fileObject.setFileLocation("to be set");
        fileObject.setName(fileName);
        fileObject.setCreated(new Date());
        fileObject.setUserId(user.getId());
        fileObject.setHash("");
        fileObject.setCollectionId(objectOfAttachedFile.getId());

        //the object will be saved in db,returns updated file-object (with id from db)
        return fileObjectService.save(fileObject);
    }

    public static Path calculateFileLocation(AttachmentHolder objectOfAttachedFile, Integer fileId) {
        String folder1 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER * MAX_FILES_IN_FOLDER))).toString();
        String folder2 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER))).toString();
        return Paths.get(objectOfAttachedFile.getStoragePath(), folder1, folder2, fileId.toString());
    }

    protected FileObject updateFileInDB(FileObject fileObject, Path fileName, String hash) {

        fileObject.setFileLocation(fileName.toString());
        fileObject.setHash(hash);

        //goes to kx fileObjectService and merges the entity in db and returns updated object from db
        return fileObjectService.save(fileObject);
    }

    protected String saveFileToFileSystem(InputStream inputStream, Path fileLocation) throws IOException, NoSuchAlgorithmException {
        //generates path
        Path parent = fileLocation.getParent();

        if (!parent.toFile().exists()) {
            if (!parent.toFile().mkdirs()) {
                throw new IOException(String.format("mkdirs failed for %s", parent));
            }
        }
        MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        Files.copy(dis, fileLocation);
        return HexUtil.toHex(md.digest());
    }

}
