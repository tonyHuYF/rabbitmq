package com.tony.rabbitmqconsumer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tony.rabbitmqcommon.domain.BrokerMessageLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BrokerMessageLogMapper extends BaseMapper<BrokerMessageLog> {
}
