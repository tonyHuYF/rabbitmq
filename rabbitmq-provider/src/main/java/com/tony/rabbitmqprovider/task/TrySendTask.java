package com.tony.rabbitmqprovider.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tony.rabbitmqcommon.domain.BrokerMessageLog;
import com.tony.rabbitmqcommon.domain.Order;
import com.tony.rabbitmqprovider.contants.Contants;
import com.tony.rabbitmqprovider.mapper.BrokerMessageLogMapper;
import com.tony.rabbitmqprovider.provider.Provider;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Log4j2
public class TrySendTask {
    @Resource
    private BrokerMessageLogMapper brokerMessageLogMapper;
    @Resource
    private Provider provider;

    /**
     * 系统启动后5秒开启定时任务 10秒执行一次
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void taskTime() {
        this.trySend();

    }

    public void trySend() {
        QueryWrapper<BrokerMessageLog> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(BrokerMessageLog::getStatus, Contants.ORDER_SENDING);
        wrapper.lambda().le(BrokerMessageLog::getNextRetry, new Date());

        List<BrokerMessageLog> logs = brokerMessageLogMapper.selectList(wrapper);

        for (BrokerMessageLog data : logs) {
            if (data.getTryCount() >= 3) {
                data.setStatus(Contants.ORDER_SEND_FAILURE);
                data.setUpdateTime(new Date());
                brokerMessageLogMapper.updateById(data);
            } else {
                data.setTryCount(data.getTryCount()+1);
                data.setUpdateTime(new Date());
                brokerMessageLogMapper.updateById(data);

                //重新投递
                provider.send(JSONUtil.toBean(data.getMessage(), Order.class));
            }
        }


    }
}
