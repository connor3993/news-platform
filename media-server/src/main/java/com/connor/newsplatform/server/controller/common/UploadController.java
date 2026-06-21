package com.connor.newsplatform.server.controller.common;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.common.utils.OssUtil;
import com.connor.newsplatform.pojo.vo.UploadVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common")
public class UploadController {
    private final OssUtil ossUtil;

    public UploadController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @LogRecord("上传文件")
    @PostMapping("/upload")
    public Result<UploadVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(new UploadVO(ossUtil.upload(file)));
    }
}
