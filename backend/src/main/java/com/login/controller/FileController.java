package com.login.controller;

import com.login.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "File is empty");
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf(".")) : "";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            return Result.error(500, "File upload failed: " + e.getMessage());
        }
        return Result.success("/uploads/" + fileName);
    }
}
