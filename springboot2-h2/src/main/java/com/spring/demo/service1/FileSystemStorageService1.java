package com.spring.demo.service1;

import com.spring.demo.entity1.FileEntity;

import java.nio.file.Path;

/**
 * com.spring.service
 * @author jacky
 * @date 2017/12/23
 **/
public interface FileSystemStorageService1 {

    /**
     * 初始化存储路径
     */
    void init();

    /**
     * 从磁盘加载指定文件
     * @param filename
     * @return
     */
    Path load(String filename);

    /**
     * 将文件缓存到磁盘
     * @param fileEntity
     */
    void store(FileEntity fileEntity);
}
