package com.campus.trading.controller;

import com.campus.trading.entity.Product;
import com.campus.trading.service.impl.BehaviorLogService;
import com.campus.trading.service.impl.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private BehaviorLogService behaviorLogService;

    @GetMapping("/list/{userId}")
    public List<Product> getRecommendList(@PathVariable Integer userId, @RequestParam(defaultValue = "10") int topN) {
        return recommendService.recommendForUser(userId, topN);
    }

    @GetMapping("/hot/{topN}")
    public List<Product> getHotProducts(@PathVariable int topN) {
        return recommendService.getHotProducts(topN);
    }

    @GetMapping("/favorite/{userId}")
    public List<Product> recommendByFavorite(@PathVariable Integer userId, @RequestParam(defaultValue = "10") int topN) {
        return recommendService.recommendByFavorite(userId, topN);
    }

    @GetMapping("/trade/{userId}")
    public List<Product> recommendByTrade(@PathVariable Integer userId, @RequestParam(defaultValue = "10") int topN) {
        return recommendService.recommendByTrade(userId, topN);
    }

    @PostMapping("/view")
    public Map<String, Object> recordView(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = behaviorLogService.logBehavior(userId, productId, 1);
        result.put("success", success);
        result.put("message", success ? "记录成功" : "记录失败");
        return result;
    }

    @PostMapping("/favorite")
    public Map<String, Object> recordFavorite(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = behaviorLogService.logBehavior(userId, productId, 2);
        result.put("success", success);
        return result;
    }

    @PostMapping("/trade")
    public Map<String, Object> recordTrade(@RequestParam Integer userId, @RequestParam Integer productId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = behaviorLogService.logBehavior(userId, productId, 3);
        result.put("success", success);
        return result;
    }
}