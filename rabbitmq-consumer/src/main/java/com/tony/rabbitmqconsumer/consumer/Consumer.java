package com.tony.rabbitmqconsumer.consumer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.tony.rabbitmqcommon.domain.BrokerMessageLog;
import com.tony.rabbitmqcommon.domain.Order;
import com.tony.rabbitmqconsumer.mapper.BrokerMessageLogMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
@Log4j2
public class Consumer {
    @Resource
    private BrokerMessageLogMapper brokerMessageLogMapper;

    @RabbitListener(bindings =
            @QueueBinding(value = @Queue(value = "order-queue", durable = "true"),
                    exchange = @Exchange(name = "order-exchange", durable = "true", type = "topic"),
                    key = {"order.register.#"})
    )
    @RabbitHandler
    public void receiveOrderMessage(@Payload Order order, @Headers Map<String, Object> headers, Channel channel) throws IOException {
        log.info("===========================收到消息，开始消费===========================");
        log.info("订单id:{}----", order.getMessageId());

        //查询订单是否在日志表，不在或者是失败状态不做操作
        QueryWrapper<BrokerMessageLog> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(BrokerMessageLog::getMessageId, order.getMessageId());
        BrokerMessageLog brokerMessageLog = brokerMessageLogMapper.selectOne(wrapper);

        if (ObjectUtil.isEmpty(brokerMessageLog) || !StrUtil.equals(brokerMessageLog.getStatus(), "1")) {
            log.info("=============消息已消费，请勿重复消费===================");
            return;
        }


        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        /**
         *  取值为 false 时，表示通知 RabbitMQ 当前消息被确认
         *  如果为 true，则额外将比第一个参数指定的 delivery tag 小的消息一并确认
         */
        channel.basicAck(deliveryTag, false);
        log.info("===========================收到消息，开始完成===========================");
    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue, exchange = @Exchange(name = "logs", type = "topic"),
                    key = {"logs.error", "logs.info"})
    })
    public void receive2(String message) {
        log.info("message2----" + message);
    }


}
