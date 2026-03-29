package com.wechat.ferry.controller.ws.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.wechat.ferry.controller.ws.msg.MessageType.PING;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Ping extends WzWebSocketMessage {
    public Ping(String src, String tgt) {
        super(PING, src, tgt);
    }
}
