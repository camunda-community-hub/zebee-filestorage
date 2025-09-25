/* ******************************************************************** */
/*                                                                      */
/*  FileVariable                                                        */
/*                                                                      */
/*  File variable contains the file. Attention, file is in memory then  */
/* ******************************************************************** */
package io.camunda.filestorage;

import io.camunda.filestorage.storage.StorageDefinition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/* Definition of a FileVariable */
public class FileVariable {
    private String name;
    private String originalName = null;
    private String mimeType;
    private byte[] valueBytes;
    private InputStream valueStream;
    /**
     * Keep the information from where this fileVariable come from.
     * So, if the worker wants to save it at the same place, it has the information.
     * This is only an information from the FileVariable, it may be null
     */
    private StorageDefinition storageDefinition;

    /**
     * The default connectors exist to let the Json deserializer create it
     */
    public FileVariable() {

    }

    /**
     * To load / create a file Variable, go to the FileVariableFactory
     */
    public FileVariable(StorageDefinition storageDefinition) {
        this.storageDefinition = storageDefinition;
    }

    /**
     * Return the suffix of the file, based on the name or on the mimeType
     *
     * @return the suffix
     */
    public static String getSuffix(String fileName) {
        if (fileName != null) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot != -1)
                return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * return the Mimetype from the fileName.
     *
     * @param fileName file name. Must exist/accessible to be transformed to a File
     * @return the mime type
     */
    public static String getMimeTypeFromName(String fileName) {
        return getMimeTypeFromPath(new File(fileName).toPath());
    }

    /**
     * Return the MimeType from a Path
     *
     * @param path path to the information
     * @return the mime type
     */
    public static String getMimeTypeFromPath(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException var3) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (originalName == null)
            originalName = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOriginalName() {
        return this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public StorageDefinition getStorageDefinition() {
        return storageDefinition;
    }

    public void setStorageDefinition(StorageDefinition storageDefinition) {
        this.storageDefinition = storageDefinition;
    }

    /* ******************************************************************** */
    /*                                                                      */
    /*  Value                                                               */
    /*                                                                      */
    /*  Value can be managed as byte[] or InputStream                       */
    /* ******************************************************************** */

    /**
     * Value can
     *
     * @return content in byte
     * @throws IOException in case of error, when the value is store in Stream
     */
    public byte[] getValue() throws IOException {
        if (valueBytes != null)
            return valueBytes;
        if (valueStream != null) {
            return valueStream.readAllBytes();
        }
        return null;
    }

    public void setValue(byte[] valueBytes) {
        this.valueBytes = valueBytes;
    }

    public InputStream getValueStream() {
        if (valueStream != null)
            return valueStream;
        if (valueBytes != null)
            return new ByteArrayInputStream(valueBytes);
        return null;
    }

    public boolean isValueBytes() {
        return valueBytes != null;
    }

    public boolean isValueStream() {
        return valueStream != null;
    }

    public void setValueStream(InputStream inputStream) {
        this.valueStream = inputStream;
    }

}
