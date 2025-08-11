package com.nukkadseva.nukkadsevabackend.services.impl;

import com.nukkadseva.nukkadsevabackend.services.AzureBlobStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements AzureBlobStorageService {

    private final String uploadDir;

    public LocalFileStorageServiceImpl() {
        // Use absolute path based on current working directory
        this.uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        // Create uploads directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null");
        }

        try {
            // Create a unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown";
            }
            String fileName = UUID.randomUUID().toString() + "-" + originalFilename;

            // Create type-specific subdirectory
            Path typeDir = Paths.get(uploadDir, type);
            if (!Files.exists(typeDir)) {
                Files.createDirectories(typeDir);
                System.out.println("Created subdirectory: " + typeDir.toAbsolutePath());
            }

            // Save the file
            Path filePath = typeDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            System.out.println("File saved successfully: " + filePath.toAbsolutePath());

            // Return a local URL (you can modify this to match your needs)
            return "/uploads/" + type + "/" + fileName;

        } catch (IOException e) {
            System.err.println("Failed to store file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to store file locally: " + e.getMessage(), e);
        }
    }
}
