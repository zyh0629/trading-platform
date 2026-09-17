package com.campus.trading.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.*;
import com.campus.trading.mapper.*;
import com.campus.trading.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    // 创建交易（买家发起购买）
    @PostMapping("/create")
    public Result<String> createTrade(@RequestParam Integer productId,
                                      @RequestParam Integer buyerId,
                                      @RequestParam String message) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStatus() != 0) {
            return Result.error("商品已售出");
        }
        if (product.getUserId().equals(buyerId)) {
            return Result.error("不能购买自己的商品");
        }

        Trade trade = new Trade();
        trade.setProductId(productId);
        trade.setBuyerId(buyerId);
        trade.setSellerId(product.getUserId());
        trade.setPrice(product.getPrice());
        trade.setStatus(0);
        trade.setMessage(message);
        trade.setCreateTime(LocalDateTime.now());
        trade.setUpdateTime(LocalDateTime.now());
        tradeMapper.insert(trade);

        PrivateMessage pm = new PrivateMessage();
        pm.setFromUserId(buyerId);
        pm.setToUserId(product.getUserId());
        pm.setContent("【购买请求】用户想购买您的商品《" + product.getTitle() + "》，价格：¥" + product.getPrice() + "。留言：" + message);
        pm.setIsRead(0);
        pm.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(pm);

        return Result.success("购买请求已发送，等待卖家确认", null);
    }

    // 卖家确认交易
    @PutMapping("/confirm")
    public Result<String> confirmTrade(@RequestParam Integer tradeId, @RequestParam Integer sellerId) {
        Trade trade = tradeMapper.selectById(tradeId);
        if (trade == null) {
            return Result.error("交易不存在");
        }
        if (!trade.getSellerId().equals(sellerId)) {
            return Result.error("无权限操作");
        }
        if (trade.getStatus() != 0) {
            return Result.error("交易已处理");
        }

        trade.setStatus(1);
        trade.setUpdateTime(LocalDateTime.now());
        tradeMapper.updateById(trade);

        Product product = new Product();
        product.setId(trade.getProductId());
        product.setStatus(1);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        // 记录交易行为
        BehaviorLog log = new BehaviorLog();
        log.setUserId(trade.getBuyerId());
        log.setProductId(trade.getProductId());
        log.setBehaviorType(3);
        log.setScore(5);
        log.setCreateTime(LocalDateTime.now());
        behaviorLogMapper.insert(log);

        PrivateMessage pm = new PrivateMessage();
        pm.setFromUserId(sellerId);
        pm.setToUserId(trade.getBuyerId());
        pm.setContent("卖家已确认您的购买请求！商品《" + productMapper.selectById(trade.getProductId()).getTitle() + "》已售出，请私信联系交易细节。");
        pm.setIsRead(0);
        pm.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(pm);

        return Result.success("交易确认成功", null);
    }

    @PutMapping("/cancel")
    public Result<String> cancelTrade(@RequestParam Integer tradeId, @RequestParam Integer userId) {
        Trade trade = tradeMapper.selectById(tradeId);
        if (trade == null) {
            return Result.error("交易不存在");
        }
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId)) {
            return Result.error("无权限操作");
        }
        if (trade.getStatus() != 0) {
            return Result.error("交易已处理，无法取消");
        }

        trade.setStatus(2);
        trade.setUpdateTime(LocalDateTime.now());
        tradeMapper.updateById(trade);

        return Result.success("交易已取消", null);
    }

    @GetMapping("/buyer/{userId}")
    public Result<List<Trade>> getBuyerTrades(@PathVariable Integer userId) {
        QueryWrapper<Trade> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", userId);
        wrapper.orderByDesc("create_time");
        return Result.success(tradeMapper.selectList(wrapper));
    }

    @GetMapping("/seller/{userId}")
    public Result<List<Trade>> getSellerTrades(@PathVariable Integer userId) {
        QueryWrapper<Trade> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", userId);
        wrapper.orderByDesc("create_time");
        return Result.success(tradeMapper.selectList(wrapper));
    }

    // 获取所有交易（管理员用）- 必须放在 /{id} 前面
    @GetMapping("/all")
    public Result<List<Trade>> getAllTrades() {
        QueryWrapper<Trade> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return Result.success(tradeMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Trade> getTradeById(@PathVariable Integer id) {
        Trade trade = tradeMapper.selectById(id);
        if (trade == null) {
            return Result.error("交易不存在");
        }
        return Result.success(trade);
    }
}