package com.ssjheima.service;

import org.springframework.web.multipart.MultipartFile;

public interface LocalPhotoStorageService {
    String save(MultipartFile file);
}

