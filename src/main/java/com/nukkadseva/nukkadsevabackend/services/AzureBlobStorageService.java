package com.nukkadseva.nukkadsevabackend.services;

import org.springframework.web.multipart.MultipartFile;

public interface AzureBlobStorageService {
    String uploadFile(MultipartFile file, String type);
}

