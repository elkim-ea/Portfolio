package com.matchaworld.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final AmazonS3 ncpS3Client;

    @Value("${ncp.bucket}")
    private String bucketName;

    @PostMapping("/api/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {

        try {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            InputStream inputStream = file.getInputStream();

            ncpS3Client.putObject(bucketName, fileName, inputStream, metadata);

            String fileUrl = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + fileName;

            return ResponseEntity.ok(new UploadResult(true, "success", fileUrl));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new UploadResult(false, e.getMessage(), null));
        }
    }

    record UploadResult(boolean success, String message, String url) {}
}
