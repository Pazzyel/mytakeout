package com.sky.websocket;

import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 建立连接成功调用方法
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端的连接: " + sid);
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message
     * @param sid
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端: " + sid + "的消息: " + message);
    }

    /**
     * 连接关闭调用的方法
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开: " + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     * @param message
     */
    public void sendToAllClients(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
