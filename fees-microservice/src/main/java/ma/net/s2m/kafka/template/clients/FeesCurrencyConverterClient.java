package ma.net.s2m.kafka.template.clients;

import static com.vladkrava.converter.http.AbstractAvroHttpMessageConverter.AVRO_JSON;
import ma.net.s2m.kafka.template.config.FeignConfig;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

/**
 *
 * @author rabbah
 */
@FeignClient(name = "${feign.name}", 
        url = "${feign.url}", 
        path = "${feign.path}",
        configuration = FeignConfig.class)
public interface FeesCurrencyConverterClient {
    
    // @Headers("Content-Type: application/x-www-form-urlencoded")
    @RequestMapping(method = RequestMethod.PUT, value = "/convert", produces = AVRO_JSON, consumes = AVRO_JSON)
    FeeResponse convert(FeeResponse feesMAD); 
}
