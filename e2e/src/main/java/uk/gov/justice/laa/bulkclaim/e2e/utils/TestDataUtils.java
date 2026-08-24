package uk.gov.justice.laa.bulkclaim.e2e.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class TestDataUtils {

  public static String readClasspathResource(String resourcePath) {
    var is = TestDataUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    if (is == null) {
      throw new IllegalArgumentException("Fixture not found on classpath: " + resourcePath);
    }
    try (var br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      var sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString().trim();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read fixture: " + resourcePath, e);
    }
  }
}
