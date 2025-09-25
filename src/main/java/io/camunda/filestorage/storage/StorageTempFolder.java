/* ******************************************************************** */
/*                                                                      */
/*  FileVariableTempFolder                                              */
/*                                                                      */
/*  Save a file variable in the temporary folder of the host            */
/*  Attention, this is the temporary folder where the worker is running */
/* ******************************************************************** */
package io.camunda.filestorage.storage;

import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class StorageTempFolder extends Storage {
    Logger logger = LoggerFactory.getLogger(StorageTempFolder.class.getName());

    public StorageTempFolder(StorageDefinition storageDefinition, FileRepoFactory fileRepoFactory) {
        super(storageDefinition, fileRepoFactory);
    }

    public static String getStorageDefinitionString() {
        return StorageDefinition.StorageDefinitionType.TEMPFOLDER.toString();
    }

    /**
     * get the temporary path
     *
     * @return the temporary path on this host
     */
    public static Path getTempFolder() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir);
    }

    @Override
    public String getName() {
        return "TempFolder";
    }

    /**
     * Save the file Variable structure in the temporary folder
     *
     * @param fileVariable          fileVariable to save it
     * @param fileVariableReference file variable to update (may be null)
     */
    public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference)
            throws Exception {
        Path tempPath = null;
        try {
            tempPath = getPathFromVariable(fileVariable);
            // File is created by getting the name
            Files.copy(fileVariable.getValueStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

            FileVariableReference fileVariableReferenceOutput = new FileVariableReference();
            fileVariableReferenceOutput.storageDefinition = getStorageDefinition().encodeToString();
            fileVariableReferenceOutput.content = tempPath.getFileName().toString();
            return fileVariableReferenceOutput;

        } catch (Exception e) {
            logger.error(getFileRepoFactory().getLoggerHeaderMessage(StorageTempFolder.class) + "Exception " + e
                    + " During write fileVariable on tempFolder[" + tempPath == null ? "null" : tempPath + "]");
            throw e;
        }
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
            // get the temporary path
            Path tempFolder = getTempFolder();
            String separator = FileSystems.getDefault().getSeparator();
            FileVariable fileVariable = new FileVariable(getStorageDefinition());
            fileVariable.setName(fileVariableReference.content.toString());
            fileVariable.setMimeType(FileVariable.getMimeTypeFromName(fileVariableReference.content.toString()));
            Path filePath = Paths.get(tempFolder + separator + fileVariableReference.content.toString());
            fileVariable.setValueStream(Files.newInputStream(filePath));
            return fileVariable;

        } catch (Exception e) {
            logger.error(
                    getFileRepoFactory().getLoggerHeaderMessage(StorageTempFolder.class) + "Exception " + e + " During read file["
                            + fileVariableReference.content.toString() + "] in temporaryPath[" + fileVariableReference.content.toString() + "]");
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

        Path tempFolder = getTempFolder();
        File file = new File(tempFolder + FileSystems.getDefault().getSeparator() + fileVariableReference.getContent().toString());
        if (file.exists())
            return file.delete();
        return true;
    }

    private Path getPathFromVariable(FileVariable fileVariable) throws IOException {
        String fileName = fileVariable.getName();
        fileName = fileName.replace("\\\"", "");
        String suffix = "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot != -1) {
            suffix = fileName.substring(lastDot + 1);
            fileName = fileName.substring(0, lastDot) + "_";
        }

        return Files.createTempFile(fileName, "." + suffix);
    }
}
