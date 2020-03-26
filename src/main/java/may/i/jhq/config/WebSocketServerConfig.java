package may.i.jhq.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket Server Config
 *
 * @author May
 * @version 1.0
 * @date 2020/2/15 19:36
 */
@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketServerConfig implements WebSocketMessageBrokerConfigurer {

	private static final String ENDPOINT = "/may/ws";

	private static final String DESTINATION_PREFIX_TOPIC = "/topic";

	private static final String DESTINATION_PREFIX_USER = "/user";

	private static final String APPLICATION_DESTINATION_PREFIX = "/app/";

	private static final String USER_DESTINATION_PREFIX = "/user/";

	/**
	 * 服务端传出，也可用于传入
	 * 配置传输的时长和同事能并发多少数据量
	 * 数据量过大，stomp会对数据进行分割
	 */
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setSendTimeLimit(15 * 1000).setSendBufferSizeLimit(512 * 1024);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		// 允许使用socketJs方式访问，允许跨域
		stompEndpointRegistry.addEndpoint(ENDPOINT).setAllowedOrigins("*").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
//		registry.enableSimpleBroker(DESTINATION_PREFIX_TOPIC, DESTINATION_PREFIX_USER)
//				.setHeartbeatValue(new long[]{10000, 10000}).setTaskScheduler(pingScheduler);
		registry.enableStompBrokerRelay(DESTINATION_PREFIX_TOPIC, DESTINATION_PREFIX_USER)
				.setRelayHost("127.0.0.1")
				.setRelayPort(61613)
				.setClientLogin("rabbit")
				.setClientPasscode("rabbit")
				.setVirtualHost("/")
				.setSystemLogin("rabbit")
				.setSystemPasscode("rabbit")
				.setSystemHeartbeatSendInterval(20)
				.setSystemHeartbeatReceiveInterval(20);

		// 目标标头以“/app”开头的STOMP消息将被路由到@Controller类中的@MessageMapping方法。
		registry.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX);
		// 点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
		registry.setUserDestinationPrefix(USER_DESTINATION_PREFIX);
	}

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxSessionIdleTimeout(10000L);
		return container;
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					accessor.setUser(() -> "admin");
				}
				return message;
			}
		});
	}
}