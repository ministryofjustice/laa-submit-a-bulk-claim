package uk.gov.justice.laa.bulkclaim.e2e.utils;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Minimal Base32 + TOTP helper for MFA automation. */
public final class TotpUtil {

  private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

  private TotpUtil() {}

  public static String generateCode(String base32Secret) {
    return generateCode(base32Secret, Instant.now().getEpochSecond(), 30, 6);
  }

  static String generateCode(String base32Secret, long epochSeconds, int periodSeconds, int digits) {
    try {
      byte[] key = decodeBase32(base32Secret);
      long counter = epochSeconds / periodSeconds;
      byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] hash = mac.doFinal(data);

      int offset = hash[hash.length - 1] & 0x0f;
      int binary = ((hash[offset] & 0x7f) << 24)
          | ((hash[offset + 1] & 0xff) << 16)
          | ((hash[offset + 2] & 0xff) << 8)
          | (hash[offset + 3] & 0xff);

      int otp = binary % (int) Math.pow(10, digits);
      return String.format(Locale.ROOT, "%0" + digits + "d", otp);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to generate TOTP code", e);
    }
  }

  private static byte[] decodeBase32(String input) {
    String normalized = input.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
    int buffer = 0;
    int bitsLeft = 0;
    byte[] out = new byte[(normalized.length() * 5) / 8];
    int index = 0;

    for (int i = 0; i < normalized.length(); i++) {
      int val = BASE32_ALPHABET.indexOf(normalized.charAt(i));
      if (val < 0) {
        throw new IllegalArgumentException("Invalid base32 character in MFA secret");
      }
      buffer = (buffer << 5) | val;
      bitsLeft += 5;
      if (bitsLeft >= 8) {
        out[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
        bitsLeft -= 8;
      }
    }

    if (index == out.length) {
      return out;
    }

    byte[] trimmed = new byte[index];
    System.arraycopy(out, 0, trimmed, 0, index);
    return trimmed;
  }
}

