package com.bbytes.recruiz.web.config;

import java.security.Principal;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.bbytes.recruiz.auth.jwt.TokenHandler;
import com.bbytes.recruiz.service.SpringProfileService;

@Configuration
@EnableWebSocketMessageBroker
@Controller
class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

	private TokenHandler tokenHandler;

	private DefaultSimpUserRegistry userRegistry = new DefaultSimpUserRegistry();
	private DefaultUserDestinationResolver resolver = new DefaultUserDestinationResolver(userRegistry);

	@Value("${token.handler.secret}")
	private String secret;

	@Value("${api.token.handler.secret}")
	private String apiSecret;
	
	@Autowired
	private SpringProfileService springProfileService;
	
	@Bean
	@Primary
	public SimpUserRegistry userRegistry() {
		return userRegistry;
	}

	@Bean
	@Primary
	public UserDestinationResolver userDestinationResolver() {
		return resolver;
	}
	
	@PostConstruct
	public void setupTokenHandler() {
		tokenHandler = new TokenHandler(secret, apiSecret, springProfileService.isSaasMode(),24);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/queue/","/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint("/websocket").setAllowedOrigins("*:*").withSockJS().setWebSocketEnabled(false)
				.setSessionCookieNeeded(false);

	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(new ChannelInterceptorAdapter() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

				Message<byte[]> msg = (Message<byte[]>) message;

				List tokenList = accessor.getNativeHeader("X-AUTH-TOKEN");
				accessor.removeNativeHeader("X-AUTH-TOKEN");

				String token = null;
				if (tokenList != null && tokenList.size() > 0) {
					token = tokenList.get(0).toString();
				

				//TokenDataHolder tokenDataHolder = tokenHandler.parseJWTStringTokenForUser(token);

				Principal principal =  new BasicUserPrincipal(token);

				if (accessor.getMessageType() == SimpMessageType.CONNECT) {
					userRegistry.onApplicationEvent(new SessionConnectedEvent(this, msg, principal));
				} else if (accessor.getMessageType() == SimpMessageType.SUBSCRIBE) {
					userRegistry.onApplicationEvent(new SessionSubscribeEvent(this, msg, principal));
				} else if (accessor.getMessageType() == SimpMessageType.UNSUBSCRIBE) {
					userRegistry.onApplicationEvent(new SessionUnsubscribeEvent(this, msg, principal));
				} else if (accessor.getMessageType() == SimpMessageType.DISCONNECT) {
					userRegistry.onApplicationEvent(
							new SessionDisconnectEvent(this, msg, accessor.getSessionId(), CloseStatus.NORMAL));
				}

				accessor.setUser(principal);

				// not documented anywhere but necessary otherwise NPE in
				// StompSubProtocolHandler!
				accessor.setLeaveMutable(true);
				}
				return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
			}
		});
	}

}