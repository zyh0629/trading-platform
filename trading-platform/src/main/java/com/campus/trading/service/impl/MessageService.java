package com.campus.trading.service.impl;

import com.campus.trading.entity.Message;
import java.util.List;

public interface MessageService {
    // 发表留言
    boolean addMessage(Integer productId, Integer fromUserId, Integer toUserId, String content);

    // 获取商品的所有留言
    List<Message> getMessagesByProductId(Integer productId);

    // 删除留言
    boolean deleteMessage(Integer messageId, Integer userId);
}