package com.nukkadseva.nukkadsevabackend.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.nukkadseva.nukkadsevabackend.service.AzureBlobStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import com.azure.storage.blob.models.PublicAccessType;
import com.nukkadseva.nukkadsevabackend.exception.FileSizeExceededException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidFileTypeException;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "azure")
public class AzureBlobStorageServiceImpl implements AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;
    private final String defaultContainerName;

    public AzureBlobStorageServiceImpl(BlobServiceClient blobServiceClient,
            @Value("${spring.cloud.azure.storage.blob.container-name:nukkadseva-files}") String defaultContainerName) {
        this.blobServiceClient = blobServiceClient;
        this.defaultContainerName = defaultContainerName;
    }

    @Override
    public String uploadFile(MultipartFile file, String type) {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException(
                    "Invalid file type. Only standard images (JPEG, PNG, WebP) are allowed.");
        }

        // Validate file size (max 200KB = 200 * 1024 bytes = 204800 bytes)
        long maxSizeInBytes = 204800;
        if (file.getSize() > maxSizeInBytes) {
            throw new FileSizeExceededException("File size must be strictly less than 200KB.");
        }
        // Determine container name based on type
        String containerName = defaultContainerName;
        if ("profilePicture".equals(type) || "customerfiles".equals(type)) {
            containerName = "customerfiles";
        } else if ("profilePictures".equals(type) || "document".equals(type) || "serviceprovidersfiles".equals(type)) {
            containerName = "serviceprovidersfiles";
        } else if (type != null && !type.isEmpty()) {
            // For safety mapping
            containerName = type.toLowerCase().replaceAll("[^a-z0-9-]", "");
            if (containerName.length() < 3) {
                containerName = defaultContainerName;
            }
        }

        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
            blobContainerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        } else {
            // Ensure existing containers get public access policy upgraded automatically
            try {
                if (blobContainerClient.getProperties().getBlobPublicAccess() != PublicAccessType.BLOB) {
                    blobContainerClient.setAccessPolicy(PublicAccessType.BLOB, null);
                }
            } catch (Exception e) {
                // Ignore if we lack permissions to read/set policy
            }
        }

        String newFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        BlobClient blobClient = blobContainerClient.getBlobClient(newFileName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Azure Blob Storage", e);
        }

        return blobClient.getBlobUrl();
    }
}
