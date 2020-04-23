package io.rebble.asr.cfg;

import java.nio.charset.*;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.util.*;
import org.springframework.web.filter.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class Config implements WebMvcConfigurer {

    public static final String MY_BOUNDARY = "--Nuance_NMSP_vutc5w1XobDdefsYG3wq";

    @Bean
    public HttpMessageConverter<MultiValueMap<String, ?>> createCustomerFormHttpMessageConverter() {
        FormHttpMessageConverter converter = new CustomerFormHttpMessageConverter();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        converter.setPartConverters(converters);
        return converter;
    }

    static class CustomerFormHttpMessageConverter extends FormHttpMessageConverter {

        @Override
        protected byte[] generateMultipartBoundary() {
            return MY_BOUNDARY.getBytes(Charset.forName("UTF-8"));
        }
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(100000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
        configurer.ignoreAcceptHeader(true);
        configurer.defaultContentType(MediaType.MULTIPART_FORM_DATA);
    }
}