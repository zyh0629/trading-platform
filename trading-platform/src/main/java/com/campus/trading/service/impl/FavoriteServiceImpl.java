package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.Favorite;
import com.campus.trading.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public boolean addFavorite(Integer userId, Integer productId) {
        // 检查是否已经收藏
        if (isFavorited(userId, productId)) {
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        return favoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFavorite(Integer userId, Integer productId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        return favoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public List<Favorite> getUserFavorites(Integer userId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return favoriteMapper.selectList(wrapper);
    }

    @Override
    public boolean isFavorited(Integer userId, Integer productId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}