package com.wechat.ferry.controller.ws.dto;

import lombok.Data;

@Data
public class RobotStatus {
    boolean wxStarted;
    String wxStartError;
    boolean loggedIn;
    String weChatUid;
    String weChatNickname;

    /**
     * 手机号
     */
    String phone;

    /**
     * 文件/图片等父路径
     */
    String homePath;
}
