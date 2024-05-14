package org.example.qposbackend.Authorization.Privileges;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivilegesService {
    private final PrivilegeRepository privilegeRepository;

    @Bean("privilegesInitializer")
    private void initializePrivileges() {
        Set<Privilege> newPrivileges = new HashSet<>();
        Set<String> oldPrivileges = privilegeRepository.findAll().stream()
                .map(Privilege::getPrivilege).collect(Collectors.toSet());

        for (PrivilegesEnum privilegesEnum : PrivilegesEnum.values()) {
            if (!oldPrivileges.contains(privilegesEnum.name())) {
                System.out.println("Privilege was: "+privilegesEnum.name());
                newPrivileges.add(Privilege.builder().privilege(privilegesEnum.name()).build());
            }
        }
        log.info("Privileges are: {}", newPrivileges);
        privilegeRepository.saveAll(newPrivileges);
    }
}
