package com.tony.rabbitmqcommon.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("broker_message_log")
public class BrokerMessageLog {
    private static final long serialVersionUID = -2417304522008102287L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String messageId;
    /**
     * 消息
     */
    private String message;
    /**
     * 尝试次数
     */
    private Integer tryCount;
    /**
     * 状态：0：发送中，1：发送成功，2：发送失败
     */
    private String status;
    /**
     * 下次尝试重连时间
     */
    private Date nextRetry;
    /**
     * 创建日期
     */
    private Date createTime;
    /**
     * 更新日期
     */
    private Date updateTime;
}
