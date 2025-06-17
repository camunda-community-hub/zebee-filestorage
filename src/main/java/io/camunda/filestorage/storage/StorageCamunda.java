package io.camunda.filestorage.storage;

import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.document.Document;
import io.camunda.document.store.DocumentCreationRequest;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.zeebe.client.api.response.DocumentReferenceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageCamunda extends Storage {
    Logger logger = LoggerFactory.getLogger(StorageCamunda.class.getName());

    OutboundConnectorContext outboundConnectorContext;

    @Override
    public String getName() {
        return "CAMUNDA";
    }

    public StorageCamunda(OutboundConnectorContext outboundConnectorContext, StorageDefinition storageDefinition, FileRepoFactory fileRepoFactory) {
        super(storageDefinition, fileRepoFactory);
        this.outboundConnectorContext = outboundConnectorContext;
    }

    @Override
    public FileVariableReference toStorage(FileVariable fileVariable, FileVariableReference fileVariableReference) throws Exception {

        DocumentCreationRequest documentCreation = DocumentCreationRequest.from(fileVariable.getValue())
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
        StorageDefinition storageDefinition = new StorageDefinition();
        storageDefinition.type = StorageDefinition.StorageDefinitionType.CAMUNDA;
        fileVariable.setStorageDefinition(storageDefinition);
        if (fileVariableReference.camundaReference instanceof DocumentReferenceResponse documentReferenceResponse) {
            fileVariable.setName(documentReferenceResponse.getMetadata().getFileName());
            fileVariable.setMimeType(documentReferenceResponse.getMetadata().getContentType());
        }


        fileVariable.setValue(document.asByteArray());

        return fileVariable;
    }

    @Override
    public boolean purgeStorage(FileVariableReference fileVariableReference) throws Exception {

        return false;
    }
}
