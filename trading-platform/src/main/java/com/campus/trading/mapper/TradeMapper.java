package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.Trade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeMapper extends BaseMapper<Trade> {
}