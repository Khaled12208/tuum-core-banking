package com.tuum.common.adapter;

import com.tuum.common.dto.mq.MQMessageData;

public interface MessageAdapter {
    MQMessageData adapt(Object message) throws Exception;
}