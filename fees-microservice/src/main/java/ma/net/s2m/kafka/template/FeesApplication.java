package ma.net.s2m.kafka.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FeesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeesApplication.class, args);
    }

}
