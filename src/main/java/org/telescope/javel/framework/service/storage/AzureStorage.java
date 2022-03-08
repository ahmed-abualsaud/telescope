
package org.telescope.javel.framework.service.storage;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.server.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AzureStorage {

    private static Config config;
    private static Logger LOGGER;
    private static String azureName, azureKey, azureContainer;
    private static String connectStr;
    private static CloudBlobContainer container;
    
    static {
        config = Context.getConfig();
        LOGGER = LoggerFactory.getLogger(AzureStorage.class);
        azureName = config.getString(Keys.AZURE_STORAGE_NAME);
        azureKey = config.getString(Keys.AZURE_STORAGE_KEY);
        azureContainer = config.getString(Keys.AZURE_STORAGE_CONTAINER);
        connectStr = "DefaultEndpointsProtocol=https;" + "AccountName=" + azureName + ";" + "AccountKey=" + azureKey;
        try { container = CloudStorageAccount.parse(connectStr).createCloudBlobClient().getContainerReference(azureContainer);
        } catch (URISyntaxException | StorageException | InvalidKeyException e) {
            LOGGER.error("Can not get azure container -> Error: " + e.getMessage(), e);
        }
    }
    
    public static void uploadFile(String sourceFile, String destFile) {
        try {
            container.getBlockBlobReference(destFile).uploadFromFile(sourceFile);
            LOGGER.info("Azure: file " + sourceFile + " uploaded successfully");
        } catch (URISyntaxException | StorageException | IOException e) {
            LOGGER.error("Can not upload file -> Error: "  + e.getMessage(), e);
        }
    }
    
    public static void downloadFile(String sourceFile, String destFile) {
        try {
            container.getBlockBlobReference(sourceFile).downloadToFile(createFileIfNotExists(destFile));
            LOGGER.info("Azure: file " + sourceFile + " downloaded successfully");
        } catch (URISyntaxException | StorageException | IOException e) {
            LOGGER.error("Can not download file -> Error: "  + e.getMessage(), e);
        }
    }
    
    public static void listFiles(String dirName) {
        try {
            for (ListBlobItem blobItem : container.getDirectoryReference(dirName).listBlobs()) {
                LOGGER.info("URI of blob is: " + blobItem.getUri());
            }
        } catch (URISyntaxException | StorageException e) {
            LOGGER.error("Can not list file -> Error: "  + e.getMessage(), e);
        }
    }
    
    public static void listAllFiles() {
        try {
            for (ListBlobItem blobItem : container.listBlobs()) {
                LOGGER.info("URI of blob is: " + blobItem.getUri());
            }
        } catch (Exception e) {
            LOGGER.error("Can not list file -> Error: "  + e.getMessage(), e);
        }
    }
    
    private static String createFileIfNotExists(String absoluteName) throws IOException {
        Files.createDirectories(Paths.get(new File(absoluteName).getParent()));
        return new File(absoluteName).getAbsolutePath();
    }
    
}
