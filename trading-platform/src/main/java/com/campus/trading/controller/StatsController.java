package com.campus.trading.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.*;
import com.campus.trading.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    /**
     * 用户增长统计（按日期）
     * @param days 最近天数，默认30天
     */
    @GetMapping("/users")
    public Map<String, Object> getUserStats(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM/dd"));
            dates.add(dateStr);

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.between("create_time", start, end);
            Long count = userMapper.selectCount(wrapper);
            counts.add(count.intValue());
        }

        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    /**
     * 商品分类统计
     */
    @GetMapping("/categories")
    public List<Map<String, Object>> getCategoryStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Integer, String> categoryNames = new HashMap<>();
        categoryNames.put(1, "教材");
        categoryNames.put(2, "电子产品");
        categoryNames.put(3, "生活用品");
        categoryNames.put(4, "其他");

        for (int i = 1; i <= 4; i++) {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("category_id", i);
            Long count = productMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("name", categoryNames.get(i));
            item.put("value", count.intValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 交易统计（按日期）
     * 交易记录通过 behavior_log 表记录，behavior_type=3 表示交易
     */
    @GetMapping("/trades")
    public Map<String, Object> getTradeStats(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM/dd"));
            dates.add(dateStr);

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
            wrapper.eq("behavior_type", 3);
            wrapper.between("create_time", start, end);
            Long count = behaviorLogMapper.selectCount(wrapper);
            counts.add(count.intValue());
        }

        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    /**
     * 推荐统计 - 真实计算
     */
    @GetMapping("/recommend")
    public Map<String, Object> getRecommendStats() {
        Map<String, Object> result = new HashMap<>();

        // 统计总浏览量
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.select("IFNULL(SUM(views), 0) as total_views");
        Map<String, Object> viewsResult = productMapper.selectMaps(productWrapper).get(0);
        Long totalViews = 0L;
        if (viewsResult.get("total_views") != null) {
            totalViews = Long.valueOf(viewsResult.get("total_views").toString());
        }

        // 统计总交易量
        QueryWrapper<BehaviorLog> tradeWrapper = new QueryWrapper<>();
        tradeWrapper.eq("behavior_type", 3);
        Long totalTrades = behaviorLogMapper.selectCount(tradeWrapper);

        // 推荐点击率 = 总交易量 / 总浏览量 * 100
        double clickRate = 0;
        if (totalViews > 0) {
            clickRate = Math.round((totalTrades.doubleValue() / totalViews) * 1000) / 10.0;
        }

        result.put("clickRate", clickRate);
        result.put("totalViews", totalViews);
        result.put("totalTrades", totalTrades);

        return result;
    }

    /**
     * 总览统计数据
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> result = new HashMap<>();

        // 用户总数
        Long totalUsers = userMapper.selectCount(null);

        // 商品总数
        Long totalProducts = productMapper.selectCount(null);

        // 在售商品数
        QueryWrapper<Product> sellingWrapper = new QueryWrapper<>();
        sellingWrapper.eq("status", 0);
        Long sellingCount = productMapper.selectCount(sellingWrapper);

        // 总浏览量
        QueryWrapper<Product> viewsWrapper = new QueryWrapper<>();
        viewsWrapper.select("IFNULL(SUM(views), 0) as total_views");
        Map<String, Object> viewsResult = productMapper.selectMaps(viewsWrapper).get(0);
        Long totalViews = 0L;
        if (viewsResult.get("total_views") != null) {
            totalViews = Long.valueOf(viewsResult.get("total_views").toString());

        }

        // 总交易量
        QueryWrapper<BehaviorLog> tradeWrapper = new QueryWrapper<>();
        tradeWrapper.eq("behavior_type", 3);
        Long totalTrades = behaviorLogMapper.selectCount(tradeWrapper);

        // 未读消息总数
        QueryWrapper<PrivateMessage> unreadWrapper = new QueryWrapper<>();
        unreadWrapper.eq("is_read", 0);
        Long unreadCount = privateMessageMapper.selectCount(unreadWrapper);

        // 推荐点击率（真实计算）
        double recommendRate = 0;
        if (totalViews > 0) {
            recommendRate = Math.round((totalTrades.doubleValue() / totalViews) * 1000) / 10.0;
        }

        result.put("totalUsers", totalUsers.intValue());
        result.put("totalProducts", totalProducts.intValue());
        result.put("sellingCount", sellingCount.intValue());
        result.put("totalViews", totalViews);
        result.put("totalTrades", totalTrades.intValue());
        result.put("unreadCount", unreadCount.intValue());
        result.put("recommendRate", recommendRate);

        return result;
    }
}