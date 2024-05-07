package de.ipb_halle.lbac.file.mock;

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.file.save.FileSaver;
import de.ipb_halle.lbac.util.HexUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSaverMock extends FileSaver {
    public FileSaverMock(final FileObjectService fileObjectService) {
        super(fileObjectService);
    }

    protected void saveFileToFileSystem(InputStream inputStream, Path fileLocation) throws IOException, NoSuchAlgorithmException {

    }
}
