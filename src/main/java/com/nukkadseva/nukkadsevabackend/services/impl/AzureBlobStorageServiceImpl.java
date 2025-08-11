package com.nukkadseva.nukkadsevabackend.services.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.nukkadseva.nukkadsevabackend.services.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "azure")
public class AzureBlobStorageServiceImpl implements AzureBlobStorageService {

    @Autowired
    private BlobContainerClient blobContainerClient;

    @Override
    public String uploadFile(MultipartFile file, String type) {
        String newFileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        BlobClient blobClient = blobContainerClient.getBlobClient(newFileName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Azure Blob Storage", e);
        }

        return blobClient.getBlobUrl();
    }
}
