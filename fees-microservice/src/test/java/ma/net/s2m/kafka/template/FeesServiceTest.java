package ma.net.s2m.kafka.template;

import lombok.extern.slf4j.Slf4j;
import ma.net.s2m.kafka.template.clients.FeesCurrencyConverterClient;
import ma.net.s2m.kafka.template.example.dto.FeeResponse;
import ma.net.s2m.kafka.template.service.FeesService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author rabbah
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FeesServiceTest {
    
    @Autowired
    FeesCurrencyConverterClient feesCurrencyConverterClient;
    
    @Ignore
    @Test
    public void testConvertionFeignAvro() {
        FeeResponse feeMAD = new FeeResponse(1l, "TTTTTTT", 89d);
        FeeResponse fee = feesCurrencyConverterClient.convert(feeMAD);
        log.info("fee response : " + fee.toString());
        Assert.assertEquals(10d, fee.getFees(), 0d);
    }
    
}
