package org.example.qposbackend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Authorization.Privileges.Privilege;
import org.example.qposbackend.Authorization.Privileges.PrivilegeRepository;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivilegesService {
    private final PrivilegeRepository privilegeRepository;

    @Bean
    private void initializePrivileges() {
        Set<Privilege> newPrivileges = new HashSet<>();
        Set<String> oldPrivileges = privilegeRepository.findAll().stream()
                .map(privilege -> privilege.getPrivilege().name()).collect(Collectors.toSet());

        for (PrivilegesEnum priv : PrivilegesEnum.values()) {
            if (!oldPrivileges.contains(priv.name())) {
                System.out.println("Privilege was: "+priv.name());
                newPrivileges.add(Privilege.builder().privilege(priv).build());
            }
        }
        log.info("Privileges are: {}", newPrivileges);
        privilegeRepository.saveAll(newPrivileges);
    }
}
