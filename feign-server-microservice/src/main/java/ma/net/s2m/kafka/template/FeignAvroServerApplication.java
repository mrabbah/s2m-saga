package ma.net.s2m.kafka.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FeignAvroServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignAvroServerApplication.class, args);
    }

}
