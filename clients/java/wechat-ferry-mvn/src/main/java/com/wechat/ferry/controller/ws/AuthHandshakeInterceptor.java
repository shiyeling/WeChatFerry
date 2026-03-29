package com.wechat.ferry.controller.ws;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    String CLIENT_VERIFY_SUCCESS = "SUCCESS"; // 客户端证书验证成功字符串 X-Client-Verify:"SUCCESS" 或者 X-Client-Verify:"NONE"
    String CLIENT_VERIFY_NONE = "NONE"; //客户端证书验证失败字符串， X-Client-Verify:"SUCCESS" 或者 X-Client-Verify:"NONE"
    /**
     * 2025年新增的证书方式
     */
    String X_HEADER_CLIENT_VERIFY = "X-Client-Verify"; // 客户端是否带了证书验证： X-Client-Verify:"SUCCESS" 或者 X-Client-Verify:"NONE"
    String X_HEADER_CLIENT_DN = "X-Client-DN";  // X-Client-DN:"C=CN,O=weizhukeji.net,CN=abcde",
    String X_HEADER_CLIENT_SERIAL = "X-Client-Serial"; //客户端证书序列号，例如： X-Client-Serial:"019859975548"
    String X_HEADER_ISSUER_DN = "X-Client-I-DN"; //客户端证书名称： X-Client-I-DN:"CN=WBoxRootCA,OU=Security,O=Weizhukeji Ltd,C=CN,ST=Jiangsu,L=Nanjing"


    public static final String CLIENT_TOKEN = "CLIENT_TOKEN";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String queryString = httpRequest.getQueryString();
        log.info("Got query string {}", queryString);
        String token = httpRequest.getParameter("token");
        log.info("Got token {}", token);
        if (StringUtils.isNotBlank(token)) {
            attributes.put(CLIENT_TOKEN, token);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 无需额外处理
    }
}
