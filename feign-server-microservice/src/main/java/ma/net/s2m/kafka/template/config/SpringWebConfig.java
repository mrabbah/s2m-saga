package ma.net.s2m.kafka.template.config;

import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vladkrava.converter.http.AvroJsonHttpMessageConverter;
import com.vladkrava.converter.http.AvroXmlHttpMessageConverter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class for configuring {@link WebMvcConfigurer}
 *
 * @author rabbah
 */
@Slf4j
@Configuration
@EnableWebMvc
@EnableSwagger2
public class SpringWebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        log.info("Registring Avro converters...");
        converters.add(0, new AvroJsonHttpMessageConverter());
        converters.add(1, new AvroXmlHttpMessageConverter());
        converters.add(2, new MappingJackson2HttpMessageConverter());
    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        final RestTemplate restTemplate = builder.build();
        restTemplate.getMessageConverters().add(0, new AvroJsonHttpMessageConverter());
        restTemplate.getMessageConverters().add(1, new AvroXmlHttpMessageConverter());
        restTemplate.getMessageConverters().add(2, new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("Feign Avro Server API",
                "Feign Avro Server API",
                "1.0",
                "Terms of service",
                new springfox.documentation.service.Contact("RABBAH", "", "mrabbah@omnidata.ma"),
                "Copyright S2M 2021", "API LICENCE URL", Collections.emptyList());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**").addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
