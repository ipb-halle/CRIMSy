package de.ipb_halle.lbac.file.mock;

import org.primefaces.model.file.UploadedFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadedFileMock implements UploadedFile {
    @Override
    public String getFileName() {
        return "DummyFile";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream("DummyContent".getBytes());
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public void write(final String s) throws Exception {

    }

    @Override
    public void delete() throws IOException {

    }
}
