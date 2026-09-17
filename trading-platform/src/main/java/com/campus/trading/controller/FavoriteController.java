package com.campus.trading.controller;

import com.campus.trading.entity.Favorite;
import com.campus.trading.service.impl.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // 添加收藏
    @PostMapping("/add")
    public Map<String, Object> addFavorite(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = favoriteService.addFavorite(userId, productId);
        result.put("success", success);
        result.put("message", success ? "收藏成功" : "已经收藏过了");
        return result;
    }

    // 取消收藏
    @DeleteMapping("/remove")
    public Map<String, Object> removeFavorite(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = favoriteService.removeFavorite(userId, productId);
        result.put("success", success);
        result.put("message", success ? "取消收藏成功" : "取消收藏失败");
        return result;
    }

    // 获取我的收藏列表
    @GetMapping("/my/{userId}")
    public List<Favorite> getUserFavorites(@PathVariable Integer userId) {
        return favoriteService.getUserFavorites(userId);
    }

    // 检查是否已收藏
    @GetMapping("/check")
    public Map<String, Object> isFavorited(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean favorited = favoriteService.isFavorited(userId, productId);
        result.put("favorited", favorited);
        return result;
    }
}