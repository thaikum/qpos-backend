package org.example.qposbackend.Utils;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumUtils {
  public static <E extends Enum<E>> List<Map<String, Object>> toEnumList(Class<E> enumClass) {
    return Arrays.stream(enumClass.getEnumConstants())
        .map(
            enumValue -> {
              Map<String, Object> mappedEnum =
                  Arrays.stream(enumClass.getDeclaredFields())
                      .filter(
                          field ->
                              !field.isEnumConstant()
                                  && !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                      .collect(
                          Collectors.toMap(
                              Field::getName,
                              field -> {
                                try {
                                  field.setAccessible(true);
                                  return field.get(enumValue);
                                } catch (IllegalAccessException e) {
                                  throw new RuntimeException(
                                      "Error accessing field: " + field.getName(), e);
                                }
                              }));
              mappedEnum.put("name", enumValue.name()); // Always include enum name
              return mappedEnum;
            })
        .collect(Collectors.toList());
  }
}
