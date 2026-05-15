package uk.gov.justice.laa.bulkclaim.dto.sorting;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@EqualsAndHashCode
public abstract class Sort<T extends SortField> {

  protected T field;
  protected SortDirection direction;

  @Override
  public String toString() {
    return direction.getValue() != null
        ? String.format("%s,%s", field.getValue(), direction.getValue())
        : null;
  }

  public Sort(String sortString) {
    Objects.requireNonNull(sortString);

    Pattern pattern = Pattern.compile("^(\\w+),(\\w+)$");
    Matcher matcher = pattern.matcher(sortString);
    if (matcher.matches()) {
      this.field = createField(matcher.group(1));
      this.direction = SortDirection.fromValue(matcher.group(2));
    } else {
      throw new IllegalArgumentException("Could not parse sort string: " + sortString);
    }
  }

  public abstract T createField(String value);
}
