package com.wechat.ferry.controller.dto;

import com.wechat.ferry.entity.RemoteWcfBotLocator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("远程WCF机器人定位描述用的数据对象")
@NoArgsConstructor
@AllArgsConstructor
public class RemoteWcfBotLocatorInfo {
    @ApiModelProperty("机器人所在主机")
    private String host;
    @ApiModelProperty("机器人的命令端口")
    private int wcfCmdPort;

    public RemoteWcfBotLocatorInfo(RemoteWcfBotLocator locator) {
        this.host = locator.getHost();
        this.wcfCmdPort = locator.getWcfCmdPort();
    }

    public RemoteWcfBotLocator getBotLocator() {
        return new RemoteWcfBotLocator(this.host, this.wcfCmdPort);
    }

    public String getUri() {
        return String.format("wcf:%s:%d", host, wcfCmdPort);
    }
}
