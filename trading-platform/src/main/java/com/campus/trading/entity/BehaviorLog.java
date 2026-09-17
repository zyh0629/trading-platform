package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("behavior_log")
public class BehaviorLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer behaviorType;  // 1浏览 2收藏 3交易

    private Integer score;  // 浏览1分，收藏3分，交易5分

    private LocalDateTime createTime;
}