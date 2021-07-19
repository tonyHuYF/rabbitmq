package com.tony.rabbitmqprovider.provider;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tony.rabbitmqcommon.domain.BrokerMessageLog;
import com.tony.rabbitmqcommon.domain.Order;
import com.tony.rabbitmqprovider.contants.Contants;
import com.tony.rabbitmqprovider.mapper.BrokerMessageLogMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Log4j2
public class Provider {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private BrokerMessageLogMapper brokerMessageLogMapper;


    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        /**
         *
         * @param correlationData 唯一标识，有了这个唯一标识，我们就知道可以确认（失败）哪一条消息了
         * @param ack  是否投递成功
         * @param cause 失败原因
         */
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {

            String messageId = correlationData.getId();
            QueryWrapper<BrokerMessageLog> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(BrokerMessageLog::getMessageId, messageId);
            BrokerMessageLog brokerMessageLog = brokerMessageLogMapper.selectOne(wrapper);

            if (ObjectUtil.isEmpty(brokerMessageLog)) {
                log.error("messageId:{}，不存在消息日志记录");
                throw new NullPointerException();
            }

            if (ack) {
                //成功
                brokerMessageLog.setStatus(Contants.ORDER_SEND_SUCCESS);
                brokerMessageLog.setUpdateTime(new Date());
                brokerMessageLogMapper.updateById(brokerMessageLog);

                log.info("消息投递成功，messageId:{}", messageId);

            } else {
                //失败
                log.error("消息消费失败，messageId:{}，原因:{}", messageId, cause);
            }
        }
    };


    /**
     * 消息投递
     */
    public void send(Order order) {
        rabbitTemplate.setConfirmCallback(confirmCallback);
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(order.getMessageId());
        rabbitTemplate.convertAndSend("order-exchange", "order.register", order, correlationData);
    }

}
