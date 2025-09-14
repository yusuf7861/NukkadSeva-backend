package com.nukkadseva.nukkadsevabackend.util;

import com.nukkadseva.nukkadsevabackend.exception.FileNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.FileSizeExceededException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidFileTypeException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidationUtil {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final String[] ALLOWED_TYPES = { "image/png", "image/jpeg" };

    public static void validateProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileNotFoundException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase(ALLOWED_TYPES[0]) ||
                        contentType.equalsIgnoreCase(ALLOWED_TYPES[1]))) {
            throw new InvalidFileTypeException("Only PNG and JPG images are allowed");
        }
    }
}

