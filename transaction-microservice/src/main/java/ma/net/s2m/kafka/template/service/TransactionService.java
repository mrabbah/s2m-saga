package ma.net.s2m.kafka.template.service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.UUID;
import ma.net.s2m.kafka.template.commun.saga.ChoreographyService;
import ma.net.s2m.kafka.template.commun.saga.SagaState;
import ma.net.s2m.kafka.template.example.dto.FeeRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class TransactionService implements ChoreographyService<TransactionRequest, TransactionResponse> {

    Map<String, TransactionResponse> transactionsDb = new HashMap<>();
    Map<String, SagaState> transactionsState = new HashMap<>();
    
    @Autowired
    @Qualifier("feeProducerTemplate")
    private KafkaTemplate<String, FeeRequest> feeProducerTemplate;
    
    @KafkaListener(topics = "${kafka.topic.transaction.request.name}", containerFactory = "transactionRequestReplyListenerContainerFactory")
    @SendTo()
    public TransactionResponse proceed(TransactionRequest request) {
        log.info("received request for Transaction: " + request.toString());
        ZonedDateTime zdt = ZonedDateTime.now();
        TransactionResponse tx = new TransactionResponse(
                request.getUuid(),
                request.getAmount(),
                zdt.toInstant().toEpochMilli(),
                request.getOrigin(), 0.0
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
        } else {
            log.error("Transaction: " + request + " Not found");
        }
        return true;
    }

}
