package com.ehb.connected.websockets;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99) //ensure that this configuration is loaded first
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    //configure the message broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //messages sent to the /user prefix will be routed to the message broker
        config.enableSimpleBroker("/user", "/topic");
        //messages sent to the /app prefix will be routed to message-handling methods
        config.setApplicationDestinationPrefixes("/app");

        config.setUserDestinationPrefix("/user");
    }

    //added new endpoint for the client to connect to the server
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:4200");
    }


    //dit zegt spring dat we een messageconverter willen toevoegen op JSON-formaat
    /*
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MediaType.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        //add other converters if needed
        return false;
    }
     */

}
