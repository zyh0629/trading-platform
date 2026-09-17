package com.campus.trading.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    // 实验三：测试功能分支合并

    @GetMapping("/hello")
    public String hello() {
        return "Hello! 校园二手交易平台项目启动成功！";
    }
}