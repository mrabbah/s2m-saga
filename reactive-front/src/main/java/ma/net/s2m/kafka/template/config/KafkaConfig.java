package ma.net.s2m.kafka.template.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.util.GlobalTracer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;

import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaOperations;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaTemplate;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionInitialOffset;

/**
 *
 * @author rabbah
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.topic.transaction.request.name}")
    private String requestTopic;

    @Value("${kafka.topic.transaction.reply.name}")
    private String replyTopic;

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    @Value("${kafka.topic.transaction.reply.partition}")
    private Integer replyPartition;

    @Value("${kafka.topic.transaction.partitions-num}")
    private Integer totalPartitions;

    @Value("${jeager.servicename}")
    private String jeagerServiceName;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset; //"earliest" or //latest
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;
    
    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryEndPoint;
    @Value("${spring.kafka.properties.schema.registry.basic.auth.user.info}")
    private String schemaregistryUserInfo;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        // Tracing distribuer en utilisant Jeager
        if (jeagerServiceName != null) {
            Tracer tracer = io.jaegertracing.Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);
            props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
        }
   
        if (schemaRegistryEndPoint != null) {
            props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                    schemaRegistryEndPoint);
            if (schemaregistryUserInfo != null) {
                props.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
                props.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG,
                        schemaregistryUserInfo);
            }

        }
        
        return props;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        // Tracing distribuer en utilisant Jeager
        if (jeagerServiceName != null) {
            Tracer tracer = io.jaegertracing.Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);
            props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        }
        
        if (schemaRegistryEndPoint != null) {
            props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                    schemaRegistryEndPoint);
            if (schemaregistryUserInfo != null) {
                props.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
                props.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG,
                        schemaregistryUserInfo);
            }

        }
        return props;
    }
    
    @Bean
    public CompletableFutureReplyingKafkaOperations<String, TransactionRequest, TransactionResponse> replyKafkaTemplate() {
        CompletableFutureReplyingKafkaTemplate<String, TransactionRequest, TransactionResponse> requestReplyKafkaTemplate
                = new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
                        replyListenerContainer());
        requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
        requestReplyKafkaTemplate.setReplyTimeout(replyTimeout);
        
        return requestReplyKafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, TransactionRequest> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, TransactionResponse> replyConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaMessageListenerContainer<String, TransactionResponse> replyListenerContainer() {

        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        
        if (replyPartition == null) {
            log.info("Using only topic: " + replyTopic + " to receive response");
            
            return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
        } else {
            if (replyPartition > totalPartitions - 1) {
                throw new KafkaException("Reply partition number must be below number of total partitions created for the topic: " + replyTopic);
            }
            log.info("Using Reply Topic: " + replyTopic + " and Partition : " + replyPartition + " to receive response");
            TopicPartitionInitialOffset topicPartitionOffset = new TopicPartitionInitialOffset(replyTopic, replyPartition);
            
            return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties, topicPartitionOffset);
        }

    }

}
