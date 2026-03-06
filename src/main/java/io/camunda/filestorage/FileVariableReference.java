/* ******************************************************************** */
/*                                                                      */
/*  FileVariableReference                                               */
/*                                                                      */
/*  This object carry the reference to a file variable, not the file    */
/*  itself. This is used as a process variable.                         */
/*  The content may be retrieved via the FileVariableFactory            */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.filestorage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.document.Document;
import io.camunda.connector.api.document.DocumentReference;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.filestorage.storage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.Map;

/**
 * A FileContainer must be self-description. So, it contains the way to find the file itself (JSON, FOLDER...) and the information to access the content
 */
public class FileVariableReference {
    private static final Logger logger = LoggerFactory.getLogger(FileVariableReference.class.getName());

    /**
     * Storage definition where the file is. It is a String to simplify the encoding.
     */
    public String storageDefinition;
    /**
     * content to retrieve the file in the storageDefinition (key to access it)
     */
    public Object content;
    public DocumentReference camundaReference = null;
    /**
     * original Filename is one was provided
     */
    public String originalFileName;
    /**
     * Application decode the string, and save the complete description in the object.
     * The caller can have some complement for the storage definition, which are not in the string, but in the object
     */
    private StorageDefinition storageDefinitionObject = null;

    /**
     * Transform the reference from an object
     *
     * @param fileReference file Reference in JSON or a Object (from the Camunda Storage)
     * @return a FileVariableReference
     * @throws Exception when an error arrived
     */
    public static FileVariableReference fromObject(Object fileReference) throws Exception {
        try {
            DocumentReference documentReference = null;
            if (fileReference instanceof AbstractList fileReferenceList
                    || fileReference instanceof Document
                    || fileReference instanceof DocumentReference) {
                documentReference = getCamundaReferenceFromObject(fileReference);
            }
            // check if this is a Camunda document
            if (documentReference != null) { // && fileReferenceMap.containsKey("camunda.document.type")) {
                // this is a Camunda document!
                FileVariableReference fileVariableReference = new FileVariableReference();
                fileVariableReference.camundaReference = documentReference;
                fileVariableReference.storageDefinition = StorageDefinition.StorageDefinitionType.CAMUNDA.toString();
                fileVariableReference.storageDefinitionObject = new StorageDefinition(StorageDefinition.StorageDefinitionType.CAMUNDA);
                return fileVariableReference;
            }

            // read a classical way
            if (fileReference instanceof String fileReferenceSt) {
                try {
                    FileVariableReference fileVariableReference = new ObjectMapper().readValue(fileReferenceSt, FileVariableReference.class);
                    fileVariableReference.storageDefinitionObject = StorageDefinition.decodeFromString(fileVariableReference.storageDefinition);
                    return fileVariableReference;
                } catch (Exception e) {
                    // Do nothing, try the next option
                }
                // do a second tentative before logging
                String fileReferenceJsonWithoutBackslash = fileReferenceSt.replace("\\\"", "\"");
                try {
                    // if the value is given explicitly, the modeler imposes to \ each ", so we have to replace all \" by "
                    FileVariableReference fileVariableReference = new ObjectMapper().readValue(fileReferenceJsonWithoutBackslash, FileVariableReference.class);
                    fileVariableReference.storageDefinitionObject = StorageDefinition.decodeFromString(fileVariableReference.storageDefinition);
                    return fileVariableReference;
                } catch (Exception e) {
                    // then now we have to log the error
                    logger.error("FileStorage.FileVariableReference.fromJson {} During UnSerialize[{}], secondTentative[{}]",
                            e,
                            fileReferenceSt,
                            fileReferenceJsonWithoutBackslash);
                    throw e;
                }
            } else if (fileReference instanceof Map fileReferenceMap) {
                try {

                    // Attention, if the fileVariable contains a CamundaDocument, conversion will fail
                    if (fileReferenceMap.containsKey("camundaReference")
                            && StorageDefinition.StorageDefinitionType.CAMUNDA.toString().equals(fileReferenceMap.get("storageDefinition"))) {
                        FileVariableReference fileVariableReference = new FileVariableReference();
                        fileVariableReference.camundaReference = getCamundaReferenceFromObject(fileReferenceMap.get("camundaReference"));
                        fileVariableReference.storageDefinition = StorageDefinition.StorageDefinitionType.CAMUNDA.toString();
                        fileVariableReference.storageDefinitionObject = StorageDefinition.decodeFromString(fileVariableReference.storageDefinition);
                        fileVariableReference.originalFileName = (String) fileReferenceMap.get("originalFileName");
                        return fileVariableReference;
                    } else {
                        // Be tolerant on previous data
                        ObjectMapper mapper = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                                .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);

                        FileVariableReference fileVariableReference = mapper.convertValue(fileReferenceMap, FileVariableReference.class);
                        fileVariableReference.storageDefinitionObject = StorageDefinition.decodeFromString(fileVariableReference.storageDefinition);
                        return fileVariableReference;
                    }
                } catch (Exception e) {
                    // then now we have to log the error
                    logger.error("FileStorage.FileVariableReference.fromMap {} During UnSerialize[{}]",
                            e,
                            fileReferenceMap);
                    throw e;
                }


            }

            // unknown format
            logger.error("FileStorage.FileVariableReference.fromInput: unknown format[{}]", fileReference);
            throw new Exception("Unknown format for FileReference");
        } catch (Exception e) {
            logger.error("FileStorage.FileVariableReference.fromInput:source[{}]  exception ", fileReference, e);
            throw e;
        }
    }

    /**
     * The reference can come as a DocumentReference or as a Document. The library wants to manage only the reference
     *
     * @param reference the input
     * @return the documentReference if it exists, else null
     */
    @JsonIgnore
    private static DocumentReference getCamundaReferenceFromObject(Object reference) throws Exception {
        if (reference instanceof Document camundaDocument) {
            return camundaDocument.reference();
        } else if (reference instanceof DocumentReference camundaReference) {
            return camundaReference;
        } else if (reference instanceof AbstractList referenceList) {
            if (referenceList.size() != 1) {
                throw new Exception("FileStorage.FileVariableReference expect only one item in array, detected " + referenceList.size());
            }
            Document camundaDocument = (Document) referenceList.get(0);
            return camundaDocument.reference();
        }

        return null;
    }

    /**
     * Must be static to not make any trouble in the serialization/deserialization
     *
     * @return information, to log it for example
     */
    @JsonIgnore
    public static String getInformation(FileVariableReference fileVariableReference, OutboundConnectorContext
            outboundConnectorContext) {
        StringBuilder result = new StringBuilder();
        try {
            StorageDefinition storageDefinition = StorageDefinition.decodeFromString(fileVariableReference.storageDefinition);
            result.append(storageDefinition.getInformation());
        } catch (Exception e) {
            result.append("Can't get storageDefinition from [");
            result.append(fileVariableReference.storageDefinition);
            result.append("] : ");
            result.append(e.getMessage());
        }
        result.append(": ");
        if (fileVariableReference.content == null)
            result.append("null");
        else if (fileVariableReference.content.toString().length() < 100)
            result.append(fileVariableReference.content.toString());
        else
            result.append(fileVariableReference.content.toString(), 0, 100);
        return result.toString();
    }

    /**
     * Check if the reference is a CamundaReference or nor
     * Not generate this in the JSON
     *
     * @return true if the reference is Camunda documentation
     */
    @JsonIgnore
    public boolean isCamundaDocument() {
        return camundaReference != null;
    }

    /**
     * Transform the fileReference to JSON
     *
     * @return the Json
     * @throws JsonProcessingException in any error
     */
    public String toJson() throws JsonProcessingException {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("FileStorage.FileVariableReference.toJson: exception " + e + " During serialize fileVariable");
            throw e;
        }
    }

    public String getStorageDefinition() {
        return storageDefinition;
    }

    /**
     * Return the storageDefinitionObject
     *
     * @return the storage definition
     * @throws Exception in case the storageObject can't be found from the storageDefintion
     */
    public StorageDefinition getStorageDefinitionObject() throws Exception {
        if (storageDefinitionObject != null) {
            return storageDefinitionObject;
        }
        if (storageDefinition != null) {
            return StorageDefinition.decodeFromString(storageDefinition);
        }
        return null;
    }

    public void setStorageDefinitionObject(StorageDefinition storageDefinitionObject) {
        this.storageDefinitionObject = storageDefinitionObject;
    }

    public Object getContent() {
        return content;
    }

    /**
     * Return the orignal filename behind the reference
     * @return
     */
    public String getFileName() {
        if (isCamundaDocument()) {
            if (camundaReference instanceof DocumentReference.CamundaDocumentReference camundaDocumentReference)
               return camundaDocumentReference.getMetadata().getFileName();
            return null;
        }
        if ( originalFileName != null)
            return originalFileName;

        // It maybe a URL document? Then get it
        if (getContent()!=null) {
            String source = getContent().toString();
            // maybe come from an URL? No file name in that situation
            String fileName = URLDecoder.decode(source.substring(source.lastIndexOf('/') + 1), StandardCharsets.UTF_8);
            return fileName;
        }

        return null;

    }
}
