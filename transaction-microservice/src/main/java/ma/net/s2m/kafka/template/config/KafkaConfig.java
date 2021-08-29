package ma.net.s2m.kafka.template.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.util.GlobalTracer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaOperations;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaTemplate;
import ma.net.s2m.kafka.template.example.dto.FeeRequest;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
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

    @Value("${kafka.topic.fees.request.name}")
    private String feeRequestTopic;

    @Value("${kafka.topic.fees.reply.name}")
    private String feeReplyTopic;

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    @Value("${kafka.topic.fees.reply.partition}")
    private Integer feeReplyPartition;

    @Value("${kafka.topic.fees.partitions-num}")
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

    ///////////// Start General Configuration //////////////
    @Bean
    public Map<String, Object> consumerConfigs() {
        log.info("Consumer config bean creation...");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());
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
        log.info("Producer config bean creation...");
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());
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

    ///////////// End General Configuration //////////////
    
    
    ///////////// Start Transaction Reply Configuration //////////////
    @Bean
    public ConsumerFactory<String, TransactionRequest> transactionRequestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean("transactionRequestReplyListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, TransactionRequest>> transactionRequestReplyListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionRequest> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionRequestConsumerFactory());
        factory.setReplyTemplate(transactionReplyTemplate());
        return factory;
    }

    @Bean("transactionListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, TransactionRequest>> transactionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionRequest> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionRequestConsumerFactory());
        return factory;
    }
    
    @Bean
    public ProducerFactory<String, TransactionResponse> transactionReplyProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TransactionResponse> transactionReplyTemplate() {
        return new KafkaTemplate<>(transactionReplyProducerFactory());
    }

    ///////////// End Transaction Reply Configuration //////////////
    
    
    ///////////// Start Fee Request/Reply Configuration //////////////
    @Bean("feeProducerTemplate")
    public KafkaTemplate<String,FeeRequest> feeProducerTemplate(){
        return new KafkaTemplate<>(feeRequestProducerFactory());
    }
    
    @Bean
    public CompletableFutureReplyingKafkaOperations<String, FeeRequest, FeeResponse> feeReplyKafkaTemplate() {
        CompletableFutureReplyingKafkaTemplate<String, FeeRequest, FeeResponse> requestReplyKafkaTemplate
                = new CompletableFutureReplyingKafkaTemplate<>(feeRequestProducerFactory(),
                        feeReplyListenerContainer());
        requestReplyKafkaTemplate.setDefaultTopic(feeRequestTopic);
        requestReplyKafkaTemplate.setReplyTimeout(replyTimeout);

        return requestReplyKafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, FeeRequest> feeRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, FeeResponse> feeReplyConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaMessageListenerContainer<String, FeeResponse> feeReplyListenerContainer() {

        ContainerProperties containerProperties = new ContainerProperties(feeReplyTopic);

        if (feeReplyPartition == null) {
            log.info("Using only topic: " + feeReplyTopic + " to receive response");

            return new KafkaMessageListenerContainer<>(feeReplyConsumerFactory(), containerProperties);
        } else {
            if (feeReplyPartition > totalPartitions - 1) {
                throw new KafkaException("Reply partition number must be below number of total partitions created for the topic: " + feeReplyTopic);
            }
            log.info("Using Reply Topic: " + feeReplyTopic + " and Partition : " + feeReplyPartition + " to receive response");
            TopicPartitionInitialOffset topicPartitionOffset = new TopicPartitionInitialOffset(feeReplyTopic, feeReplyPartition);

            return new KafkaMessageListenerContainer<>(feeReplyConsumerFactory(), containerProperties, topicPartitionOffset);
        }

    }

    ///////////// End Fee Request/Reply Configuration //////////////
}
