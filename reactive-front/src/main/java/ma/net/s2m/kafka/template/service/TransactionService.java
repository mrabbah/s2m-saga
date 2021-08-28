package ma.net.s2m.kafka.template.service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaOperations;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class TransactionService {

    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, TransactionRequest, TransactionResponse> requestReplyKafkaTemplate;
    
    @Autowired
    private KafkaTemplate<String, TransactionRequest> producerTemplate;

    @Value("${kafka.topic.transaction.request.name}")
    private String requestTopic;

    @Value("${kafka.topic.transaction.completed.name}")
    private String completedTopic;

    @Value("${kafka.topic.transaction.failed.name}")
    private String failedTopic;
    
    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;

    public TransactionResponse proceed(TransactionRequest request) {
        try {
            Mono<TransactionResponse> response = this.requestReply(request);
            TransactionResponse reply = response.block(Duration.of(replyTimeout, ChronoUnit.MILLIS));
            if (this.completed(request)) {
                return reply;
            } else {
                throw new KafkaException("Failed to successfully notify others Microservices");
            }
        } catch (Exception ex) {
            try {
                this.failed(request);
            } catch (Exception e) {
                log.error("Transaction : " + request.toString() + " Failed and manual notification must be done to others Microservices");
                throw e;
            } finally {
                throw ex;
            }
        }
    }

    private boolean completed(TransactionRequest request) {
        try {
            producerTemplate.send(completedTopic, request);
            return true;
        } catch(Exception ex) {
            log.error("Failed to notify others Ms with success transaction: " + request.toString() + " error detail: " + ex.getMessage());
            return false;
        }
        
    }

    private boolean failed(TransactionRequest request) {
        try {
            producerTemplate.send(failedTopic, request);
            return true;
        } catch(Exception ex) {
            log.error("Failed to notify others Ms with failed transaction: " + request.toString() + " must notify manually, error detail: " + ex.getMessage());
            return false;
        }
    }

    private Mono<TransactionResponse> requestReply(TransactionRequest request) {
        Mono<TransactionResponse> response = Mono.fromFuture(requestReplyAsync(request));
        return response;
    }

    private CompletableFuture<TransactionResponse> requestReplyAsync(TransactionRequest request) {
        return requestReplyKafkaTemplate.requestReply(requestTopic, request);
    }
}
