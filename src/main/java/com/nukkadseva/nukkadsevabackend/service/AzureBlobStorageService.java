package com.nukkadseva.nukkadsevabackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface AzureBlobStorageService {
    String uploadFile(MultipartFile file, String type);
}

