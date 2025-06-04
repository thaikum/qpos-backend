package org.example.qposbackend.shop;

import jakarta.persistence.*;
import java.util.Date;

import lombok.*;
import org.example.qposbackend.Utils.StoqItUtils;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Setter(AccessLevel.NONE)
  @Column(unique = true)
  private String code;

  private String phone;
  private String email;

  private String address;
  private String location;

  private boolean active = true;
  private boolean deleted = false;

  private String currency;

  private Date createdAt;
  private Date updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = new Date();
    this.updatedAt = new Date();
  }

  @PostPersist
  protected void createId() {
    this.code = StoqItUtils.generateStringFromLong(this.id, 9);
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = new Date();
  }
}
