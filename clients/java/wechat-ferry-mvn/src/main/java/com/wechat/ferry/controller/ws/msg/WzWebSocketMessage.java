package com.wechat.ferry.controller.ws.msg;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public abstract class WzWebSocketMessage {
    MessageType type; // 消息类型
    String src;
    String tgt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime time;

    public WzWebSocketMessage(MessageType type, String src, String tgt) {
        this.type = type;
        this.src = src;
        this.tgt = tgt;
    }
}
