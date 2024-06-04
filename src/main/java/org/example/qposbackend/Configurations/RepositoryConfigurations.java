package org.example.qposbackend.Configurations;

import org.example.qposbackend.Accounting.Accounts.AccountEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfigurations {
    public RepositoryConfigurations(){
        super();
    }

    @Bean
    AccountEventHandler accountEventHandler(){
        return new AccountEventHandler();
    }
}
