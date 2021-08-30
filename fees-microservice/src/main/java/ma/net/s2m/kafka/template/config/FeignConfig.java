package ma.net.s2m.kafka.template.config;

import com.vladkrava.converter.http.AvroJsonHttpMessageConverter;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 *
 * @author rabbah
 */
@Configuration
public class FeignConfig {
    
    @Bean
    public Decoder avroDecoder() {
        HttpMessageConverter avroConverter = new AvroJsonHttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(avroConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    @Bean
    Encoder avroEncoder() {
        HttpMessageConverter avroConverter = new AvroJsonHttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(avroConverter);
        return new SpringEncoder(objectFactory);
    }
   
}
