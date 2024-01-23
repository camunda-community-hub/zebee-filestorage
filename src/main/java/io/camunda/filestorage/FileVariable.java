/* ******************************************************************** */
/*                                                                      */
/*  FileVariable                                                        */
/*                                                                      */
/*  File variable contains the file. Attention, file is in memory then  */
/* ******************************************************************** */
package io.camunda.filestorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/* Definition of a FileVariable */
public class FileVariable {
  private String name;
  private String mimeType;
  private byte[] value;

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
  protected FileVariable(StorageDefinition storageDefinition) {
    this.storageDefinition = storageDefinition;
  }

  public String getName() {
    return name;
  }

  public String getMimeType() {
    return mimeType;
  }

  public byte[] getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  public void setStorageDefinition(StorageDefinition storageDefinition) {
    this.storageDefinition = storageDefinition;
  }

  public StorageDefinition getStorageDefinition() {
    return storageDefinition;
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

}
