package may.i.jhq.config;


import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

/**
 * WebSocket Server Event Listener
 *
 * @author May
 * @version 1.0
 * @date 2020/2/15 21:45
 */
@Component
@AllArgsConstructor
public class WebSocketEventListener {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpUserRegistry userRegistry;

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Published shortly after a SessionConnectEvent when the broker has sent a STOMP CONNECTED frame in response to the CONNECT. At this point,
     * the STOMP session can be considered fully established.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        LOGGER.info("WebSocket Server Create A New Connect, Username: {}", user.getName());
        LOGGER.info("WebSocket Server Current Online User Count: {}", userRegistry.getUserCount() + 1);
    }

	/**
	 * Published when a STOMP session ends.
     */
    @EventListener
    public void sessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        LOGGER.info("WebSocket Server Closed A Connect Session With User, Username: {}", user.getName());
        LOGGER.info("WebSocket Server Current Online User Count: {} ", userRegistry.getUserCount() - 1);
    }

    /**
     * Published when a new STOMP SUBSCRIBE is received.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        MessageHeaders messageHeaders = headerAccessor.getMessageHeaders();
        Principal user = headerAccessor.getUser();
        String simpDestination = (String) messageHeaders.get("simpDestination");
        LOGGER.info("WebSocket Server Get A New Client Subscriber, Username: {}, Destination: {}", user.getName(), simpDestination);
    }
}
