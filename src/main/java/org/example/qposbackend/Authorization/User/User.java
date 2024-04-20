package org.example.qposbackend.Authorization.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.User.Password.Password;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20)
    private String firstName;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(length = 20)
    private String lastName;
    private IdType idType;
    @Column(length = 50)
    private String idNumber;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    @JsonIgnore
    private List<Password> passwords;
    @Builder.Default
    private Boolean enabled = true;
    @Builder.Default
    private Boolean isLoggedIn = false;
    @ManyToOne
    @JoinColumn
    private SystemRole role;
}
