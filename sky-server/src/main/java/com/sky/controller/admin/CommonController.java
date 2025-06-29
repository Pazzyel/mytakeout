package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.constant.UploadConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
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
            return Result.success(UploadConstant.UPLOAD_PATH + "\\" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败: {}",e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
