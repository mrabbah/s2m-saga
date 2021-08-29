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
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.FeeRequest;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


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
    
    
    ///////////// Start Fees Reply Configuration //////////////
    @Bean
    public ConsumerFactory<String, FeeRequest> feesRequestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean("feesRequestReplyListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, FeeRequest>> feesRequestReplyListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FeeRequest> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(feesRequestConsumerFactory());
        factory.setReplyTemplate(feesReplyTemplate());
        return factory;
    }

    @Bean("feesListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, FeeRequest>> feesListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FeeRequest> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(feesRequestConsumerFactory());
        return factory;
    }
    
    @Bean
    public ProducerFactory<String, FeeResponse> feesReplyProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, FeeResponse> feesReplyTemplate() {
        return new KafkaTemplate<>(feesReplyProducerFactory());
    }

    ///////////// End Fees Reply Configuration //////////////

}
