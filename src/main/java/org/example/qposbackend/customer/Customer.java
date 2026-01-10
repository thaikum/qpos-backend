package org.example.qposbackend.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.qposbackend.shop.Shop;

@Data
@Entity
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "First name cannot be empty!")
  @Pattern(regexp = "^[A-Za-z']+$", message = "Name can only contain alphabets and apostrophe!")
  private String firstName;

  @NotNull(message = "Last name cannot be empty!")
  @Pattern(regexp = "^[A-Za-z']+$", message = "Name can only contain alphabets and apostrophe!")
  private String lastName;

  @NotNull(message = "Customer phone cannot be empty!")
  @Column(length = 20)
  @Pattern(regexp = "^\\+?\\d{10,}$", message = "Phone number not valid, please check again.")
  private String phoneNumber;

  @Column(length = 50)
  @Email(message = "Email not valid")
  private String email;

  private String idNumber;

  @JsonIgnore
  @JoinColumn(nullable = false)
  @ManyToOne(cascade = CascadeType.MERGE)
  private Shop shop;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private String fullName;

  @Transient private Double receivables;
  @Transient Double payables;

  public String getFullName() {
    return String.format("%s %s", firstName, lastName);
  }
}
