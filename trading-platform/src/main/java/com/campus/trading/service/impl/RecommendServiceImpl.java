package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.BehaviorLog;
import com.campus.trading.entity.Product;
import com.campus.trading.mapper.BehaviorLogMapper;
import com.campus.trading.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> recommendForUser(Integer userId, int topN) {
        if (hasNoBehavior(userId)) {
            return new ArrayList<>();
        }
        List<Product> userCF = userBasedCF(userId, topN);
        List<Product> itemCF = itemBasedCF(userId, topN);
        Set<Integer> productIds = new LinkedHashSet<>();
        for (Product p : userCF) productIds.add(p.getId());
        for (Product p : itemCF) productIds.add(p.getId());
        return productIds.stream()
                .limit(topN)
                .map(id -> productMapper.selectById(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getHotProducts(int topN) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByDesc("views");
        wrapper.last("limit " + topN);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> recommendByFavorite(Integer userId, int topN) {
        QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("behavior_type", 2);
        List<BehaviorLog> favoriteLogs = behaviorLogMapper.selectList(wrapper);
        if (favoriteLogs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> categoryIds = new HashSet<>();
        Set<Integer> userProductIds = new HashSet<>();
        for (BehaviorLog log : favoriteLogs) {
            userProductIds.add(log.getProductId());
            Product p = productMapper.selectById(log.getProductId());
            if (p != null) {
                categoryIds.add(p.getCategoryId());
            }
        }
        List<Product> results = new ArrayList<>();
        int perCategory = Math.max(1, topN / categoryIds.size());
        for (Integer categoryId : categoryIds) {
            QueryWrapper<Product> query = new QueryWrapper<>();
            query.eq("category_id", categoryId);
            query.eq("status", 0);
            query.notIn("id", userProductIds);
            query.orderByDesc("views");
            query.last("limit " + perCategory);
            results.addAll(productMapper.selectList(query));
        }
        if (results.size() < topN) {
            QueryWrapper<Product> query = new QueryWrapper<>();
            query.in("category_id", categoryIds);
            query.eq("status", 0);
            query.notIn("id", userProductIds);
            query.orderByDesc("views");
            query.last("limit " + (topN - results.size()));
            results.addAll(productMapper.selectList(query));
        }
        return results.stream().limit(topN).collect(Collectors.toList());
    }

    @Override
    public List<Product> recommendByTrade(Integer userId, int topN) {
        QueryWrapper<BehaviorLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("behavior_type", 3);
        List<BehaviorLog> tradeLogs = behaviorLogMapper.selectList(wrapper);
        if (tradeLogs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> categoryIds = new HashSet<>();
        Set<Integer> userProductIds = new HashSet<>();
        for (BehaviorLog log : tradeLogs) {
            userProductIds.add(log.getProductId());
            Product p = productMapper.selectById(log.getProductId());
            if (p != null) {
                categoryIds.add(p.getCategoryId());
            }
        }
        List<Product> results = new ArrayList<>();
        int perCategory = Math.max(1, topN / categoryIds.size());
        for (Integer categoryId : categoryIds) {
            QueryWrapper<Product> query = new QueryWrapper<>();
            query.eq("category_id", categoryId);
            query.eq("status", 0);
            query.notIn("id", userProductIds);
            query.orderByDesc("views");
            query.last("limit " + perCategory);
            results.addAll(productMapper.selectList(query));
        }
        if (results.size() < topN) {
            QueryWrapper<Product> query = new QueryWrapper<>();
            query.in("category_id", categoryIds);
            query.eq("status", 0);
            query.notIn("id", userProductIds);
            query.orderByDesc("views");
            query.last("limit " + (topN - results.size()));
            results.addAll(productMapper.selectList(query));
        }
        return results.stream().limit(topN).collect(Collectors.toList());
    }

    @Override
    public List<Product> userBasedCF(Integer userId, int topN) {
        List<BehaviorLog> userLogs = behaviorLogMapper.selectList(
                new QueryWrapper<BehaviorLog>().eq("user_id", userId)
        );
        if (userLogs.isEmpty()) return new ArrayList<>();
        Set<Integer> userProductIds = userLogs.stream()
                .map(BehaviorLog::getProductId)
                .collect(Collectors.toSet());
        Set<Integer> categoryIds = new HashSet<>();
        for (Integer pid : userProductIds) {
            Product p = productMapper.selectById(pid);
            if (p != null) categoryIds.add(p.getCategoryId());
        }
        if (categoryIds.isEmpty()) return new ArrayList<>();
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.in("category_id", categoryIds);
        query.eq("status", 0);
        query.notIn("id", userProductIds);
        query.orderByDesc("views");
        query.last("limit " + topN);
        return productMapper.selectList(query);
    }

    @Override
    public List<Product> itemBasedCF(Integer userId, int topN) {
        List<BehaviorLog> userLogs = behaviorLogMapper.selectList(
                new QueryWrapper<BehaviorLog>().eq("user_id", userId)
        );
        if (userLogs.isEmpty()) return new ArrayList<>();
        Set<Integer> userProductIds = userLogs.stream()
                .map(BehaviorLog::getProductId)
                .collect(Collectors.toSet());
        Set<Integer> categoryIds = new HashSet<>();
        for (Integer pid : userProductIds) {
            Product p = productMapper.selectById(pid);
            if (p != null) categoryIds.add(p.getCategoryId());
        }
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.in("category_id", categoryIds);
        query.eq("status", 0);
        query.notIn("id", userProductIds);
        query.orderByDesc("views");
        query.last("limit " + topN);
        return productMapper.selectList(query);
    }

    private boolean hasNoBehavior(Integer userId) {
        return behaviorLogMapper.selectCount(
                new QueryWrapper<BehaviorLog>().eq("user_id", userId)
        ) == 0;
    }
}