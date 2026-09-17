package com.campus.trading.service.impl;

import com.campus.trading.entity.PrivateMessage;
import java.util.List;

public interface PrivateMessageService {
    // 发送私信
    boolean sendMessage(Integer fromUserId, Integer toUserId, String content);

    // 获取与某个用户的聊天记录
    List<PrivateMessage> getChatHistory(Integer userId, Integer otherUserId);

    // 获取用户的对话列表（最后一条消息）
    List<PrivateMessage> getConversationList(Integer userId);

    // 标记消息为已读
    boolean markAsRead(Integer messageId, Integer userId);

    // 获取未读消息数量
    int getUnreadCount(Integer userId);
}