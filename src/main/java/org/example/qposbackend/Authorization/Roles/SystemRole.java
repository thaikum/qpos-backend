package org.example.qposbackend.Authorization.Roles;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.Privilege;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemRole {
    @Id
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Privilege> privileges;
}
