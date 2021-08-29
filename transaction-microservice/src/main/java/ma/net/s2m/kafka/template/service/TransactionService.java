package ma.net.s2m.kafka.template.service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import ma.net.s2m.kafka.template.commun.kafkareqrep.CompletableFutureReplyingKafkaOperations;
import ma.net.s2m.kafka.template.commun.saga.ChoreographyService;
import ma.net.s2m.kafka.template.commun.saga.SagaState;
import ma.net.s2m.kafka.template.example.dto.FeeRequest;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class TransactionService implements ChoreographyService<TransactionRequest, TransactionResponse> {

    Map<String, TransactionResponse> transactionsDb = new HashMap<>();
    Map<String, SagaState> transactionsState = new HashMap<>();
    Map<String, FeeRequest> feesDb = new HashMap<>();
    
    @Autowired
    @Qualifier("feeProducerTemplate")
    private KafkaTemplate<String, FeeRequest> feeProducerTemplate;
    
    @Autowired
    private CompletableFutureReplyingKafkaOperations<String, FeeRequest, FeeResponse> feesReplyKafkaTemplate;
    
    @Value("${kafka.topic.fees.request.name}")
    private String feesRequestTopic;

    @Value("${kafka.topic.fees.completed.name}")
    private String feesCompletedTopic;

    @Value("${kafka.topic.fees.failed.name}")
    private String feesFailedTopic;
    
    @Value("${kafka.request-reply.timeout-ms}")
    private Long replyTimeout;
    
    @KafkaListener(topics = "${kafka.topic.transaction.request.name}", containerFactory = "transactionRequestReplyListenerContainerFactory")
    @SendTo()
    public TransactionResponse proceed(TransactionRequest request) {
        log.info("received request for Transaction: " + request.toString());
        
        log.info("Delegating Fees calculation to outside Microservice (Request/Reply) throws Kafka");
        FeeRequest feeRequest = new FeeRequest(request.getUuid(), request.getAmount());
        Mono<FeeResponse> response = this.requestReply(feeRequest);
        FeeResponse feesReply = response.block(Duration.of(replyTimeout, ChronoUnit.MILLIS));
        
        feesDb.put(request.getUuid(), feeRequest);
        
        ZonedDateTime zdt = ZonedDateTime.now();
        TransactionResponse tx = new TransactionResponse(
                request.getUuid(),
                request.getAmount(),
                zdt.toInstant().toEpochMilli(),
                request.getOrigin(), feesReply.getFees()
        );
        transactionsDb.put(request.getUuid(), tx);
        transactionsState.put(request.getUuid(), SagaState.InProgress);
        log.info("Sending response : " + tx.toString());
        return tx;
    }
    
    @Override
    @KafkaListener(topics = "${kafka.topic.transaction.completed.name}", 
            containerFactory = "transactionListenerContainerFactory")
    public boolean completed(TransactionRequest request) {
        log.info("Transaction: " + request.toString() + " Completed successfully");
        if(transactionsState.containsKey(request.getUuid())) {
            // Here put your Business Logic to confirm the transaction
            transactionsState.put(request.getUuid(), SagaState.Completed);
            if(feesDb.containsKey(request.getUuid())) {
                feeProducerTemplate.send(feesCompletedTopic, feesDb.get(request.getUuid()));
            } else {
                log.error("Fees for transaction: " + request + " Not found" );
            }
        } else {
            log.error("Transaction: " + request + " Not found");
        }
        return true;
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.transaction.failed.name}", 
            containerFactory = "transactionListenerContainerFactory")
    public boolean failed(TransactionRequest request) {
        log.error("Transaction: " + request.toString() + " Failed");
        if(transactionsState.containsKey(request.getUuid())) {
            // Here put your Business Logic to rollback the transaction
            transactionsState.put(request.getUuid(), SagaState.Failed);
            if(feesDb.containsKey(request.getUuid())) {
                feeProducerTemplate.send(feesFailedTopic, feesDb.get(request.getUuid()));
            } else {
                log.error("Fees for transaction: " + request + " Not found" );
            }
        } else {
            log.error("Transaction: " + request + " Not found");
        }
        return true;
    }

    private Mono<FeeResponse> requestReply(FeeRequest request) {
        Mono<FeeResponse> response = Mono.fromFuture(requestReplyAsync(request));
        return response;
    }

    private CompletableFuture<FeeResponse> requestReplyAsync(FeeRequest request) {
        return feesReplyKafkaTemplate.requestReply(feesRequestTopic, request);
    }
}
