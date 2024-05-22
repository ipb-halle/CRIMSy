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
import de.ipb_halle.lbac.collections.Collection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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

    private static final long serialVersionUID = 1L;
    public final static Integer MAX_FILES_IN_FOLDER = 100;
    public static final String HASH_ALGO = "SHA-512";

    @Inject
    private FileObjectService fileObjectService;

    private Path collectionBaseDirectory;
    private Logger logger;

    public FileService() {
        collectionBaseDirectory = Paths.get(System.getProperty(
                "de.ipb_halle.lbac.cloud.servlet.FileUploadExec.Path",
                "/data/ui/"), "collections");
        logger = LogManager.getLogger(FileService.class);
    }

    public String getCollectionBaseDirectory() {
        return collectionBaseDirectory.toString();
    }

    /**
     * set the base directory for test purposes
     * @param dir
     */
    public void setCollectionBaseDirectory(Path dir) {
        collectionBaseDirectory = dir;
    }

    public Path getUploadPath(AttachmentHolder attachmentHolder) {
        return Paths.get(getCollectionBaseDirectory(),
                attachmentHolder.getName());
    }

    public String getStoragePath(AttachmentHolder attachmentHolder) {
        if (storagePathExists(attachmentHolder)) {
            return getUploadPath(attachmentHolder).toString();
        }
        return "";
    }

    /**
     * check if storage path exists see:
     * https://docs.oracle.com/javase/tutorial/essential/io/check.html
     * attention: java nio: (!Files.exists) is NOT EQUAL to (Files.notExists)
     *
     * @param attachmentHolder
     * @return
     */
    public boolean storagePathExists(AttachmentHolder attachmentHolder) {
        if (Files.exists(getUploadPath(attachmentHolder))) {
            return true;
        } else if (Files.notExists(getUploadPath(attachmentHolder))) {
            return false;
        } else {
            logger.error(String.format("status of filesystem %s is unknown.", getUploadPath(attachmentHolder)));
            return false;
        }
    }

    /**
     * create new sub directory in root path
     *
     * @param collection - collection to create directory for
     * @return
     */
    public boolean createDir(AttachmentHolder collection) {
        if (collection != null) {
            Path rootPath = getUploadPath(collection);
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
    public boolean deleteDir(AttachmentHolder holder) {
        Path rootPath = null;

        if (holder != null) {
            logger.error("Upload path now " + holder.getStoragePath());
            try {
                rootPath = getUploadPath(holder);
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
                return true;
            } catch (IOException e) {
                logger.error("deleting dir " + holder.getStoragePath() + " |  failed.");
                return false;
            }
        }
        return false;
    }

    /**
     * delete file with absolute path
     *
     * @param path
     * @return
     */
    public boolean deleteFile(String path) {
        if (path != null) {
            Path filePath = Paths.get(path);
            try {
                Files.delete(filePath);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
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

        FileObject fileObject = saveFileInDB(objectOfAttachedFile, fileName, user);
        Path fileLocation = calculateFileLocation(objectOfAttachedFile, fileObject.getId());
        System.out.printf("##\n##\n##Location %s\n##\n##\n", fileLocation.toString());
        updateFileInDB(fileObject,
                fileLocation,
                saveFileToFileSystem(fileStream, fileLocation));
        return fileObject;
    }

    protected FileObject saveFileInDB(AttachmentHolder objectOfAttachedFile, String fileName, User user) {
        FileObject fileObject = new FileObject();
        fileObject.setFileLocation("to be set");
        fileObject.setName(fileName);
        fileObject.setCreated(new Date());
        fileObject.setUserId(user.getId());
        fileObject.setHash("");
        fileObject.setCollectionId(objectOfAttachedFile.getId());
        fileObject = fileObjectService.save(fileObject);
        return fileObject;
    }

    public static Path calculateFileLocation(AttachmentHolder objectOfAttachedFile, Integer fileId) {
        String folder1 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER * MAX_FILES_IN_FOLDER))).toString();
        String folder2 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER))).toString();
        return Paths.get(objectOfAttachedFile.getStoragePath(), folder1, folder2, fileId.toString());
    }

    protected void updateFileInDB(FileObject fileObject, Path fileName, String hash) {
        fileObject.setFileLocation(fileName.toString());
        fileObject.setHash(hash);
        fileObject = fileObjectService.save(fileObject);
    }

    protected String saveFileToFileSystem(InputStream inputStream, Path fileLocation) throws IOException, NoSuchAlgorithmException {
        File f = fileLocation.toFile();
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        Files.copy(dis, fileLocation);
        return HexUtil.toHex(md.digest());
    }

}
