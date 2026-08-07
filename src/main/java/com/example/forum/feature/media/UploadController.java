package com.example.forum.feature.media;

import com.example.forum.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/forum/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upload avatar successfully",
                        cloudinaryService.uploadImage(file)
                )
        );
    }

    @PostMapping(value ="/post-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPostMedia(
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upload post media successfully",
                        cloudinaryService.uploadImages(files)
                )
        );
    }
}
