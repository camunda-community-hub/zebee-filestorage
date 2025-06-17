package io.camunda.filestorage.storage;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class StorageGoogleDrive extends Storage {
    Logger logger = LoggerFactory.getLogger(StorageCMIS.class.getName());

    private String APPLICATION_NAME = "FileStorage";

    public StorageGoogleDrive(StorageDefinition storageDefinition, FileRepoFactory fileRepoFactory) {
        super(storageDefinition, fileRepoFactory);
    }

    @Override
    public String getName() {
        return "GOOGLEDRIVE";
    }


    /**
     * Save the file Variable structure in the CMIS repository
     *
     * @param fileVariable          fileVariable to save it
     * @param fileVariableReference file variable to update (may be null)
     */
    public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference) throws Exception {

        Drive driveService = connect();

        // File metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileVariable.getName());

        // File content
        java.io.File filePath = new java.io.File("local-example.txt");
        ByteArrayContent content = new ByteArrayContent(fileVariable.getMimeType(), fileVariable.getValue());

        File uploadedFile = driveService.files().create(fileMetadata, content)
                .setFields("id, name")
                .execute();


        FileVariableReference fileVariableReferenceOutput = new FileVariableReference();
        fileVariableReferenceOutput.storageDefinition = getStorageDefinition().encodeToString();
        fileVariableReferenceOutput.content = uploadedFile.getId();
        return fileVariableReferenceOutput;

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
            Drive driveService = connect();

            // Download file content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            File fileMetadata = driveService.files().get(fileVariableReference.content.toString())
                    .setFields("name, mimeType")
                    .execute();

            driveService.files().get(fileVariableReference.content.toString()).executeMediaAndDownloadTo(outputStream);


            FileVariable fileVariable = new FileVariable(getStorageDefinition());
            fileVariable.setName(fileMetadata.getName());
            fileVariable.setMimeType(fileMetadata.getMimeType());
            fileVariable.setValue(outputStream.toByteArray());
            return fileVariable;
        } catch (Exception e) {
            logger.error(getFileRepoFactory().getLoggerHeaderMessage(StorageCMIS.class) + ": exception " + e + " During read file[" + fileVariableReference.content.toString() + "]");
            throw e;
        }
    }


    /**
     * Remove a file in the directory
     * Remove a file in the directory
     *
     * @param fileVariableReference name of the file in the temporary directory
     * @return true if the operation was successful
     */
    public boolean purgeStorage(FileVariableReference fileVariableReference) throws Exception {
        return false;
    }

    private Drive connect() throws Exception {
        try {
            Long beginExecution = System.currentTimeMillis();
            /* JsonKey is something like
              {
               "type": "service_account",
                    "  "project_id": "myProject",
                    "  "private_key_id": "myPrivateKey",
                    "  "private_key": "-----BEGIN PRIVATE KEY-----M=-----END PRIVATE KEY-----",
                    "  "client_email": "myEmail",
                    "  "client_id": "11",
                    "  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                    "  "token_uri": "https://oauth2.googleapis.com/token",
                    "  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                    "  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/",
                    "  "universe_domain": "googleapis.com"
                }
             */

            logger.debug("Start connecting to Google Drive");

            // Convert the JSON string to an InputStream
            if (getStorageDefinition().fileStorageComplement == null)
                throw new Exception("Incomplete configuration");
            String jsonKey = getStorageDefinition().fileStorageComplement.toString();
            ByteArrayInputStream credentialStream = new ByteArrayInputStream(jsonKey.getBytes(StandardCharsets.UTF_8));

            // Load credentials
            GoogleCredential credential = GoogleCredential.fromStream(credentialStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

            // Build the Drive service
            Drive drive = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();
            logger.info("Connected to Google drive in {} ms", System.currentTimeMillis() - beginExecution);
            return drive;

        } catch (Exception e) {
            logger.error("Can't connect to Google Driver ", e);
            throw e;
        }
    }
}
