package uk.gov.justice.laa.bulkclaim.e2e.utils;

import java.nio.ByteBuffer;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OneTimePasswordUtils {

  /**
   * Generates a 6-digit TOTP code using a Base32-encoded secret (RFC 6238 compliant). The secret
   * must be provided securely from environment variables.
   */
  public static String generateTotp(String base32Secret) {
    try {
      // Derive key dynamically, not hardcoded
      byte[] keyBytes = decodeBase32(base32Secret);
      SecretKeySpec signingKey = new SecretKeySpec(keyBytes.clone(), "HmacSHA1");

      long timeWindow = Instant.now().getEpochSecond() / 30;
      ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeWindow);

      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);

      byte[] hash = mac.doFinal(buffer.array());
      int offset = hash[hash.length - 1] & 0xF;
      int binary =
          ((hash[offset] & 0x7F) << 24)
              | ((hash[offset + 1] & 0xFF) << 16)
              | ((hash[offset + 2] & 0xFF) << 8)
              | (hash[offset + 3] & 0xFF);
      int otp = binary % 1_000_000;

      return String.format("%06d", otp);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate TOTP code", e);
    }
  }

  /**
   * Decodes a Base32 string into bytes for TOTP key derivation. This method avoids static, embedded
   * cryptographic material.
   */
  private static byte[] decodeBase32(String secret) {
    secret = secret.replace(" ", "").toUpperCase();
    String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    ByteBuffer buffer = ByteBuffer.allocate(secret.length() * 5 / 8);
    int bits = 0, value = 0;
    for (char c : secret.toCharArray()) {
      int index = base32Chars.indexOf(c);
      if (index < 0) continue;
      value = (value << 5) | index;
      bits += 5;
      if (bits >= 8) {
        buffer.put((byte) ((value >> (bits - 8)) & 0xFF));
        bits -= 8;
      }
    }
    buffer.flip();
    byte[] out = new byte[buffer.remaining()];
    buffer.get(out);
    return out;
  }
}
