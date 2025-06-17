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
            String jsonKey = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"pierre-yves\",\n" +
                    "  \"private_key_id\": \"3e314775f4c90d82980d6666b7e3953c0e7884ef\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDkykT9Z5swSS51\\nhQb6Ly4tqXr2eQdC4WyPptkbvINZJXOXQWXNYEBcwM9Gvw9WymClqx/6yjtBXuai\\nSnKDCTqoLNVpemyU8Y8Kk37tdbpqtlIx73hqDcy0P0V25R1H/mwGQKflF3HS0aqk\\nkdB4cj6fNAW2S6tS1PA7sSkZSvBFG53R5sRuvaKsC0sxw+q/OKTLynDT0GuPcvUe\\nUm+04ARdA4Rye6ymXSEW3v//WCLkdbH+Kfho3Y+C4nNb1Mi0FlxeVSqS9y8alnZL\\ntGtbhItFZjOhWaN5R9KoW7RUQVV9+vbuZg+aSt8Ggcall42dBZvg3DQ8HR81gFvm\\nsOOjo2V7AgMBAAECggEACSSYKzRHvF9ATwv44mieRM7gDhd6Fc8iYn89b+FRsm9P\\ntjyzz8hH6iz+W4ppF4PQ5u32wjPwuEUNnHCzMM9em7fm82ckDbO1jqukR+MLhxYw\\nJLS4MCzLAG0C3qR7ThQ5xuowi3QrZUxhsF/vX9DY9c3DaKyPdSjzTL9gsBURkwFb\\nLU0AcWD3p+1AuBx9ITO0FxDmKRjB7mHkElFgqN+oof/FIp8ipT+Ep2LWPhSQDv7+\\nfsEjquil0rYILDC7RsU4KRQM/C3cIh5GhPhLZbWhDEWRPMSrdMm3XB8BPynuqSft\\n7fqZoSpwroym6hp+dm2vE3qpcHNIJ40zJkxNuF06qQKBgQD3LNmkYnPnkTZxLFTL\\nfGnIWYowUf5ioxXp47fPoG29msvOjYfHGggCTDJ7WoXfXAbhOs4XShRvPRikUMUt\\nnBiOYV7eIR0FiSpC96e8pZuRX0Tq0U7iaYvoY59SxAmbxZ+VNUM+a9SB0fnNa6a8\\nkf0PiIV0br9cfxu3uCpgVzfqPwKBgQDs9WHMoeYfgGZVicN0VM9SQRXOutCp5jkH\\nTHKmHZp/rlXmuswWYmIIUbZtSlOR3yEn+m0jrnF5FvtHvUN2x8pMcabS7CYt3v53\\nPRjdbvFn+NZcO0eg9KQl6QNoSpqprlQ84aY6SFvKruDHykX/L/evtrDIS5XO8nkZ\\nhwpGPS0dxQKBgQDcvohMv8RlXZFydzXvWNgcHqNETiXSr3VloYJKAiuftnwnpsxI\\n6x2V97jp40lF1ikqwtrFf9pKEhVMwfmmpw5jMeCInqgNhpdgoU6DMp+Br8SbtXwu\\nxjY1v6rNPtiZ7l974MqCF8j8e6sNYwPQysxnL/SHrFRoZfg6FlZ6HcqI/wKBgQCk\\nooDGyp22uWFggQ0Z4GsDPVFHhyi/QsPQvF7T57GWkKwCWTq/Oq/eE9fp60BRE3RX\\n8Hiv193jQgJof6lF994oarZ7ybNlH5AxjHOgNhroIE3fWxiTTiZWaKUDawI2bnb8\\nrdLun6OXGRX3+iPT/6HZpdcB8vItDO7yu4556rtT+QKBgQDQo40lKGfQf+HuOv8U\\njIRPiSOIaaigGR377Fi8tB8k2vNl4KKTIjrXquFTIVZYDNX15PD9ZLMn9GdC0uJz\\nJw5xKhTWXNtd4L0d9syy6OWVTAMXpc5sVE9fe19fRwVmd+ot/2MBa6EASFxiNXJ6\\nNKEWtS1yqrJvK+um5kbDxUCFRA==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"camunda8-backup-account-202405@pierre-yves.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105599727109775553053\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/camunda8-backup-account-202405%40pierre-yves.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"; // full JSON key here

            logger.debug("Start connecting to Google Drive");

            // Convert the JSON string to an InputStream
            if (getStorageDefinition().fileStorageComplement != null)
                jsonKey = getStorageDefinition().fileStorageComplement.toString();
            ByteArrayInputStream credentialStream = new ByteArrayInputStream(jsonKey.getBytes(StandardCharsets.UTF_8));

            // Load credentials
            GoogleCredential credential = GoogleCredential.fromStream(credentialStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

            // Build the Drive service
            Drive drive= new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();
            logger.info("Connected to Google drive in {} ms", System.currentTimeMillis()-beginExecution);
            return drive;

        } catch (Exception e) {
            logger.error("Can't connect to Google Driver ", e);
            throw e;
        }
    }
}
