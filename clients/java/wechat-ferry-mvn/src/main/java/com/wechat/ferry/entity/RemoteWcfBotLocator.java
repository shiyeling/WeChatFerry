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
    private int helperPort;

    public RemoteWcfBotLocator(String host, int wcfCmdPort) {
        this.host = host;
        this.wcfCmdPort = wcfCmdPort;
    }

    public String getUri() {
        return String.format("wcf:%s:%d", host, wcfCmdPort);
    }

    public String getFileUploadUrlBase() {
        return String.format("http://%s:%d", host, helperPort);
    }

    public String getUploadPath() {
        return "/upload";
    }

    public String getUploadUrl() {
        return String.format("http://%s:%d/upload", host, helperPort);
    }
    public String getQrCodeImageDownloadUrl() {
        return String.format("http://%s:%d/qrcode", host, helperPort);
    }
}
