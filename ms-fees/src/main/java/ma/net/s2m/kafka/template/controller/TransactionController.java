package ma.net.s2m.kafka.template.controller;

import static com.vladkrava.converter.http.AbstractAvroHttpMessageConverter.AVRO_JSON;
import ma.net.s2m.kafka.template.example.dto.Transaction;
import ma.net.s2m.kafka.template.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 *
 * @author rabbah
 */
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    
    @Autowired
    TransactionService transactionService;
    
    @PostMapping(value = "/init", produces = AVRO_JSON, consumes = AVRO_JSON)
    public ResponseEntity<Transaction> init(@RequestBody final Transaction tx) {
        return ResponseEntity.ok(transactionService.init(tx));
    }
    
    @PostMapping(value = "/init2", produces = AVRO_JSON, consumes = AVRO_JSON)
    public ResponseEntity<Transaction> init(@RequestParam("origin") String origin, @RequestParam("amount") Double amount) {
        return ResponseEntity.ok(transactionService.init(origin, amount));
    }

    
}
