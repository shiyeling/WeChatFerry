package com.wechat.ferry.controller.ws.msg;

/**
 * * 支持的操作类型：
 * * - 切换轮播开关：开/关
 * * - 切换播放速度： 快中慢
 * * - 切换当前页： 用于在关闭轮播后，手动给切换页面
 */
public enum CommandType {
    RS, // 要求上报状态：服务器发给客户端时，客户端应立即上报播放器状态
        // 客户端发给服务器时，客户端把需要的sessionId，放到tgt里，服务器会下发对应播放器的状态（如果有）
    LS, // 枚举 当前账户能管理的播放终端的WebSocket会话列表
    PLAY, // 播放，需要指定sesionId
    PAUSE, // 暂停，需要指定sessionId
    SET_SPEED, // 设置播放速度，需要指定SessionId
    SWITCH_PAGE; // 切换页面，需要指定SessionId
}
