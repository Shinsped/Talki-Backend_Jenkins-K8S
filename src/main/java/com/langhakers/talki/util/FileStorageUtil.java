package com.langhakers.talki.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private final Path fileBasePath = Paths.get("./audio_files").toAbsolutePath().normalize();

    public String saveFile(MultipartFile file) throws IOException {
        if (!Files.exists(fileBasePath)) {
            Files.createDirectories(fileBasePath);
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                ext = originalFilename.substring(dotIndex);
            }
        }

        String storedFilename = UUID.randomUUID().toString() + ext;
        Path targetLocation = fileBasePath.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return storedFilename;
    }
}

