package org.example.qposbackend.EOD;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.AuthUtils.AuthUserShopProvider;
import org.example.qposbackend.shop.Shop;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.example.qposbackend.constants.Constants.TIME_ZONE;
import static org.example.qposbackend.constants.Constants.YYYY_MM_DD;

@Service
@RequiredArgsConstructor
public class EODDateService {
  private final AuthUserShopProvider authProvider;
  private final EODRepository eodRepository;

  public LocalDate getSystemDateOrThrowIfEodNotDone() {
    Shop shop = authProvider.getCurrentShop();
    LocalDate localDate = getCurrentSystemDate(shop);

    if (localDate.equals(LocalDate.now(ZoneId.of(TIME_ZONE)))) {
      return localDate;
    } else {
      throw new RuntimeException(
          String.format(
              "Your system date %s does not match the actual date. Perform EOD to proceed!",
              YYYY_MM_DD.format(localDate)));
    }
  }

  public LocalDate getCurrentSystemDate(Shop shop) {
    Optional<EOD> eodOptional = eodRepository.findLastEODAndShop(shop.getId());

    if (eodOptional.isPresent()) {
      EOD eod = eodOptional.get();
      return eod.getDate().plusDays(1);
    } else {
      return LocalDate.now();
    }
  }
}
