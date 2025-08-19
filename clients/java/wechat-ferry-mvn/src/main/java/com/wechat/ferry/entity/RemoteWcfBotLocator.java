package com.wechat.ferry.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteWcfBotLocator {
    private String host;
    private int wcfCmdPort;

    public String getUri() {
        return String.format("wcf:%s:%d", host, wcfCmdPort);
    }
}
