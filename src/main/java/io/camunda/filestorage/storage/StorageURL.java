package io.camunda.filestorage.storage;

import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageURL extends Storage {
  Logger logger = LoggerFactory.getLogger(StorageURL.class.getName());


  public StorageURL(StorageDefinition storageDefinition, FileRepoFactory fileRepoFactory) {
    super(storageDefinition, fileRepoFactory);
  }

  /**
   *
   * @param url to access
   * @return the connection string
   */
  public static String getStorageDefinitionString(URL url ) {
    return StorageDefinition.StorageDefinitionType.URL.toString()+StorageDefinition.STORAGE_DEFINITION_DELIMITATEUR+url.toString();
  }
  /**
   *
   * @param url to access
   * @return the connection string
   */
  public static String getStorageDefinitionString(String url ) {
    return StorageDefinition.StorageDefinitionType.URL.toString()+StorageDefinition.STORAGE_DEFINITION_DELIMITATEUR+url;
  }
  @Override
  public String getName() {
    return "URL";
  }

  public static String getStorageDefinitionString() {
    return StorageDefinition.StorageDefinitionType.URL.toString();
  }

  /**
   * Save the file Variable structure in the temporary folder
   *
   * @param fileVariable          fileVariable to save it
   * @param fileVariableReference file variable to update (may be null)
   */
  public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference)
      throws Exception {
    throw new Exception("Storage URL work in read only");
  }

  /**
   * read the fileVariable
   *
   * @param fileVariableReference name of the file in the temporary directory
   * @return the fileVariable object
   * @throws Exception during the writing
   */
  public FileVariable fromStorage(FileVariableReference fileVariableReference) throws Exception {
    InputStream inputStream = null;
    try {
      URL url = new URL(fileVariableReference.getContent().toString());

      // Open a connection to the URL

      inputStream = url.openStream();
      // Define a buffer to read data into
      byte[] buffer = new byte[1024];
      int bytesRead;
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

      // Read bytes from the input stream and store them in the ByteBuffer
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        byteBuffer.write(buffer, 0, bytesRead);
      }

      FileVariable fileVariable = new FileVariable(getStorageDefinition());
      fileVariable.setName(fileVariableReference.content.toString());

      Path pathUri = Paths.get(url.toURI().getPath());
      fileVariable.setMimeType(FileVariable.getMimeTypeFromPath(pathUri));
      fileVariable.setValue(byteBuffer.toByteArray());
      return fileVariable;

    } catch (Exception e) {
      logger.error(getFileRepoFactory().getLoggerHeaderMessage(StorageURL.class) + "Exception " + e
          + " During write fileVariable on Url[" + fileVariableReference.getContent().toString() + "]");
      throw e;
    } finally {
      if (inputStream != null)
        inputStream.close();
    }

  }

  /**
   * Delete the file
   *
   * @param fileVariableReference name of the file in the temporary directory
   * @return true if the operation was successful
   */
  public boolean purgeStorage(FileVariableReference fileVariableReference) {
    // Read only
    return false;
  }

}
