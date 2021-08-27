package ma.net.s2m.kafka.template.controller;

import static com.vladkrava.converter.http.AbstractAvroHttpMessageConverter.AVRO_JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;
import ma.net.s2m.kafka.template.service.SagaOrchestratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@Slf4j
@RestController
@RequestMapping("/saga")
@Api(value = "Saga EndPoints",
        tags = "Saga controller")
public class SagaController {

    @Autowired
    SagaOrchestratorService sagaOrchestratorService;

    

    @ApiOperation(produces = AVRO_JSON, consumes = AVRO_JSON, value = "Revert transaction", httpMethod = "PUT", notes = "<br>This service handle transaction", response = TransactionResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = TransactionResponse.class, message = "Successful operation")})
    @PutMapping(value = "/revert", produces = AVRO_JSON, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionResponse> handel(@RequestBody final TransactionRequest transaction) {
        log.info("transaction input : " + transaction.toString());
        // TransactionResponse tx = transactionService.init(transaction.getOrigin(), transaction.getAmount());
        // log.info("transaction result : " + tx.toString());
        // return Mono.justOrEmpty(tx);
        return Mono.empty();
    }

    @ResponseStatus(
            value = HttpStatus.GATEWAY_TIMEOUT,
            reason = "Request take too mutch time")
    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgumentHandler() {}
}
