package org.example.qposbackend.Authorization.User.userShop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.Roles.SystemRole;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.shop.Shop;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShop {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne()
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne()
  @JoinColumn(name = "shop_id", nullable = false)
  private Shop shop;

  @ManyToOne
  @JoinColumn(nullable = false)
  private SystemRole role;

  @Column(nullable = false)
  private boolean isDefault = false;

  private Date createdAt = new Date();
  private Date updatedAt;

  private boolean isDeleted = false;
  private boolean isActive = true;

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = new Date();
  }
}
