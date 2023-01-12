# zebee-cherry-filestorage

Library to manipulate File process variable, and store the content in different storage (Folder, CMIS...)

This library is used by connectors. There is three main function 


# Store file to the storage

Get an instance of the factory

````
  FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
````

Create a FileVariable. Upload the file in the FileVariable object

````
FileVariable fileVariable = new FileVariable();
byte[] contentInByte = <Content In Byte>
fileVariable.setValue( contentInByte );
fileVariable.setName( "MyDocumentName.pdf");
fileVariable.setMimeType( FileVariable.getMimeTypeFromName(fileVariable.getName()));
`````

Set the Storage Definition. The Storage definition describe where the core document is stored.


````
    StorageDefinition storageDefinition = StorageDefinition.getFromString(storageDefinitionSt);
    
    fileVariable.setStorageDefinition(storageDefinition);
````

See below the different Storage definition available


Save the File Variable. The function return a FileVariableReference


````
    FileVariableReference fileVariableReference = fileRepoFactory.saveFileVariable(fileVariableValue);
````

The fileVariableReference is only a reference, and can be saved in the process variable

````
    String processVariableSt = fileVariableReference.toJson()
    // save the processVariableSt as a process variable
````

# Read file from the storage

To read a file from the storage, the first operation consist to access the FileVariableReference

````
    String processVariableSt = <GetFromProcessVariable>
    FileVariableReference fileVariableReference = FileVariableReference.fromJson( processVariableSt );
````

Then get the FileVariable from the repository

````
  FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
  FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference)
````

Content, fileName, MimeType are available via method.

The storage definition is stored in the FileVariable, so to update it, change the content and store it again.

# Purge a fileVariable
From the FileVariableReference, it is possible to purge the document in the storage.

````
    String processVariableSt = <GetFromProcessVariable>
    FileVariableReference fileVariableReference = FileVariableReference.fromJson( processVariableSt );

    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    boolean filePurged = fileRepoFactory.purgeFileVariable(fileVariableReference);
````

# Storage definition
Different storage definition are available. The storage store the core of the document.

## JSON

The file is saved in JSON, and the FileVariableReference contains the complete document. This is very efficient, but Zeebe limited the size of a process to 4 Mb. This solution is not possible to saved documents.

Storage Definition Key: "JSON"

````
import StorageJSON;

StorageCMIS.getStorageDefinitionString()
````


## Temporary folder

The file is saved in the Temporary folder of the host. 
If multiple applications (different connectors) needs to access the file, this is not an acceptable document, except if all application are hosted by the same machine (or the same Pod)

Storage Definition Key: "TEMPFOLDER"

````
import StorageTempFolder;

StorageCMIS.getStorageDefinitionString()
````

## Folder
The file is saved in the folder given in the connection string. 
If multiple applications needs to access the file, the folder must be visible and share on the same place (/mnt/filestoreage" for example).

Storage Definition Key: "FOLDER:/mnt/filestorage"


````
import StorageFolder;

StorageCMIS.getStorageDefinitionString(String folder)
````


## CMIS

The file is saved in a CMIS tool. The connection to the tool (Url, Repository name, username, password) must be provide. 
The folder where the file must be store must be provide too.

Storage Definition Key: "CMIS:{"url":"<url>", "repositoryName":"<repositoryName>", "userName": "<userName>", "password": "<password>", "storageDefinitionFolder":"<folder>"}"

The static methode is available in the StorageCMIS class

````
import StorageCMIS;

StorageCMIS.getStorageDefinitionString(String url, String repositoryName, String userName, String password, String storageDefinitionFolder)
````
