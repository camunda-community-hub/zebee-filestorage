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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * original Filename is one was provided
   */
  public String originalFileName;
  /**
   * Transform the reference from JSON
   *
   * @param fileReferenceJson file Reference in JSON
   * @return a FileVariableReference
   * @throws Exception when an error arrive
   */
  public static FileVariableReference fromJson(String fileReferenceJson) throws Exception {
    try {
      return new ObjectMapper().readValue(fileReferenceJson, FileVariableReference.class);
    } catch (Exception e) {
    }
    // do a second tentative before logging
    String fileReferenceJsonWithoutBackslash = fileReferenceJson.replace("\\\"", "\"");
    try {
      // if the value is given explicitly, the modeler impose to \ each ", so we have to replace all \" by "
      return new ObjectMapper().readValue(fileReferenceJsonWithoutBackslash, FileVariableReference.class);
    } catch (Exception e) {
      // then now we have to log the error
      logger.error("FileStorage.FileVariableReference.fromJson:" + e + " During UnSerialize[" + fileReferenceJson
          + "], secondTentative[" + fileReferenceJsonWithoutBackslash + "]");
      throw e;
    }
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

  public Object getContent() {
    return content;
  }

  /**
   * Must be static to not make any trouble in the serialization/deserialization
   *
   * @return information, to log it for example
   */
  public static String getInformation(FileVariableReference fileVariableReference) {
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
