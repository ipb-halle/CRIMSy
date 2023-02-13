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
    private final static String defaultPath = "/data/ui";
    private final static String pathPropName = "de.ipb_halle.lbac.cloud.servlet.FileUploadExec.Path";

    private Logger logger;
    private String uploadRootPath;

    public FileService() {
        logger = LogManager.getLogger(FileService.class);
        this.uploadRootPath = System.getProperty(pathPropName, defaultPath);
    }

    //*** getter and setter
    public static String getDefaultPath() {
        return defaultPath;
    }

    public Path getUploadPath(String path) {
        return Paths.get(this.uploadRootPath, path);
    }

    public String getStoragePath(String dirPath) {
        if (storagePathExists(dirPath)) {
            return getUploadPath(dirPath).toString();
        }
        return "";
    }

    /**
     * check if storage path exists see:
     * https://docs.oracle.com/javase/tutorial/essential/io/check.html
     * attention: java nio: (!Files.exists) is NOT EQUAL to (Files.notExists)
     *
     * @param dirPath
     * @return
     */
    public boolean storagePathExists(String dirPath) {
        if (Files.exists(getUploadPath(dirPath))) {
            return true;
        } else if (Files.notExists(getUploadPath(dirPath))) {
            return false;
        } else {
            logger.error(String.format("status of filesystem %s is unknown.", getUploadPath(dirPath)));
            return false;
        }
    }

    /**
     * @param dirPath
     * @return boolean of (exists & read & write permission)
     */
    public boolean storagePathIsAccessible(String dirPath) {
        return Files.isDirectory(getUploadPath(dirPath)) & Files.isReadable(getUploadPath(dirPath)) & Files.isWritable(getUploadPath(dirPath));
    }

    /**
     * create new sub directory in root path
     *
     * @param dirPath - name of directory
     * @return
     */
    public boolean createDir(String dirPath) {
        if (dirPath != null && dirPath.length() > 0) {
            Path rootPath = Paths.get(this.uploadRootPath, dirPath);
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
     * @param dirPath input Path
     * @return boolean
     */
    public boolean deleteDir(String dirPath) {
        if (dirPath != null && dirPath.length() > 0) {
            try {
                Path rootPath = Paths.get(this.uploadRootPath, dirPath);
                try (Stream<Path> walk = Files.walk(rootPath)) {
                    walk
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
                return true;
            } catch (IOException e) {
                logger.error("deleting dir " + dirPath + " failed.");
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
     * @param collection
     * @param filename
     * @return
     */
    public boolean deleteFile(String collection, String filename) {
        if (filename != null && filename.length() > 4) {
            Path filePath = Paths.get(this.getUploadPath(collection).toString(), filename.substring(1, 2), filename.substring(3, 4), filename);
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
     * wrapper for delete file
     *
     * @param collection
     * @param filename
     * @return
     */
    public boolean deleteFile(Collection collection, String filename) {
        return deleteFile(collection.getName(), filename);
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
}
