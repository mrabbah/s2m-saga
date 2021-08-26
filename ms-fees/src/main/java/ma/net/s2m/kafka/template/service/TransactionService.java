package ma.net.s2m.kafka.template.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.Transaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class TransactionService {
    
    @Value("${topic.name}") 
    private String TOPIC;
    
    @Autowired
    private KafkaTemplate<String, Transaction> kafkaTemplate;
    
    public Transaction init(Transaction transaction) {
        log.info(String.format("#### -> Init transaction -> %s", transaction.toString()));
        ProducerRecord<String, Transaction> record = new ProducerRecord<>(TOPIC, transaction.getUuid(), transaction);
        record.headers()
                .add("UUID", transaction.getUuid().getBytes(StandardCharsets.UTF_8))
                .add("TRIGGERED_ON", transaction.getTriggeredOn().getBytes(StandardCharsets.UTF_8));
        this.kafkaTemplate.send(record);
        return null;
    }
    
    public Transaction init(String origin, Double amount) {
        ZonedDateTime zdt = ZonedDateTime.now();
        Transaction tx = new Transaction(
                UUID.randomUUID().toString().toUpperCase(),
                amount,
                zdt.toInstant().toString(),
                origin
        );
        
        return this.init(tx);
    }
    
    @KafkaListener(topics = "users", groupId = "group_id")
    public void consume(ConsumerRecord<String, Transaction> record) throws IOException {
        log.info(String.format("#### -> Consumed message -> %s", record.value().toString()));
        log.info(String.format("#### -> Consumed message headers -> %s", record.headers().toString()));
    }
}
