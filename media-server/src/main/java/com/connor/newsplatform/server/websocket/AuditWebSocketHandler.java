package com.connor.newsplatform.server.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuditWebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String receiverType = extractReceiverType(session);
        Long receiverId = extractReceiverId(session);
        if (receiverId == null) {
            return;
        }
        if ("admin".equals(receiverType)) {
            adminSessions.put(receiverId, session);
        } else if ("user".equals(receiverType)) {
            userSessions.put(receiverId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String receiverType = extractReceiverType(session);
        Long receiverId = extractReceiverId(session);
        if (receiverId == null) {
            return;
        }
        if ("admin".equals(receiverType)) {
            adminSessions.remove(receiverId);
        } else if ("user".equals(receiverType)) {
            userSessions.remove(receiverId);
        }
    }

    public void sendToAdmin(Long adminId, String message) {
        send(adminSessions, adminId, message);
    }

    public void sendToUser(Long userId, String message) {
        send(userSessions, userId, message);
    }

    public void broadcastAdmins(String message) {
        adminSessions.forEach((adminId, session) -> sendToAdmin(adminId, message));
    }

    public void broadcast(String message) {
        broadcastAdmins(message);
    }

    private void send(Map<Long, WebSocketSession> sessions, Long receiverId, String message) {
        WebSocketSession session = sessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException ignored) {
                sessions.remove(receiverId);
            }
        }
    }

    private String extractReceiverType(WebSocketSession session) {
        Object receiverType = session.getAttributes().get("receiverType");
        if (receiverType instanceof String type) {
            return type;
        }
        Object adminId = session.getAttributes().get("adminId");
        return adminId == null ? null : "admin";
    }

    private Long extractReceiverId(WebSocketSession session) {
        Object receiverId = session.getAttributes().get("receiverId");
        if (receiverId instanceof Long id) {
            return id;
        }
        Object adminId = session.getAttributes().get("adminId");
        if (adminId instanceof Long id) {
            return id;
        }
        return null;
    }
}
