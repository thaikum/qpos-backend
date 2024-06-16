package org.example.qposbackend.Configurations.AdminParameters;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminParametersService {
    private final AdminParametersRepository adminParametersRepository;

    @Bean
    private void initializeAdminParameters(){
        if(adminParametersRepository.count() == 0){
            AdminParameters adminParameters = new AdminParameters();
            adminParametersRepository.save(adminParameters);
        }
    }
}
