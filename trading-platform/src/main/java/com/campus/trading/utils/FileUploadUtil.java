package com.campus.trading.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload.path:D:/upload/}")
    private String uploadPath;

    /**
     * 上传图片
     * @param file 上传的文件
     * @return 文件访问路径
     */
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        // 获取原文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成新文件名（UUID + 后缀）
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 创建目录
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            File destFile = new File(uploadPath + newFileName);
            file.transferTo(destFile);
            // 返回访问路径
            return "/upload/" + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}