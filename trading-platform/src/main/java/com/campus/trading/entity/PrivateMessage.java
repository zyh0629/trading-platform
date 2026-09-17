package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("private_message")
public class PrivateMessage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer fromUserId;

    private Integer toUserId;

    private String content;

    private Integer isRead;  // 0未读 1已读

    private LocalDateTime createTime;
}