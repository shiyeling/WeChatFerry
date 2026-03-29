package com.wechat.ferry.controller.ws.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.wechat.ferry.controller.ws.msg.MessageType.PONG;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Pong extends WzWebSocketMessage {
    public Pong(String src, String tgt) {
        super(PONG, src, tgt);
    }
}
