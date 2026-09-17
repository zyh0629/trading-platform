package com.campus.trading.service.impl;

import com.campus.trading.entity.Product;
import java.util.List;

public interface RecommendService {
    // 为用户推荐商品（综合推荐）
    List<Product> recommendForUser(Integer userId, int topN);

    // 热门商品推荐（按浏览量排序）
    List<Product> getHotProducts(int topN);

    // 基于用户的协同过滤
    List<Product> userBasedCF(Integer userId, int topN);

    // 基于物品的协同过滤
    List<Product> itemBasedCF(Integer userId, int topN);

    // 收藏推荐 - 根据用户收藏的商品分类推荐
    List<Product> recommendByFavorite(Integer userId, int topN);

    // 买过相似推荐 - 根据用户购买记录推荐同分类商品
    List<Product> recommendByTrade(Integer userId, int topN);
}