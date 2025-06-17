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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.document.CamundaDocument;
import io.camunda.document.reference.DocumentReference;
import io.camunda.filestorage.storage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;

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
     * Application decode the string, and save the complete description in the object.
     * The caller can have some complement for the storage definition, which are not in the string, but in the object
     */
    public StorageDefinition storageDefinitionObject = null;


    /**
     * content to retrieve the file in the storageDefinition (key to access it)
     */
    public Object content;

    public DocumentReference camundaReference = null;


    /**
     * original Filename is one was provided
     */
    public String originalFileName;

    public boolean isCamundaDocument() {
        return camundaReference != null;
    }

    /**
     * Transform the reference from JSON
     *
     * @param fileReference file Reference in JSON or a Object (from the Camunda Storage)
     * @return a FileVariableReference
     * @throws Exception when an error arrived
     */
    public static FileVariableReference fromInput(Object fileReference) throws Exception {
        try {
            DocumentReference documentReference = null;
            if (fileReference instanceof AbstractList fileReferenceList) {
                if (fileReferenceList.size() != 1) {
                    throw new Exception("FileStorage.FileVariableReference expect only one item in array, detected " + fileReferenceList.size());
                }
                CamundaDocument camundaDocument = (CamundaDocument) fileReferenceList.get(0);
                documentReference = camundaDocument.reference();
            } else if (fileReference instanceof CamundaDocument camundaDocument) {
                documentReference = camundaDocument.reference();
            } else if (fileReference instanceof DocumentReference) {
                documentReference = (DocumentReference) fileReference;
            }
            // check if this is a Camunda document
            if (documentReference != null) { // && fileReferenceMap.containsKey("camunda.document.type")) {
                // this is a Camunda document!
                FileVariableReference fileVariableReference = new FileVariableReference();
                fileVariableReference.camundaReference = documentReference;
                fileVariableReference.storageDefinition = StorageDefinition.StorageDefinitionType.CAMUNDA.toString();
                fileVariableReference.storageDefinitionObject = new StorageDefinition();
                return fileVariableReference;
            }

            // read a classical way
            if (fileReference instanceof String fileReferenceSt) {
                try {

                    FileVariableReference fileVariableReference = new ObjectMapper().readValue(fileReferenceSt, FileVariableReference.class);
                    fileVariableReference.storageDefinitionObject = StorageDefinition.getFromString(fileVariableReference.storageDefinition);
                    return fileVariableReference;
                } catch (Exception e) {
                }
                // do a second tentative before logging
                String fileReferenceJsonWithoutBackslash = fileReferenceSt.replace("\\\"", "\"");
                try {
                    // if the value is given explicitly, the modeler impose to \ each ", so we have to replace all \" by "
                    FileVariableReference fileVariableReference= new ObjectMapper().readValue(fileReferenceJsonWithoutBackslash, FileVariableReference.class);
                    fileVariableReference.storageDefinitionObject = StorageDefinition.getFromString(fileVariableReference.storageDefinition);
                    return fileVariableReference;
                } catch (Exception e) {
                    // then now we have to log the error
                    logger.error("FileStorage.FileVariableReference.fromJson {} During UnSerialize[{}], secondTentative[{}]",
                            e,
                            fileReferenceSt,
                            fileReferenceJsonWithoutBackslash);
                    throw e;
                }
            }
            // unknown format
            logger.error("FileStorage.FileVariableReference.fromInput: unknown format[{}]", fileReference);
            throw new Exception("Unknown format for FileReference");
        }
        catch(Exception e) {
            logger.error("FileStorage.FileVariableReference.fromInput:source[{}]  exception ", fileReference,e);
            throw e;
        }
    }


        /**
         * Transform the fileReference to JSON
         *
         * @return the Json
         * @throws JsonProcessingException in any error
         */
        public String toJson () throws JsonProcessingException {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                logger.error("FileStorage.FileVariableReference.toJson: exception " + e + " During serialize fileVariable");
                throw e;
            }
        }

        public String getStorageDefinition () {
            return storageDefinition;
        }

        public Object getContent () {
            return content;
        }

        /**
         * Must be static to not make any trouble in the serialization/deserialization
         *
         * @return information, to log it for example
         */
        public static String getInformation (FileVariableReference fileVariableReference, OutboundConnectorContext
        outboundConnectorContext){
            StringBuilder result = new StringBuilder();
            try {
                StorageDefinition storageDefinition = StorageDefinition.getFromString(fileVariableReference.storageDefinition);
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
                result.append(fileVariableReference.content.toString().substring(0, 100));
            return result.toString();
        }
    }
