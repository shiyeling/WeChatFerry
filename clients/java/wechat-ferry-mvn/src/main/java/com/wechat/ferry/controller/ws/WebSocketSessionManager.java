package com.wechat.ferry.controller.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Preconditions;
import com.wechat.ferry.controller.ws.dto.RobotStatus;
import com.wechat.ferry.controller.ws.msg.WzWebSocketMessage;
import com.wechat.ferry.controller.ws.msg.cmd.WzCmdMessage;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static com.wechat.ferry.controller.ws.AuthHandshakeInterceptor.CLIENT_TOKEN;


@Component
@Slf4j
public class WebSocketSessionManager {
    // 用于对结构化消息内容进行序列化反序列化的工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 账号/Token对应的session列表，
    private final ConcurrentHashMap<String, WebSocketSession> tokenSessionMap = new ConcurrentHashMap<>();

    // Session ID 到 session对象的缓存
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();


    /**
     * Session ID 到 播放器信息的列表
     */
    private final ConcurrentHashMap<String, RobotStatus> robotStatusMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Synchronized
    public void addSession(String token, WebSocketSession session) {

        sessionMap.put(session.getId(), session); // 加入全局的 session 寄存器
        WebSocketSession exisiting = tokenSessionMap.getOrDefault(token, null);
        if (exisiting != null) {
            log.info("Found existing session {}, removing", exisiting.getId());
            removeSession(token, session.getId());
        }
        tokenSessionMap.put(token, session);
    }

    public void updateRobotStatus(WebSocketSession session, RobotStatus status) {
        log.info("Got player status update from {}: {}", session.getId(), status);
        String token = getSessionToken(session.getId());
        robotStatusMap.put(token, status);
    }

    /**
     * 获取当前账户可管理的全部播放器
     *
     * @param session socket session
     * @return 指定sessionId的账户信息
     */
    public String getSessionToken(WebSocketSession session) {
        Preconditions.checkArgument(session != null, "Session must not be null");
        String token = (String) session.getAttributes().get(CLIENT_TOKEN);
        if (StringUtils.isBlank(token)) {
            log.error("Account Id not available in session {} ", session);
        }
        return token;
    }

    public String getSessionToken(String sessionId) {
        WebSocketSession session = sessionMap.get(sessionId);
        Preconditions.checkArgument(session != null, "No session found with id " + sessionId);
        return getSessionToken(session);
    }


    public RobotStatus getPlayerStatus(String sessionId) {
        return robotStatusMap.getOrDefault(sessionId, null);
    }

    @Synchronized
    public void removeSession(WebSocketSession session) {

    }

    public void removeSession(String sessionId) {
        log.info("Removing session {}", sessionId);
        try (WebSocketSession removed = sessionMap.remove(sessionId)) {
            if (removed != null) {
                if (removed.isOpen()) {
                    removed.close();
                }
                String token = (String) removed.getAttributes().get(CLIENT_TOKEN);
                log.info("Session has token {}", token);
                removeClientSession(token, sessionId);
            } else {
                log.info("Session already removed.");
            }
        } catch (Exception ignore) {
            log.warn("Close stale socket session {} got exception {}", sessionId, ignore.getMessage(), ignore);
        }
    }

    public void removeSession(String token, String sessionId) {
        log.info("Removing session {} of token {}", sessionId, token);
        try (WebSocketSession removed = sessionMap.remove(sessionId)) {
            if (removed != null && removed.isOpen()) {
                removed.close();
            }
            removeClientSession(token, sessionId);
        } catch (Exception ignore) {
            log.warn("Close stale socket session {} got exception {}", sessionId, ignore.getMessage(), ignore);
        }
    }

    private void removeClientSession(String account, String sessionId) {
        WebSocketSession remove = tokenSessionMap.remove(account);
        // 移除该session的播放器状态缓存
        robotStatusMap.remove(sessionId);
    }

//    private void removeSessionFromListenMap() {
//        // 移除该session的监听列表
//        listenMap.remove(sessionId);
//        // 通知所有监听该sessionId的session，它已下线。
//        listenMap.forEach((listenerSessionId, listeneeSessionId) -> {
//            if (listeneeSessionId.equalsIgnoreCase(sessionId)) {
//                // 找到了一个监听关系， listenerSessionId 正在监听下线的那个
//                try {
//                    log.info("Session {} is listening {}, will notify the offline event with updated player list", listenerSessionId, listeneeSessionId);
//                    String sessionAccount = getSessionToken(listenerSessionId);
//                    List<String> accountPlayerList = getAccountPlayerList(sessionAccount);

    /// /                    sendMessage(new PlayerListMessage(accountPlayerList, listenerSessionId));
//                } catch (Exception e) {
//                    log.error("Exception when notifying listener {} of session {} removal: {}", listenerSessionId, listeneeSessionId, e.getMessage(), e);
//                }
//            }
//        });
//    }
    public void sendCmd(String sessionId, WzCmdMessage cmdMessage) throws JsonProcessingException {
        sendMessageToSession(sessionId, objectMapper.writeValueAsString(cmdMessage));
    }

    public void sendMessageToSession(WebSocketSession session, String message) throws IOException {
        Preconditions.checkArgument(session != null, "WebSocketSession不可为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(message), "消息不可为空");
        session.sendMessage(new TextMessage(message));
    }

    public void sendMessage(WzWebSocketMessage message) throws JsonProcessingException {
        log.info("Sending {}", message);
        sendMessageToSession(message.getTgt(), objectMapper.writeValueAsString(message));
    }

    public void sendMessageToSession(String sessionId, String message) {
        WebSocketSession session = sessionMap.getOrDefault(sessionId, null);
        if (session != null && session.isOpen()) {
            try {
                sendMessageToSession(session, message);
            } catch (IOException e) {
                log.error("IOException {}", e.getMessage(), e);
            }
        } else {
            if (session == null) {
                log.error("No session found with id {}", sessionId);
            } else {
                log.error("Session {} is closed", sessionId);
            }
        }
    }
}
