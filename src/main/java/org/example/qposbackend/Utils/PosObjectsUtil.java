package org.example.qposbackend.Utils;

import jakarta.validation.constraints.NotNull;

public class PosObjectsUtil {
  public static String firstNonNullString(String... strings) {
    for (var string : strings) {
      if (string != null && !string.isBlank()) {
        return string;
      }
    }
    return "";
  }

  @NotNull
  @SafeVarargs
  public static <T> T firstNonNull(T... objects) {
    for (var object : objects) {
      if (object != null) {
        return object;
      }
    }
    return null;
  }
}
