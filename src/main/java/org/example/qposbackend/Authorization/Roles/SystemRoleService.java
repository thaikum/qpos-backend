package org.example.qposbackend.Authorization.Roles;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.Privilege;
import org.example.qposbackend.Authorization.Privileges.PrivilegeRepository;
import org.example.qposbackend.Authorization.Privileges.PrivilegesEnum;
import org.example.qposbackend.DTOs.PrivilegeDTO;
import org.example.qposbackend.DTOs.RoleDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemRoleService {
  private final SystemRoleRepository systemRoleRepository;
  private final PrivilegeRepository privilegeRepository;

  @Bean
  @DependsOn("privilegesInitializer")
  private void initializeRoles() {
    Optional<SystemRole> optionalSystemRole = systemRoleRepository.findById("ADMIN");
    Optional<SystemRole> optionalOwnerRole = systemRoleRepository.findById("OWNER");
    SystemRole systemRole, ownerRole;

    systemRole = optionalSystemRole.orElseGet(() -> SystemRole.builder().name("ADMIN").build());
    ownerRole = optionalOwnerRole.orElseGet(() -> SystemRole.builder().name("ADMIN").build());

    Set<Privilege> privilegeSet = new HashSet<>(privilegeRepository.findAll());

    systemRole.setPrivileges(privilegeSet);
    ownerRole.setPrivileges(privilegeSet);

    systemRoleRepository.saveAll(List.of(systemRole, ownerRole)); // initialize roles
  }

  public List<RoleDTO> getRoles() {
    return systemRoleRepository.findAll().stream()
        .map(
            role ->
                new RoleDTO(
                    role.getName(),
                    role.getPrivileges().stream()
                        .map(
                            p -> {
                              PrivilegesEnum privilege = PrivilegesEnum.valueOf(p.getPrivilege());
                              return new PrivilegeDTO(
                                  privilege.name(),
                                  privilege.getDisplayName(),
                                  privilege.getCategory());
                            })
                        .collect(Collectors.toList())))
        .collect(Collectors.toList());
  }
}
