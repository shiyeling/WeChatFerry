package com.wechat.ferry.controller.ws.msg;

import com.wechat.ferry.controller.ws.dto.RobotStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class StatusReport extends WzWebSocketDataMsg<RobotStatus> {
    public StatusReport(RobotStatus robotStatus, String src, String tgt) {
        super(MessageType.SR, src, tgt, robotStatus);
    }
}
