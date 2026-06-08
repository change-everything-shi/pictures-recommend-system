package com.ssjheima.service.impl;

import com.ssjheima.service.LocalPhotoStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalPhotoStorageServicelmpl implements LocalPhotoStorageService {

    @Override
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalName = file.getOriginalFilename();
        String ext = extractExt(originalName);
        String name = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(System.getProperty("user.dir"), "uploads/photos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dst = new File(dir, name);
        try {
            file.transferTo(dst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "/uploads/photos/" + name;
    }

    private String extractExt(String filename) {
        if (filename == null) {
            return ".bin";
        }
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return ".bin";
        }
        String ext = filename.substring(i).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return ".bin";
        }
        return ext;
    }
}

