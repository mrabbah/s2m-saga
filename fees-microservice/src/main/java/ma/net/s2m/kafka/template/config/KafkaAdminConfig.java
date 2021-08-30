package ma.net.s2m.kafka.template.config;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Slf4j
@Configuration
public class KafkaAdminConfig {

    @Value("${kafka.topic.fees.request.name}")
    private String requestFeesTopicName;

    @Value("${kafka.topic.fees.reply.name}")
    private String replyFeesTopicName;
    
    @Value("${kafka.topic.fees.completed.name}")
    private String completedFeesTopicName;
    
    @Value("${kafka.topic.fees.failed.name}")
    private String failedFeesTopicName;

    @Value("${kafka.topic.fees.partitions-num}")
    private Integer topicsPartitions;

    @Value("${kafka.topic.fees.replication-factor}")
    private short replicationFactor;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    @Value("${spring.kafka.ssl.trust-store-password}")
    private String trustuedStorePassword;
    @Value("${spring.kafka.ssl.trust-store-location}")
    private String trustedStorePath;
    @Value("${spring.kafka.ssl.key-store-password}")
    private String keystorePassword;
    @Value("${spring.kafka.ssl.key-store-location}")
    private String keystorePath;
    @Value("${kafka.additionalconfig}")
    private String additionalConfig;
    
    @Bean
    NewTopic requestFeesTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", replyTimeout.toString());
        return new NewTopic(requestFeesTopicName, topicsPartitions, replicationFactor).configs(configs);
    }

    @Bean
    NewTopic completedFeesTopic() {
        return new NewTopic(completedFeesTopicName, topicsPartitions, replicationFactor);
    }
    
    @Bean
    NewTopic failedFeesTopic() {
        return new NewTopic(failedFeesTopicName, topicsPartitions, replicationFactor);
    }

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        if (additionalConfig != null && !additionalConfig.isEmpty()) {
            StringTokenizer tok = new StringTokenizer(additionalConfig, ", \t\n\r");
            while (tok.hasMoreTokens()) {
                String record = tok.nextToken();
                int endIndex = record.indexOf('=');
                if (endIndex == -1) {
                    throw new RuntimeException("Failed to parse Map from String");
                }
                String key = record.substring(0, endIndex);
                String value = record.substring(endIndex + 1);
                configs.put(key.trim(), value.trim());
            }
        }

        // Cryptage communication
        if (trustuedStorePassword != null && trustedStorePath != null) {
            log.info("Configuring truststore");
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            configs.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PKCS12");
            configs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustuedStorePassword);
            configs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustedStorePath);
        }

        // Authentification MTLS
        if (keystorePassword != null && keystorePath != null) {
            log.info("Configuring keystore");
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            configs.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            configs.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
            configs.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
        }

        return new KafkaAdmin(configs);
    }
    
}
