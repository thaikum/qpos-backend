package org.example.qposbackend.Authorization.Roles;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.Privileges.Privilege;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemRole implements Cloneable{
    @Id
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Privilege> privileges;

    @Override
    public SystemRole clone() {
        try {
            SystemRole clone = (SystemRole) super.clone();

            // Deep copy the name (primitive String)
            clone.setName(this.name);

            // Deep copy the privileges Set (assuming Privilege is not cloneable)
            Set<Privilege> copiedPrivileges = new HashSet<>();
            for (Privilege privilege : this.privileges) {
                copiedPrivileges.add(new Privilege(privilege.getPrivilege())); // Create new Privilege with name
            }
            clone.setPrivileges(copiedPrivileges);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
