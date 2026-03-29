package com.wechat.ferry.controller.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final AuthHandshakeInterceptor authInterceptor;
    private final WCFServerWebSocketHandler handler;

    @Autowired
    private StompAuthHandshakeHandler authHandshakeHandler;

    @Value("${cors.allowed.origins:https://*.weizhukeji.net,http://10.33.10.185:3000,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Allowed origins: {}", allowedOrigins);
        registry.addEndpoint("/api/v1/websocket")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
                .setHandshakeHandler(authHandshakeHandler)
                .withSockJS();
    }

    public WebSocketConfig(AuthHandshakeInterceptor authInterceptor, WCFServerWebSocketHandler handler) {
        this.authInterceptor = authInterceptor;
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/wechat-ferry/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*"); // 生产环境可改为指定域名
    }
}
