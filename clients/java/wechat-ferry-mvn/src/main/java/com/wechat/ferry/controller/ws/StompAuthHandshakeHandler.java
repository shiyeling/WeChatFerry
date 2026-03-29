package com.wechat.ferry.controller.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;

import java.util.Map;

@Component
public class StompAuthHandshakeHandler implements HandshakeHandler {
    @Autowired
    AuthHandshakeInterceptor handshakeInterceptor;

    @Override
    public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {
        return handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes);
    }
}
