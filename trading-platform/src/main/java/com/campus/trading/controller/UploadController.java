package com.campus.trading.controller;

import com.campus.trading.utils.FileUploadUtil;
import com.campus.trading.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        // 上传文件
        String fileUrl = fileUploadUtil.uploadImage(file);
        if (fileUrl == null) {
            return Result.error("上传失败");
        }

        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        return Result.success("上传成功", data);
    }
}