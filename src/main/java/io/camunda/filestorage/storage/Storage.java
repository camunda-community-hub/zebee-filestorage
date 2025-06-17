package io.camunda.filestorage.storage;

import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;

public abstract class Storage {

  private final StorageDefinition storageDefinition;
  private final FileRepoFactory fileRepoFactory;

  protected Storage(StorageDefinition storageDefinition,  FileRepoFactory fileRepoFactory) {
    this.storageDefinition = storageDefinition;
    this.fileRepoFactory = fileRepoFactory;
  }


  public StorageDefinition getStorageDefinition() {
    return storageDefinition;
  }

  public FileRepoFactory getFileRepoFactory() {
    return fileRepoFactory;
  }

  /**
   * Return the name of the storage, for identification
   * @return name of the storage
   */
  public abstract String getName();

  public  StorageDefinition.StorageDefinitionType getType() {
    return storageDefinition.type;
  }


  /**
   * Save a FileVariable to the storageDefinition.
   * @param fileVariable file to save
   * @param fileVariableReference  the reference to update. Maybe null to save the file for the first time
   * @return a FileVariableReference
   * @throws Exception if an error arrived
   */
   public abstract FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference) throws Exception;

  /**
   * Return a File variable from a FileVariableReference
   * @param fileVariableReference file reference to get
   * @return the FileVariable
   * @throws Exception if an error arrive
   */
  public abstract FileVariable fromStorage(FileVariableReference fileVariableReference) throws Exception;

  /**
   * purge the reference in the storage
   * @param fileVariableReference reference to purge
   * @throws Exception if an error arrive
   */
  public abstract boolean purgeStorage(FileVariableReference fileVariableReference) throws Exception;

}
