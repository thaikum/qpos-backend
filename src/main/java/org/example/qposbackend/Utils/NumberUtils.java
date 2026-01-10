package org.example.qposbackend.Utils;

public class NumberUtils {
  public static Double zeroIfNull(Double number) {
    return number == null ? 0D : number;
  }
}
