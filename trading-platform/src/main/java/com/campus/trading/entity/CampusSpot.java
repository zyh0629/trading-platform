package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("campus_spot")
public class CampusSpot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private Long ownerId;

    private Integer isPublic;
}
