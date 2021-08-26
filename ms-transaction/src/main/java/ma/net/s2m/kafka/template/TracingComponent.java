package ma.net.s2m.kafka.template;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 *
 * @author rabbah
 */
@Component
@Slf4j
public class TracingComponent implements CommandLineRunner {

    @Value("${jeagerservicename}")
    private String jeagerServiceName;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing tracing...");
        
        if(jeagerServiceName != null) {
            Tracer tracer = Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);
        }
        
        log.info("Tracing class name = %s", TracingProducerInterceptor.class.getName());
    }
    
}
