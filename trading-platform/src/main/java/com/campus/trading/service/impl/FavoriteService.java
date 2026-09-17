package com.campus.trading.service.impl;

import com.campus.trading.entity.Favorite;
import java.util.List;

public interface FavoriteService {
    boolean addFavorite(Integer userId, Integer productId);
    boolean removeFavorite(Integer userId, Integer productId);
    List<Favorite> getUserFavorites(Integer userId);
    boolean isFavorited(Integer userId, Integer productId);
}