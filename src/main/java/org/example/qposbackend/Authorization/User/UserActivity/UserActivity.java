package org.example.qposbackend.Authorization.User.UserActivity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.Authorization.User.userShop.UserShop;
import org.springframework.data.annotation.CreatedBy;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @Deprecated(forRemoval = true, since = "V2-Multishop System roles will shift to usershop instead")
  private User user;

  @ManyToOne @JoinColumn @JsonIgnore private UserShop userShop;
  @Builder.Default private Date timeIn = new Date();
  private Date timeOut;
}
