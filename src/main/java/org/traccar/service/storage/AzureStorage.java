/*
 * Copyright 2017 Anton Tananaev (anton@traccar.org)
 * Copyright 2017 Andrey Kunitsyn (andrey@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.service.storage;

import org.traccar.Context;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;
import org.traccar.config.Config;
import org.traccar.config.Keys;
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
