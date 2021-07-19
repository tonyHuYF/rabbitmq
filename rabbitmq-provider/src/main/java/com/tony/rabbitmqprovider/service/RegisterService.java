package com.tony.rabbitmqprovider.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.tony.rabbitmqcommon.domain.BrokerMessageLog;
import com.tony.rabbitmqcommon.domain.Order;
import com.tony.rabbitmqprovider.contants.Contants;
import com.tony.rabbitmqprovider.mapper.BrokerMessageLogMapper;
import com.tony.rabbitmqprovider.mapper.OrderMapper;
import com.tony.rabbitmqprovider.provider.Provider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class RegisterService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private BrokerMessageLogMapper brokerMessageLogMapper;

    @Resource
    private Provider provider;


    @Transactional
    public void register(String name) {
        Order order = new Order();
        order.setName(name);
        //用uuid+时间戳确保唯一
        String messageId = IdUtil.randomUUID() + System.currentTimeMillis();
        order.setMessageId(messageId);

        //插入订单表
        orderMapper.insert(order);

        //插入日志表
        BrokerMessageLog brokerMessageLog = new BrokerMessageLog();
        brokerMessageLog.setMessageId(messageId);
        brokerMessageLog.setMessage(JSONUtil.toJsonStr(order));
        brokerMessageLog.setStatus(Contants.ORDER_SENDING);
        //下次投递时间 (延迟1分钟)
        brokerMessageLog.setNextRetry(new Date(new Date().getTime() + 60000));

        brokerMessageLogMapper.insert(brokerMessageLog);


        //发送消息到mq
        provider.send(order);

    }
}
