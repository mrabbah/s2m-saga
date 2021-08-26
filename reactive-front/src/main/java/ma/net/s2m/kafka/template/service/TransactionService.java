package ma.net.s2m.kafka.template.service;

import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaOperations;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class TransactionService {

    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, TransactionRequest, TransactionResponse> avroRequestReplyKafkaTemplate;

    @Value("${kafka.topic.transaction.request.name}")
    private String requestTopic;

    public Mono<TransactionResponse> init(TransactionRequest request) {
        /*try {
            return initAsync(request).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get Car", e);
        }*/
         Mono<TransactionResponse> response = Mono.fromFuture(initAsync(request));
         return response;
    }

    public CompletableFuture<TransactionResponse> initAsync(TransactionRequest request) {
        return avroRequestReplyKafkaTemplate.requestReply(requestTopic, request);
    }

    public Mono<TransactionResponse> init(String origin, Double amount) {
        TransactionRequest request = new TransactionRequest(amount, origin);
        return this.init(request);
        /*ZonedDateTime zdt = ZonedDateTime.now();
        TransactionResponse tx = new TransactionResponse(
                UUID.randomUUID().toString().toUpperCase(),
                amount,
                zdt.toInstant().toEpochMilli(),
                origin, 0.0
        );*/

        // return this.init(tx);
        // return tx;
    }

    /*@KafkaListener(topics = "users", groupId = "group_id")
    public void consume(ConsumerRecord<String, TransactionResponse> record) throws IOException {
        log.info(String.format("#### -> Consumed message -> %s", record.value().toString()));
        log.info(String.format("#### -> Consumed message headers -> %s", record.headers().toString()));
    }*/
}
