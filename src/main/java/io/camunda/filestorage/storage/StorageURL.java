package io.camunda.filestorage.storage;

import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param url to access
     * @return the connection string
     */
    public static String getStorageDefinitionString(URL url) {
        return StorageDefinition.StorageDefinitionType.URL + StorageDefinition.STORAGE_DEFINITION_DELIMITATEUR + url;
    }

    /**
     * @param url to access
     * @return the connection string
     */
    public static String getStorageDefinitionString(String url) {
        return StorageDefinition.StorageDefinitionType.URL + StorageDefinition.STORAGE_DEFINITION_DELIMITATEUR + url;
    }

    public static String getStorageDefinitionString() {
        return StorageDefinition.StorageDefinitionType.URL.name();
    }

    @Override
    public String getName() {
        return "URL";
    }

    /**
     * Save the file Variable structure in the temporary folder
     *
     * @param fileVariable          fileVariable to save it
     * @param fileVariableReference file variable to update (may be null)
     */
    public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference) throws Exception {
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
        try {
            URL url = new URL(fileVariableReference.getContent().toString());

            // Open a connection to the URL
            // don't use a try() because we want the inputStream open when we finish the method
            InputStream urlInputStream = url.openStream();
            // Define a buffer to read data into

            Path pathUri = Paths.get(url.toURI().getPath());
            String filename = pathUri.getFileName().toString();

            FileVariable fileVariable = new FileVariable(getStorageDefinition());
            fileVariable.setName(filename);
            fileVariable.setOriginalName(url.toString());
            fileVariable.setMimeType(FileVariable.getMimeTypeFromPath(pathUri));
            fileVariable.setValueStream(urlInputStream);
            return fileVariable;

        } catch (Exception e) {
            logger.error(getFileRepoFactory().getLoggerHeaderMessage(StorageURL.class) + "Exception " + e + " During write fileVariable on Url[" + fileVariableReference.getContent().toString() + "]");
            throw e;
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
