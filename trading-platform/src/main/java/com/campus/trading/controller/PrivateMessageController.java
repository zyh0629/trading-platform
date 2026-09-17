package com.campus.trading.controller;

import com.campus.trading.entity.PrivateMessage;
import com.campus.trading.entity.User;
import com.campus.trading.mapper.UserMapper;
import com.campus.trading.service.impl.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService messageService;

    @Autowired
    private UserMapper userMapper;

    // 发送私信
    @PostMapping("/send")
    public Map<String, Object> sendMessage(
            @RequestParam Integer fromUserId,
            @RequestParam Integer toUserId,
            @RequestParam String content) {
        Map<String, Object> result = new HashMap<>();
        boolean success = messageService.sendMessage(fromUserId, toUserId, content);
        result.put("success", success);
        result.put("message", success ? "发送成功" : "发送失败");
        return result;
    }

    // 获取聊天记录
    @GetMapping("/chat/{userId}/{otherUserId}")
    public List<PrivateMessage> getChatHistory(
            @PathVariable Integer userId,
            @PathVariable Integer otherUserId) {
        return messageService.getChatHistory(userId, otherUserId);
    }

    // 获取对话列表
    @GetMapping("/conversations/{userId}")
    public List<PrivateMessage> getConversationList(@PathVariable Integer userId) {
        return messageService.getConversationList(userId);
    }

    // 获取未读消息数量
    @GetMapping("/unread/{userId}")
    public Map<String, Object> getUnreadCount(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int count = messageService.getUnreadCount(userId);
        result.put("count", count);
        return result;
    }

    // 标记消息已读
    @PutMapping("/read/{messageId}")
    public Map<String, Object> markAsRead(
            @PathVariable Integer messageId,
            @RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = messageService.markAsRead(messageId, userId);
        result.put("success", success);
        return result;
    }
}