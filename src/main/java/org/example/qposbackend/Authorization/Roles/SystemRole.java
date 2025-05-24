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

            clone.setName(this.name);

            Set<Privilege> copiedPrivileges = new HashSet<>();
            for (Privilege privilege : this.privileges) {
                copiedPrivileges.add(new Privilege(privilege.getPrivilege()));
            }
            clone.setPrivileges(copiedPrivileges);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
