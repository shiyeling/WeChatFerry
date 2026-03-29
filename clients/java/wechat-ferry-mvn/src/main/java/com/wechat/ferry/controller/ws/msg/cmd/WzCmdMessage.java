package com.wechat.ferry.controller.ws.msg.cmd;

import com.wechat.ferry.controller.ws.msg.CommandType;
import com.wechat.ferry.controller.ws.msg.MessageType;
import com.wechat.ferry.controller.ws.msg.WzWebSocketDataMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WzCmdMessage<T> extends WzWebSocketDataMsg<T> {
    CommandType cmd;

    public WzCmdMessage(CommandType cmd, T data, String src, String tgt) {
        super(MessageType.CMD, src, tgt, data);
        this.cmd = cmd;
    }
}
