package com.wechat.ferry.controller.ws.msg;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WzWebSocketDataMsg<T> extends WzWebSocketMessage{
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    T data;

    public WzWebSocketDataMsg(MessageType type, String src, String tgt, T data) {
        super(type, src, tgt);
        this.data = data;
    }
}
