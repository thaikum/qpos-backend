package org.example.qposbackend.Authorization.User.userShop;

import org.example.qposbackend.Authorization.User.User;
import org.example.qposbackend.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserShopRepository extends JpaRepository<UserShop, Long> {
  @Query(nativeQuery = true, value =
      "select us.* "
          + "from user_shop us "
          + "         join system_user su on su.id = us.user_id "
          + "         join shop s on us.shop_id = s.id "
          + "where su.email = :email "
          + "  and s.code = :shopCode "
          + "limit 1")
  Optional<UserShop> findUserShopByShop_CodeAndUser_email(String shopCode, String email);

  @Query(
      nativeQuery = true,
      value =
          "select us.* from user_shop us join system_user su on su.id = us.user_id where su.email =:email and us.is_default = true limit 1")
  Optional<UserShop> findDefaultUserShopByUser_email(String email);

  Optional<UserShop> findUserShopByUserAndShop(User user, Shop shop);
}
