package com.template.webserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

import static org.springframework.boot.WebApplicationType.SERVLET;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
public class Starter {
    /**
     * Starts our Spring Boot application.
     */
    @Configuration
    class MvcConf extends WebMvcConfigurationSupport {
        protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(converter());
            addDefaultHttpMessageConverters(converters);
        }

        @Bean
        MappingJackson2HttpMessageConverter converter() {
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            //do your customizations here...
            return converter;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Starter.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(SERVLET);
        app.run(args);
    }
}