package io.camunda.filestorage.storage;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.api.document.DocumentCreationRequest;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageCamunda extends Storage {
    public static String STORAGE_CAMUNDA_NAME = "camunda";
    Logger logger = LoggerFactory.getLogger(StorageCamunda.class.getName());
    OutboundConnectorContext outboundConnectorContext;

    public StorageCamunda(OutboundConnectorContext outboundConnectorContext, StorageDefinition storageDefinition, FileRepoFactory fileRepoFactory) {
        super(storageDefinition, fileRepoFactory);
        this.outboundConnectorContext = outboundConnectorContext;
    }

    @Override
    public String getName() {
        return STORAGE_CAMUNDA_NAME;
    }

    @Override
    public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference) throws Exception {

        DocumentCreationRequest documentCreation = DocumentCreationRequest.from(fileVariable.getValueStream())
                .contentType(fileVariable.getMimeType())
                .fileName(fileVariable.getName())
                .build();
        Document document = outboundConnectorContext.create(documentCreation);

        FileVariableReference fileVariableReferenceOutput = new FileVariableReference();
        fileVariableReferenceOutput.storageDefinition = getStorageDefinition().encodeToString();
        fileVariableReferenceOutput.camundaReference = document.reference();


        return fileVariableReferenceOutput;
    }

    @Override
    public FileVariable fromStorage(FileVariableReference fileVariableReference) throws Exception {

        // CamundaDocumentReferenceImpl camundaDoc = new ObjectMapper().readValue(fileVariableReference.camundaReference, CamundaDocumentReferenceImpl.class);
        Document document = outboundConnectorContext.resolve(fileVariableReference.camundaReference);

        FileVariable fileVariable = new FileVariable();
        StorageDefinition storageDefinition = new StorageDefinition(StorageDefinition.StorageDefinitionType.CAMUNDA);
        fileVariable.setStorageDefinition(storageDefinition);
        fileVariable.setName(document.metadata().getFileName());
        fileVariable.setMimeType(document.metadata().getContentType());

        fileVariable.setValueStream(document.asInputStream());

        return fileVariable;
    }

    @Override
    public boolean purgeStorage(FileVariableReference fileVariableReference) throws Exception {

        return false;
    }
}
