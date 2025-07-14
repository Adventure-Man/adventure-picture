package com.adventure.picturebackend.controller;

import com.adventure.picturebackend.common.resp.BaseResponse;
import com.adventure.picturebackend.common.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主控制器
 */
@RestController
@Tag(name = "mainController", description = "提供系统健康检查接口")
public class MainController {
    /**
     * 健康检查
     *
     * @return 响应
     */
    @GetMapping("/health")
    //@CrossOrigin(origins = "*", maxAge = 3600)
    @Operation(summary = "healthCheck", description = "检查系统是否正常")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("Hello World!");
    }
}
