package uk.gov.justice.laa.bulkclaim.e2e.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SqlStatement(String sql, List<Object> parameters) {

  public Map<Integer, Object> getParameters() {
    Map<Integer, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < parameters.size(); i++) {
      map.put(i + 1, parameters.get(i));
    }
    return map;
  }

  public static SqlStatement fromRaw(String sql) {
    return new SqlStatement(sql, List.of());
  }
}
