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
import de.ipb_halle.lbac.collections.Collection;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import jakarta.ejb.Stateless;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * implements functions for file handling in a repository
 */
@Stateless
public class FileService implements Serializable {

    private static final long serialVersionUID = 1L;

    private Logger logger;

    public FileService() {
        logger = LogManager.getLogger(FileService.class);

    }

    public Path getUploadPath(AttachmentHolder attachmentHolder) {
        return Paths.get(attachmentHolder.getBaseFolder());
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
            Path rootPath = Paths.get(collection.getBaseFolder());
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
     * @param dirName input Path
     * @return boolean
     */
    public boolean deleteDir(AttachmentHolder holder) {
        Path rootPath = null;

        if (holder != null) {
            logger.error("Upload path now " + holder.getBaseFolder());
            try {
                rootPath = Paths.get(holder.getBaseFolder());
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
                return true;
            } catch (IOException e) {
                logger.error("deleting dir " + holder.getBaseFolder() + " |  failed.");
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
     * delete file from repository
     *
     * @param attachmentHolder
     * @param filename
     * @return
     */
    public boolean deleteFile(AttachmentHolder attachmentHolder, String filename) {
        if (attachmentHolder != null && filename != null && filename.length() > 4) {
            Path filePath = Paths.get(
                    this.getUploadPath(attachmentHolder).toString(),
                    filename.substring(1, 2),
                    filename.substring(3, 4),
                    filename);
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
     * delete file from repository
     *
     * @param collection
     * @param filename
     * @return
     */


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
}
