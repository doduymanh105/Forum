package com.example.forum.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMqConfig {

    public static final String EMAIL_QUEUE = "email.otp.queue";
    public static final String NOTIFICATION_EXCHANGE="forum.notification.exchange";
    public static final String EMAIL_ROUTING_KEY="email.otp.key";

    public static final String ALERT_QUEUE = "email.alert.queue";
    public static final String ALERT_ROUTING_KEY = "email.alert.key";

    public static final String DEAD_LETTER_EXCHANGE = "forum.dlx.exchange";
    public static final String DEAD_LETTER_QUEUE = "email.otp.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "email.otp.dlq.key";

    public static final String POST_FANOUT_EXCHANGE = "forum.post.fanout.exchange";
    public static final String NOTIFICATION_QUEUE = "post.notification.queue";

    public static final String INTERACTION_EXCHANGE = "forum.interaction.exchange";

    public static final String VOTE_QUEUE = "interaction.vote.queue";
    public static final String VOTE_ROUTING_KEY = "interaction.vote.key";

    public static final String FOLLOW_QUEUE = "interaction.follow.queue";
    public static final String FOLLOW_ROUTING_KEY = "interaction.follow.key";

    public static final String PRIVATE_NOTIFICATION_QUEUE = "interaction.notification.queue";
    public static final String PRIVATE_NOTI_ROUTING_KEY = "interaction.notification.key";

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public DirectExchange interactionExchange(){
        return new DirectExchange(INTERACTION_EXCHANGE);
    }

    @Bean
    public Queue voteQueue(){
        return QueueBuilder.durable(VOTE_QUEUE).build();
    }

    @Bean
    public Binding bindingVoteQueue(){
        return BindingBuilder
                .bind(voteQueue())
                .to(interactionExchange())
                .with(VOTE_ROUTING_KEY);
    }

    @Bean
    public Queue privateNotificationQueue(){
        return QueueBuilder.durable(PRIVATE_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding bindingPrivateNotificationQueue(){
        return BindingBuilder.bind(privateNotificationQueue())
                .to(notificationExchange())
                .with(PRIVATE_NOTI_ROUTING_KEY);
    }

    @Bean
    public Queue followQueue() {
        return QueueBuilder.durable(FOLLOW_QUEUE).build();
    }
    @Bean
    public Binding bindingFollowQueue() {
        return BindingBuilder.bind(followQueue()).to(interactionExchange()).with(FOLLOW_ROUTING_KEY);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Queue emailQueue(){
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY).build();
    }

    @Bean
    public Queue alertQueue(){
        return new Queue(ALERT_QUEUE, true);
    }

    @Bean
    public DirectExchange notificationExchange(){
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding bindingAlertQueue(Queue alertQueue, DirectExchange notificationExchange){
        return BindingBuilder.bind(alertQueue)
                .to(notificationExchange)
                .with(ALERT_ROUTING_KEY);
    }

    @Bean
    public Binding bindingEmailQueue(Queue emailQueue, DirectExchange notificationExchange){
        return BindingBuilder.bind(emailQueue)
                .to(notificationExchange)
                .with(EMAIL_ROUTING_KEY);
    }


    @Bean
    public FanoutExchange postFanoutExchange(){
        return new FanoutExchange(POST_FANOUT_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue(){
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Binding bindingNotification(){
        return BindingBuilder.bind(
                notificationQueue()).to(postFanoutExchange());
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper){
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }


    @Bean(name = "batchContainerFactory")
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(50);
        factory.setReceiveTimeout(3000L);

        return factory;
    }

}
