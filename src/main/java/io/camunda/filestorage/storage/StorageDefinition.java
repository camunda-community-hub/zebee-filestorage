/* ******************************************************************** */
/*                                                                      */
/*  StorageDefinition                                                   */
/*                                                                      */
/*  The storage definition contains only the description to access the  */
/* storage, and not what it is saved inside.                            */
/* the Storage definition may bne simple (TEMPFOLDER) or complex        */
/*  for CMIS, the complete informatin to connect to the CMIS is part of */
/*  This class just manipulate the information with the format          */
/*  <Type>:<Complement>
/* ******************************************************************** */
package io.camunda.filestorage.storage;

import com.google.gson.Gson;
import io.camunda.filestorage.cmis.CmisParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Why the storageDefinition does not contains the OutboundConnectorContext ?
 * The storage definition is static, define information and not supposed to manipulate objects like the OutboundConnectorContext
 * The goal of the definition is to be saved in JSON in the process, or in the process variable.
 */

public class StorageDefinition {

  public final static String ERROR_INCORRECT_STORAGEDEFINITION = "INCORRECT_STORAGEDEFINITION";
  public static final String STORAGE_DEFINITION_DELIMITATEUR = ":";
  static Logger logger = LoggerFactory.getLogger(StorageDefinition.class.getName());
  public StorageDefinitionType type;
  public String complement = null;
  public Object complementInObject = null;

  /**
   * Filestorage may need complement to connect
   */
  public Object fileStorageComplement;
  /**
   *
   * @param completeStorageDefinition storage definition as a string
   * @return the storageDefinition object
   * @throws Exception
   */
  public static StorageDefinition getFromString(String completeStorageDefinition) throws Exception {
    try {
      int posDelimiter = completeStorageDefinition.indexOf(STORAGE_DEFINITION_DELIMITATEUR);

      String storageTypeSt =
          posDelimiter == -1 ? completeStorageDefinition : completeStorageDefinition.substring(0, posDelimiter);
      StorageDefinition storageDefinition = new StorageDefinition();
      storageDefinition.type = StorageDefinitionType.valueOf(storageTypeSt);

      switch (storageDefinition.type) {
      case FOLDER:
        storageDefinition.complement = completeStorageDefinition.substring(posDelimiter + 1);
        break;
      case CMIS:
        String complement = completeStorageDefinition.substring(posDelimiter + 1);
        Gson gson = new Gson();
        storageDefinition.complementInObject = gson.fromJson(complement, Object.class);
        break;
        case GOOGLEDRIVE:
          // Complement is the folder where to save the data
          // Note: this is not the authorization information, which is store in the fileStorageComplement
          complement = completeStorageDefinition.substring(posDelimiter + 1);
          storageDefinition.complementInObject = complement;
        default:
        break;
      }

      return storageDefinition;
    } catch (Exception e) {
      String message = "Can't decode storageDefinition [" + completeStorageDefinition + "]. Format should be ["
          + StorageDefinitionType.JSON + "|" + StorageDefinitionType.CMIS + "|" + StorageDefinitionType.TEMPFOLDER + "|"
          + StorageDefinitionType.FOLDER + "]";
      logger.error("StorageDefinition: Can't decode [" + completeStorageDefinition + "] " + e);
      throw new Exception(ERROR_INCORRECT_STORAGEDEFINITION + ": " + message);
    }
  }

  /**
   * Encode the current storage definition to a String, so it is easily movable to any information
   *
   * @return the string which encode the storage definition
   */
  public String encodeToString() {
    String result = type.toString();
    if (complement != null) {
      result += STORAGE_DEFINITION_DELIMITATEUR + complement;
    } else if (complementInObject != null) {
      Gson gson = new Gson();
      result += STORAGE_DEFINITION_DELIMITATEUR + gson.toJson(complementInObject);
    }
    return result;
  }

  /**
   * return information on the storage, to log it for example
   *
   * @return information on the storageDefinition
   */
  public String getInformation() {
    StringBuilder info = new StringBuilder();
    info.append(type.toString());
    switch (type) {
    case FOLDER:
      info.append(": folder[");
      info.append(complement);
      info.append("[");
      break;

    case CMIS:
      try {
        CmisParameters cmisParameters = CmisParameters.getCodingConnection(complementInObject);
        info.append(": url[");
        info.append(cmisParameters.url);
        info.append("] repository[");
        info.append(cmisParameters.repositoryName);
        info.append("] userName[");
        info.append(cmisParameters.userName);
        info.append("]");
      } catch (Exception e) {
        info.append("Can't decode Parameter " + e.getMessage());
      }
      break;

    case TEMPFOLDER:
      info.append(": folder[");
      info.append(StorageTempFolder.getTempFolder());
      info.append("]");
      break;

    case URL:
        info.append(": url provided in file");
      break;

    case JSON:
      info.append("");
      break;

      case CAMUNDA:
        break;
    }
    return info.toString();
  }

  /**
   * Define how the file variable is stored.
   * JSON: easy, but attention to large file
   */
  public enum StorageDefinitionType {JSON, TEMPFOLDER, FOLDER, CMIS, URL, CAMUNDA, GOOGLEDRIVE}

}
