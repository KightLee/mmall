package com.mmall.service.impl;

import com.mmall.service.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * create by lixing on 2019/4/8 10:00
 */
public class IFileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(IFileServiceImpl.class);
    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();
        //获取后缀名
        String fileExtensionName = fileName.substring(fileName.indexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件{},{},{}",fileName,path,uploadFileName);
        File fileDir = new File(path);

        return null;
    }
}
