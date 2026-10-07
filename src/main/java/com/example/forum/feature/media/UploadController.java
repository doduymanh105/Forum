package com.example.forum.feature.media;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.media.dto.SignatureResponse;
import com.example.forum.feature.media.dto.UploadResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Upload API")
@RestController
@RequestMapping("/forum/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;

    @RateLimit(capacity = 3, time = 1)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponseDto>> upload(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upload avatar successfully",
                        cloudinaryService.uploadImage(file)
                )
        );
    }

    @RateLimit(capacity = 3, time = 1)
    @PostMapping(value ="/post-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<UploadResponseDto>>> uploadPostMedia(
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Upload post media successfully",
                        cloudinaryService.uploadImages(files)
                )
        );
    }

    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<SignatureResponse>> getSignature() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "upload signature get",
                        cloudinaryService.getSignatureUpload()
                )
        );
    }
}
