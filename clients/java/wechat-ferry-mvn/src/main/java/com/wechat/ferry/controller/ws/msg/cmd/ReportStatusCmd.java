package com.wechat.ferry.controller.ws.msg.cmd;

import com.wechat.ferry.controller.ws.msg.CommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ReportStatusCmd extends WzCmdMessage<Void> {
    public ReportStatusCmd(String src, String tgt) {
        super(CommandType.RS, null, src, tgt);
    }
}
