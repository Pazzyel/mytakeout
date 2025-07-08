package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.constant.UploadConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/admin/common/upload")
    @ApiOperation("文件上传接口")
    public Result upload(MultipartFile file) {
        log.info("文件上传: {}", file);
        try {
            //转换文件名为唯一
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            File newfile = new File(UploadConstant.UPLOAD_PATH, fileName);
            log.info("正在上传文件");
            file.transferTo(newfile);//保存到指定位置
            return Result.success(UploadConstant.REQUEST_PATH + "/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败: {}",e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

    /**
     * 请求文件资源
     * @param filename
     * @param response
     */
    @GetMapping("/uploads/{filename}")
    public void getFile(@PathVariable String filename, HttpServletResponse response) {
        try {
            File file = new File(UploadConstant.UPLOAD_PATH + "/" + filename);
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            if (file.exists()) {
                response.setContentType(contentType);
                Files.copy(file.toPath(), response.getOutputStream());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
