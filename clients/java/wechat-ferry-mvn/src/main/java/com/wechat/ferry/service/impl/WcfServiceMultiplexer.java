package com.wechat.ferry.service.impl;

import com.wechat.ferry.config.WeChatFerryProperties;
import com.wechat.ferry.entity.RemoteWcfBotLocator;
import com.wechat.ferry.service.WeChatDllService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务实现层-对接原本DLL的接口
 *
 * @author chandler
 * @date 2024-10-01 15:58
 */
@Slf4j
@Service
public class WcfServiceMultiplexer {

    @Resource
    private WeChatFerryProperties weChatFerryProperties;

    private final Map<String, WeChatDllService> wcfRobots = new HashMap<>();

    public WeChatDllService getService(RemoteWcfBotLocator locator) {
        String botUri = locator.getUri();
        log.info("Accessing bot : {}", botUri);
        if (!wcfRobots.containsKey(botUri)) {
            try {
                WeChatDllServiceImpl weChatDllService = new WeChatDllServiceImpl(locator.getHost(), locator.getWcfCmdPort());
                weChatDllService.setWeChatFerryProperties(weChatFerryProperties);
                wcfRobots.put(botUri, weChatDllService);
            } catch (Exception e) {
                log.error("无法创建到机器人{}:{}的链接，{}", locator.getHost(), locator.getWcfCmdPort(), e.getMessage(), e);
                throw e;
            }
        }
        return wcfRobots.get(locator.getUri());
    }
}
