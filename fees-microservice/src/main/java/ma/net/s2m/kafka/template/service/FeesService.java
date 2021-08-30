package ma.net.s2m.kafka.template.service;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.clients.FeesCurrencyConverterClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ma.net.s2m.kafka.template.commun.saga.ChoreographyService;
import ma.net.s2m.kafka.template.commun.saga.SagaState;
import ma.net.s2m.kafka.template.example.dto.FeeRequest;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 *
 * @author rabbah
 */
@Service
@Slf4j
public class FeesService implements ChoreographyService<FeeRequest, FeeResponse> {

    Map<String, FeeResponse> feesDb = new HashMap<>();
    Map<String, SagaState> feesState = new HashMap<>();
    
    @Autowired
    FeesCurrencyConverterClient feesCurrencyConverterClient;
      
    @KafkaListener(topics = "${kafka.topic.fees.request.name}", containerFactory = "feesRequestReplyListenerContainerFactory")
    @SendTo()
    public FeeResponse proceed(FeeRequest request) {
        log.info("received request for Fee: " + request.toString());
        Long id = feesDb.size() + 1L;
        Double feeAmount = request.getAmount() / 10d;
        FeeResponse feeMAD = new FeeResponse(id, request.getTransactionUuid(), feeAmount);
        FeeResponse fee = feesCurrencyConverterClient.convert(feeMAD);
        feesDb.put(request.getTransactionUuid(), fee);
        feesState.put(request.getTransactionUuid(), SagaState.InProgress);
        log.info("Sending response : " + fee.toString());
        return fee;
    }
    
    @Override
    @KafkaListener(topics = "${kafka.topic.fees.completed.name}", 
            containerFactory = "feesListenerContainerFactory")
    public boolean completed(FeeRequest request) {
        log.info("Fee: " + request.toString() + " Completed successfully");
        if(feesState.containsKey(request.getTransactionUuid())) {
            // Here put your Business Logic to confirm the Fees
            feesState.put(request.getTransactionUuid(), SagaState.Completed);
        } else {
            log.error("Fee: " + request + " Not found");
        }
        return true;
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.fees.failed.name}", 
            containerFactory = "feesListenerContainerFactory")
    public boolean failed(FeeRequest request) {
        log.error("Fee: " + request.toString() + " Failed");
        if(feesState.containsKey(request.getTransactionUuid())) {
            // Here put your Business Logic to rollback the Fees
            feesState.put(request.getTransactionUuid(), SagaState.Failed);
        } else {
            log.error("Fee: " + request + " Not found");
        }
        return true;
    }
}
