package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.PrivateMessage;
import com.campus.trading.mapper.PrivateMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Override
    public boolean sendMessage(Integer fromUserId, Integer toUserId, String content) {
        PrivateMessage message = new PrivateMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);  // 0未读
        message.setCreateTime(LocalDateTime.now());
        return privateMessageMapper.insert(message) > 0;
    }

    @Override
    public List<PrivateMessage> getChatHistory(Integer userId, Integer otherUserId) {
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w
                .eq("from_user_id", userId).eq("to_user_id", otherUserId)
                .or()
                .eq("from_user_id", otherUserId).eq("to_user_id", userId)
        );
        wrapper.orderByAsc("create_time");
        return privateMessageMapper.selectList(wrapper);
    }

    @Override
    public List<PrivateMessage> getConversationList(Integer userId) {
        // 获取与该用户有关的所有消息
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w
                .eq("from_user_id", userId)
                .or()
                .eq("to_user_id", userId)
        );
        wrapper.orderByDesc("create_time");
        List<PrivateMessage> allMessages = privateMessageMapper.selectList(wrapper);

        // 按对话对象分组，取最后一条消息
        Set<Integer> otherUserIds = new LinkedHashSet<>();
        List<PrivateMessage> conversations = new ArrayList<>();

        for (PrivateMessage msg : allMessages) {
            int otherId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            if (!otherUserIds.contains(otherId)) {
                otherUserIds.add(otherId);
                conversations.add(msg);
            }
        }
        return conversations;
    }

    @Override
    public boolean markAsRead(Integer messageId, Integer userId) {
        PrivateMessage message = privateMessageMapper.selectById(messageId);
        if (message != null && message.getToUserId().equals(userId)) {
            message.setIsRead(1);
            return privateMessageMapper.updateById(message) > 0;
        }
        return false;
    }

    @Override
    public int getUnreadCount(Integer userId) {
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId).eq("is_read", 0);
        Long count = privateMessageMapper.selectCount(wrapper);
        return count.intValue();  // 转成int
    }
}