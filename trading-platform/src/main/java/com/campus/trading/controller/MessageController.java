package com.campus.trading.controller;

import com.campus.trading.entity.Message;
import com.campus.trading.service.impl.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 发表留言
    @PostMapping("/add")
    public Map<String, Object> addMessage(
            @RequestParam Integer productId,
            @RequestParam Integer fromUserId,
            @RequestParam Integer toUserId,
            @RequestParam String content) {
        Map<String, Object> result = new HashMap<>();
        boolean success = messageService.addMessage(productId, fromUserId, toUserId, content);
        result.put("success", success);
        result.put("message", success ? "留言成功" : "留言失败");
        return result;
    }

    // 获取商品留言列表
    @GetMapping("/list/{productId}")
    public List<Message> getMessagesByProductId(@PathVariable Integer productId) {
        return messageService.getMessagesByProductId(productId);
    }

    // 删除留言
    @DeleteMapping("/delete")
    public Map<String, Object> deleteMessage(@RequestParam Integer messageId, @RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = messageService.deleteMessage(messageId, userId);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }
}