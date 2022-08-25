package com.bbytes.recruiz.rabbit;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.auklabs.recruiz.connect.core.utils.RabbitMQConstants;

//Commented out since connect feature is not used
@Configuration
@EnableRabbit
public class RabbitConfig implements RabbitListenerConfigurer {

	// /**
	// * Declaring the queues for recruiz saas application for india
	// *
	// * @return
	// */
	// @Bean
	// public List<Declarable> recruizSaasTopicBindings() {
	//
	// Queue positionTopicQueue = new
	// Queue(RabbitMQConstants.RECRUIZ_SAAS_POSITION_TOPIC_QUEUE, false);
	// Queue candidateTopicQueue = new
	// Queue(RabbitMQConstants.RECRUIZ_SAAS_CANDIDATE_TOPIC_QUEUE, false);
	//
	// TopicExchange recruizTopicExchange = new
	// TopicExchange(RabbitMQConstants.RECRUIZ_TOPIC_EXCHANGE);
	//
	// return Arrays.asList(positionTopicQueue, candidateTopicQueue,
	// recruizTopicExchange,
	// BindingBuilder.bind(positionTopicQueue).to(recruizTopicExchange)
	// .with(RabbitMQConstants.RECRUIZ_SAAS_POSITION_TOPIC_ROUTING_KEY),
	// BindingBuilder.bind(candidateTopicQueue).to(recruizTopicExchange)
	// .with(RabbitMQConstants.RECRUIZ_SAAS_CANDIDATE_TOPIC_ROUTING_KEY));
	// }
	//
	// /**
	// * Declaring the queues for recruiz connect application.
	// *
	// * @return
	// */
	// @Bean
	// public List<Declarable> recruizConnectTopicBindings() {
	//
	// Queue positionTopicQueue = new
	// Queue(RabbitMQConstants.RECRUIZ_CONNECT_POSITION_TOPIC_QUEUE, false);
	// Queue candidateTopicQueue = new
	// Queue(RabbitMQConstants.RECRUIZ_CONNECT_CANDIDATE_TOPIC_QUEUE, false);
	// TopicExchange connectTopicExchange = new
	// TopicExchange(RabbitMQConstants.RECRUIZ_CONNECT_TOPIC_EXCAHNGE);
	//
	// return Arrays.asList(positionTopicQueue, candidateTopicQueue,
	// connectTopicExchange,
	// BindingBuilder.bind(positionTopicQueue).to(connectTopicExchange)
	// .with(RabbitMQConstants.RECZ_CONNECT_POSITION_TOPIC_ROUTING_KEY),
	// BindingBuilder.bind(candidateTopicQueue).to(connectTopicExchange)
	// .with(RabbitMQConstants.RECZ_CONNECT_CANDIDATE_TOPIC_ROUTING_KEY));
	// }
	//
	// @Bean
	// public RabbitTemplate rabbitTemplate(final ConnectionFactory
	// connectionFactory) {
	// final RabbitTemplate rabbitTemplate = new
	// RabbitTemplate(connectionFactory);
	// rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
	// return rabbitTemplate;
	// }
	//
	// @Bean
	// public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
	// return new Jackson2JsonMessageConverter();
	// }
	//
	// @Bean
	// public MappingJackson2MessageConverter consumerJackson2MessageConverter()
	// {
	// return new MappingJackson2MessageConverter();
	// }
	//
	// @Bean
	// public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
	// DefaultMessageHandlerMethodFactory factory = new
	// DefaultMessageHandlerMethodFactory();
	// factory.setMessageConverter(consumerJackson2MessageConverter());
	// return factory;
	// }

	@Override
	public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
		// registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}
}
