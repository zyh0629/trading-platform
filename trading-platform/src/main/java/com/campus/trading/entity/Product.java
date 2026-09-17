package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String title;

    private String description;

    private BigDecimal price;

    private Integer categoryId;

    private String images;

    private Integer status;  // 0在售 1已售 2下架

    private Integer views;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}