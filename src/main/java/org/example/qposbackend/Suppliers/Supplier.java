package org.example.qposbackend.Suppliers;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(unique=true)
    private String phoneNumber;
    private String contactPersonName;
    private String country;
    private String city;
    private String address;
    private String description;
    @Column(unique=true)
    private String email;
}
