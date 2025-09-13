package com.nukkadseva.nukkadsevabackend.util;

import com.nukkadseva.nukkadsevabackend.service.AzureBlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements AzureBlobStorageService {

    private final String uploadDir;

    public LocalFileStorageService() {
        // Use absolute path based on current working directory
        this.uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        // Create uploads directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", e.getMessage());
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
                log.info("Created subdirectory: {}", typeDir.toAbsolutePath());
            }

            // Save the file
            Path filePath = typeDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            log.info("File saved successfully: {}", filePath.toAbsolutePath());

            // Return a local URL (you can modify this to match your needs)
            return "/uploads/" + type + "/" + fileName;

        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to store file locally: " + e.getMessage(), e);
        }
    }
}
