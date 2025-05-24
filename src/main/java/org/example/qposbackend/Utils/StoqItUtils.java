package org.example.qposbackend.Utils;

import org.apache.commons.codec.binary.Base32;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class StoqItUtils {
  public static String stringPadding(String input, int targetLength) {
    SecureRandom random = new SecureRandom();
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder(input);

    while (sb.length() < targetLength) {
      sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return sb.length() > targetLength ? sb.substring(0, targetLength) : sb.toString();
  }

  public static String generateStringFromLong(Long number, int length) {
    Base32 base32 = new Base32();
    byte[] idBuffer = ByteBuffer.allocate(Long.BYTES).putLong(number).array();
    String encodedId = base32.encodeAsString(idBuffer);
    String paddedString = stringPadding(encodedId, length);
    return encodedId + generateCheckSum(encodedId);
  }

  public static boolean checkLuhn(String cardNo) {
    int nDigits = cardNo.length();

    int nSum = 0;
    boolean isSecond = false;
    for (int i = nDigits - 1; i >= 0; i--) {
      int d = cardNo.charAt(i) - '0';

      if (isSecond) d = d * 2;

      nSum += d / 10;
      nSum += d % 10;

      isSecond = !isSecond;
    }
    return (nSum % 10 == 0);
  }

  public static char generateCheckSum(String str) {
    str = str + "0";
    int nSum = 0;
    boolean isSecond = false;

    for (int i = str.length() - 1; i >= 0; i--) {
      int d = str.charAt(i) - '0';

      if (isSecond) d = d * 2;

      nSum += d / 10;
      nSum += d % 10;

      isSecond = !isSecond;
    }

    int checkSum = (10 - (nSum % 10)) % 10;
    return Character.forDigit(checkSum, 10);
  }
}
