package org.example.qposbackend.Configurations.AdminParameters;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String checkInIp;
    private String checkOutIp;
    @Builder.Default
    private String globalDateFormat = "dd/MM/yyyy";
    @Builder.Default
    private String globalTimeFormat = "hh:mm:ss a";
    @Builder.Default
    private Boolean systemIsUp = true;
    @Builder.Default
    private Integer maximumUserSessions = 5;
    @Builder.Default
    private Integer numberOfPasswordStored = 12;
    private String passwordPolicy;
}
