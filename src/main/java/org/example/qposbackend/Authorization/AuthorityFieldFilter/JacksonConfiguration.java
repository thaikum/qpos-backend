package org.example.qposbackend.Authorization.AuthorityFieldFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;

//@Configuration
public class JacksonConfiguration {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Object.class, new AuthorityBasedFieldSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
