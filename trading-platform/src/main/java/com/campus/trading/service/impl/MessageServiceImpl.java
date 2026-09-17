package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.Message;
import com.campus.trading.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public boolean addMessage(Integer productId, Integer fromUserId, Integer toUserId, String content) {
        Message message = new Message();
        message.setProductId(productId);
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        return messageMapper.insert(message) > 0;
    }

    @Override
    public List<Message> getMessagesByProductId(Integer productId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.orderByDesc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public boolean deleteMessage(Integer messageId, Integer userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("id", messageId).eq("from_user_id", userId);
        return messageMapper.delete(wrapper) > 0;
    }
}