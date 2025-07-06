package com.tuum.common.dto.mq;

import com.tuum.common.types.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MQMessageData {

    private  RequestType requestType;
    private  String idempotencyKey;
    private  String requestId;
    private  String status;
    private  String messageBody;


}
