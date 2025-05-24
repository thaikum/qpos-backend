package org.example.qposbackend.Authorization.User.userShop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserShopRepository extends JpaRepository<UserShop, Long> {
    Optional<UserShop> findUserShopByShop_CodeAndUser_email(String shopCode, String email);
}
