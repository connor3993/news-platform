package com.connor.newsplatform.server.config;

import com.connor.newsplatform.server.websocket.AuditWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {
    private final AuditWebSocketHandler auditWebSocketHandler;

    public WebSocketConfig(AuditWebSocketHandler auditWebSocketHandler) {
        this.auditWebSocketHandler = auditWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(auditWebSocketHandler, "/ws/audit/{adminId}")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String path = request.getURI().getPath();
                        String adminId = path.substring(path.lastIndexOf('/') + 1);
                        attributes.put("adminId", Long.valueOf(adminId));
                        attributes.put("receiverType", "admin");
                        attributes.put("receiverId", Long.valueOf(adminId));
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                .setAllowedOriginPatterns("*");
        registry.addHandler(auditWebSocketHandler, "/ws/notify/{receiverType}/{receiverId}")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String[] parts = request.getURI().getPath().split("/");
                        String receiverType = parts[parts.length - 2];
                        String receiverId = parts[parts.length - 1];
                        if (!"admin".equals(receiverType) && !"user".equals(receiverType)) {
                            return false;
                        }
                        attributes.put("receiverType", receiverType);
                        attributes.put("receiverId", Long.valueOf(receiverId));
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                .setAllowedOriginPatterns("*");
    }
}
