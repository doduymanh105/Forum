package com.example.forum.feature.media;

import com.example.forum.feature.media.dto.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    UploadResponseDto uploadImage(MultipartFile file);

    List<UploadResponseDto> uploadImages(List<MultipartFile> files);

    void deleteImage(String publicId);
}
