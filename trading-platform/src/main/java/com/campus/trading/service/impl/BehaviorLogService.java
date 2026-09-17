package com.campus.trading.service.impl;

import com.campus.trading.entity.BehaviorLog;

public interface BehaviorLogService {
    // 记录行为（behaviorType: 1浏览 2收藏 3交易）
    boolean logBehavior(Integer userId, Integer productId, Integer behaviorType);

    // 获取用户评分矩阵（用于推荐算法）
    // 浏览=1分，收藏=3分，交易=5分
}