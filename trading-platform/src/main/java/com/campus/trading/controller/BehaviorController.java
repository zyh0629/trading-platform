package com.campus.trading.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.BehaviorLog;
import com.campus.trading.mapper.BehaviorLogMapper;
import com.campus.trading.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/behavior")
public class BehaviorController {

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    @GetMapping("/favorites/{userId}")
    public Result<List<BehaviorLog>> getUserFavorites(@PathVariable Integer userId) {
        QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("behavior_type", 2)
                .orderByDesc("create_time");
        return Result.success(behaviorLogMapper.selectList(wrapper));
    }

    @GetMapping("/favorite/check")
    public Result<Boolean> checkFavorite(@RequestParam Integer userId, @RequestParam Integer productId) {
        QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("product_id", productId)
                .eq("behavior_type", 2);
        Long count = behaviorLogMapper.selectCount(wrapper);
        return Result.success(count > 0);
    }

    @DeleteMapping("/favorite")
    public Result<String> removeFavorite(@RequestParam Integer userId, @RequestParam Integer productId) {
        QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("product_id", productId)
                .eq("behavior_type", 2);
        int deleted = behaviorLogMapper.delete(wrapper);
        if (deleted > 0) {
            return Result.success("取消收藏成功", null);
        }
        return Result.error("取消收藏失败");
    }
}