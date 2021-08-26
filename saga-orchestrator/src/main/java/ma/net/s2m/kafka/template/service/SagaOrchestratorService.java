package ma.net.s2m.kafka.template.service;

import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.UUID;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class SagaOrchestratorService {

    @KafkaListener(topics = "${kafka.topic.transaction.request.name}", containerFactory = "avroRequestReplyListenerContainerFactory")
    @SendTo()
    public TransactionResponse receive(TransactionRequest request) {
        log.info("received request for Transaction: " + request.toString());
        ZonedDateTime zdt = ZonedDateTime.now();
        TransactionResponse tx = new TransactionResponse(
                UUID.randomUUID().toString().toUpperCase(),
                request.getAmount(),
                zdt.toInstant().toEpochMilli(),
                request.getOrigin(), 0.0
        );
        log.info("Sending response : " + tx.toString());
        return tx;
    }

    /*@Value("${topic.name}") 
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
    }*/
}
