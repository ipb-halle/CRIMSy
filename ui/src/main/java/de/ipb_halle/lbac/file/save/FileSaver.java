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
package de.ipb_halle.lbac.file.save;

import de.ipb_halle.kx.file.AttachmentHolder;
import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.util.HexUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class FileSaver {

    protected Path fileLocation;
    protected Integer fileId;
    protected AttachmentHolder objectOfAttachedFile;
    protected FileObjectService fileObjectService;
    protected User user;
    protected String hash;
    protected static final String HASH_ALGO = "SHA-512";
    protected Integer MAX_FILES_IN_FOLDER = 100;
    protected FileObject fileObject;

    public FileSaver(FileObjectService fileObjectService) {
        this.fileObjectService = fileObjectService;
    }

    public Integer saveFile(
            AttachmentHolder objectOfAttachedFile,
            String fileName,
            InputStream fileStream) throws NoSuchAlgorithmException, IOException {

        fileId = saveFileInDB(objectOfAttachedFile, fileName);
        fileLocation = calculateFileLocation(objectOfAttachedFile);
        saveFileToFileSystem(fileStream, fileLocation);
        updateFileInDB(fileLocation, hash);

        return fileId;
    }

    protected Integer saveFileInDB(AttachmentHolder objectOfAttachedFile, String fileName) {
        fileObject = new FileObject();
        fileObject.setFileLocation("to be set");
        fileObject.setName(fileName);
        fileObject.setCreated(new Date());
        fileObject.setUserId(user.getId());
        fileObject.setHash(hash);
        fileObject.setCollectionId(objectOfAttachedFile.getId());
        fileObject = fileObjectService.save(fileObject);
        return fileObject.getId();
    }

    protected Path calculateFileLocation(AttachmentHolder objectOfAttachedFile) {
        String folder1 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER * MAX_FILES_IN_FOLDER))).toString();
        String folder2 = ((Integer) (fileId / (MAX_FILES_IN_FOLDER))).toString();
        return Paths.get(objectOfAttachedFile.getBaseFolder(), folder1, folder2, fileId.toString());

    }

    protected void updateFileInDB(Path fileName, String hash) {
        fileObject.setFileLocation(fileName.toString());
        fileObject.setHash(hash);
        fileObjectService.save(fileObject);
    }

    protected void saveFileToFileSystem(InputStream inputStream, Path fileLocation) throws IOException, NoSuchAlgorithmException {
        File f = fileLocation.toFile();
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        Files.copy(dis, fileLocation);
        hash = HexUtil.toHex(md.digest());
    }

    public void updateLanguageOfFile(String language) {
        fileObject.setDocumentLanguage(language);
        fileObjectService.save(fileObject);
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Path getFileLocation() {
        return fileLocation;
    }
}
