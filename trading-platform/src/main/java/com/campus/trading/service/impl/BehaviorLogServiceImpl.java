package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.BehaviorLog;
import com.campus.trading.mapper.BehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class BehaviorLogServiceImpl implements BehaviorLogService {

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    @Override
    public boolean logBehavior(Integer userId, Integer productId, Integer behaviorType) {
        // 获取分数
        int score = getScoreByBehaviorType(behaviorType);

        BehaviorLog log = new BehaviorLog();
        log.setUserId(userId);
        log.setProductId(productId);
        log.setBehaviorType(behaviorType);
        log.setScore(score);
        log.setCreateTime(LocalDateTime.now());

        return behaviorLogMapper.insert(log) > 0;
    }

    private int getScoreByBehaviorType(Integer behaviorType) {
        switch (behaviorType) {
            case 1: return 1;  // 浏览
            case 2: return 3;  // 收藏
            case 3: return 5;  // 交易
            default: return 1;
        }
    }
}