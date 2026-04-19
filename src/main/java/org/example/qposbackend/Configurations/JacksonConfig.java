package org.example.qposbackend.Configurations;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  /**
   * Ensure {@link JavaTimeModule} is on the same ObjectMapper used for {@code @RequestBody} JSON.
   * A standalone {@code @Bean} {@link com.fasterxml.jackson.databind.Module} is not always wired
   * into {@link org.springframework.http.converter.json.MappingJackson2HttpMessageConverter}; the
   * builder customizer is.
   *
   * <p>Also register {@link Hibernate6Module} for Hibernate proxies in API responses. Do not use
   * {@code builder.modules(...)} with only Hibernate — that can replace the module set and drop
   * JSR-310; {@code modulesToInstall(Class...)} adds modules without that pitfall.
   */
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonModulesCustomizer() {
    return builder ->
        builder.modulesToInstall(JavaTimeModule.class, Hibernate6Module.class);
  }
}
