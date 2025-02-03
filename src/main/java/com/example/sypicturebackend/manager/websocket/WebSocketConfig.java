package com.example.sypicturebackend.manager.websocket;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import javax.annotation.Resource;
/**
 * WebSocket 配置（定义连接）
 * @author 诺诺
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Resource
	private PictureEditHandler pictureEditHandler;
	@Resource
	private WsHandshakeInterceptor wsHandshakeInterceptor;
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(pictureEditHandler, "/ws/picture/edit")
				.addInterceptors(wsHandshakeInterceptor)
				.setAllowedOrigins("*");
	}
}