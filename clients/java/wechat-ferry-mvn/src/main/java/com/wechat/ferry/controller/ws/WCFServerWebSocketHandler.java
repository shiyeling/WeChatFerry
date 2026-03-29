package com.wechat.ferry.controller.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wechat.ferry.controller.ws.msg.*;
import com.wechat.ferry.controller.ws.msg.cmd.WzCmdMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

import static com.wechat.ferry.controller.ws.AuthHandshakeInterceptor.CLIENT_TOKEN;

@Component
@Slf4j
public class WCFServerWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public WCFServerWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        String clientToken = attributes.get(CLIENT_TOKEN).toString();
        log.info("Robot with token {} connected", clientToken);
        sessionManager.addSession(clientToken, session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("📩 Received: " + payload);
        if (payload.startsWith("CONNECT")) {
            // 回显消息或自定义逻辑
            session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
            return;
        }
        JsonNode root = null;
        MessageType type = null;
        try {
            root = objectMapper.readTree(message.getPayload());
            type = MessageType.valueOf(root.get("type").asText());
        } catch (Exception e) {
            log.error("Error parsing client message: {}", e.getMessage(), e);
            session.sendMessage(new TextMessage("Invalid message: " + message.getPayload() + ", Error: " + e.getMessage()));
        }

        switch (type) {
            case PING: {
                Ping ping = objectMapper.treeToValue(root, Ping.class);
                handlePing(session, ping);
                break;
            }
            case SR: {
                StatusReport cmd = objectMapper.treeToValue(root, StatusReport.class);
                handleStatusReport(session, cmd);
                break;
            }
            case CMD: {
                CommandType cmd = CommandType.valueOf(root.get("cmd").asText());
                WzCmdMessage cmdMessage = objectMapper.treeToValue(root, WzCmdMessage.class);
                switch (cmd) {
                    case PLAY: {
                        log.info("Got play from {}", session.getId());
                        sessionManager.sendCmd(cmdMessage.getTgt(), cmdMessage);
                        break;
                    }
                    case PAUSE: {
                        log.info("Got PAUSE from {}", session.getId());
                        sessionManager.sendCmd(cmdMessage.getTgt(), cmdMessage);
                        break;
                    }
                    case LS: {
                        log.info("Got LS from {}", session.getId());
                        String account = sessionManager.getSessionToken(session.getId());
                        if (StringUtils.isBlank(account)) {
                            log.error("会话{}无账户信息", session.getId());
                        } else {
//                            List<PlayerStatusInfo> accountPlayerList = sessionManager.getAccountPlayerList(account);
//                            sessionManager.sendMessage(new PlayerListMessage(accountPlayerList, session.getId()));
//                            sessionManager.sendMessageToSession(session.getId(), objectMapper.writeValueAsString(accountPlayerList));
                        }
                        break;
                    }
                    case RS: {
                        String tgt = cmdMessage.getTgt();
                        log.info("Got Report Status request from {} for player session {}", session.getId(), tgt);
                        log.warn("RS not supported here");
                        break;
                    }
                    case SET_SPEED: {
                        log.info("Got SET_SPEED from {}", session.getId());
                        sessionManager.sendCmd(cmdMessage.getTgt(), cmdMessage);
                        break;
                    }
                    case SWITCH_PAGE: {
                        log.info("Got SWITCH_PAGE from {}", session.getId());
                        sessionManager.sendCmd(cmdMessage.getTgt(), cmdMessage);
                        break;
                    }
                    default:
                        session.sendMessage(new TextMessage("Unknown type: " + type));
                }
                break;
            }
            default:
                session.sendMessage(new TextMessage("Unknown type: " + type));
        }

    }

    private void handleStatusReport(WebSocketSession session, StatusReport cmd) {
        sessionManager.updateRobotStatus(session, cmd.getData());
    }

    private void handlePing(WebSocketSession session, Ping ping) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new Pong(null, session.getId()))));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("❌ WebSocket closed: {}", session.getId());
        sessionManager.removeSession(session.getId());
    }
}
