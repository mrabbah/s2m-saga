package ma.net.s2m.kafka.template.controller;

import static com.vladkrava.converter.http.AbstractAvroHttpMessageConverter.AVRO_JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import ma.net.s2m.kafka.template.example.dto.TransactionRequest;
import ma.net.s2m.kafka.template.example.dto.TransactionResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Api(value = "Feign Avro EndPoints",
        tags = "Feign Avro controller")
public class FeignServerController {

    @ApiOperation(produces = AVRO_JSON, consumes = AVRO_JSON, value = "Currency convertion", httpMethod = "PUT", notes = "<br>This service convert currency from MAD to USD", response = FeeResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = FeeResponse.class, message = "Successful operation")})
    @PutMapping(value = "/convert", produces = AVRO_JSON, consumes = AVRO_JSON)
    public FeeResponse convert(@RequestBody final FeeResponse fees) {
        
        log.info("Fees input : " + fees.toString());
        
        FeeResponse response = FeeResponse.newBuilder()
                .setId(fees.getId())
                .setTransactionUuid(fees.getTransactionUuid())
                .setFees(fees.getFees() / 8.9).build();
        
        return response;
        
    }

}
