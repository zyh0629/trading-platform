package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade")
public class Trade {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productId;
    private Integer buyerId;
    private Integer sellerId;
    private BigDecimal price;
    private Integer status;  // 0待确认 1已确认 2已取消 3已完成
    private String message;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}