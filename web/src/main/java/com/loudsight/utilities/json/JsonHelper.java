package com.loudsight.utilities.json;

import com.loudsight.meta.Meta;
import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.useful.helper.JvmClassHelper;
import com.loudsight.meta.EntityHelper;
import com.loudsight.meta.MetaRepository;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.json.*;

public class JsonHelper {

    public static class TypeToken<T> {
        public static <T> TypeToken<T> of() {
            return new TypeToken<>();
        };

        Class<?> getGenericType() {
            return null;
        };
    }
    
    private static final MetaRepository metaRepository = MetaRepository.getInstance();

    public static Map<String, Object> toMap(String jsonStr) {
        return Collections.emptyMap();
    }


    public static <T> T fromJson(String jsonStr, Class<T> aClazz) {
        try (var reader = Json.createReader(new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8)))) {
            JsonStructure jsonStructure = reader.read();

            if (String.class == aClazz) {
                if (jsonStructure.getValueType() == JsonValue.ValueType.OBJECT) {
                    return (T)jsonStructure.asJsonObject().getString("value");
                }
            }
            var meta = metaRepository.<T>getMeta(aClazz);
            var result = meta.newInstance();

            if (jsonStructure.getValueType() == JsonValue.ValueType.OBJECT) {
                jsonStructure.asJsonObject().forEach((fieldName, v) -> {
                    var field = meta.getFieldByName(fieldName);
                    if (v != null && !"null".equals(v.toString())) {
                        field.set(result, EntityHelper.convert(getValue(v), field.typeClass()));
                    }
                });
            }
            return result;
        }
    }

    private static <T> T xddfff(JsonObject jsonObject, Class<T> aClazz) {
        var meta = metaRepository.getMeta(aClazz);
        var result = meta.newInstance();
        jsonObject.forEach((fieldName, v) -> {
            var field = meta.getFieldByName(fieldName);
            field.set(result, EntityHelper.convert(getValue(v), field.typeClass()));
        });

        return result;
    }

    public static <T> Collection<T> fromJson(String jsonStr, Class<T> entityType, TypeToken<? extends Collection<T>> collectionClass) {
        List<T> results = new ArrayList<>();

        try (var reader = Json.createReader(new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8)))) {
            JsonStructure jsonStructure = reader.read();
            if (jsonStructure.getValueType() == JsonValue.ValueType.ARRAY) {
                jsonStructure.asJsonArray().forEach(e -> {

                    var result = xddfff(e.asJsonObject(), entityType);
                    results.add(result);
                });
            }
        }

        return results;
    }

    public static String toJson(Collection<?> entities) {
        return "[" + entities.stream().map(JsonHelper::toJson)
                .collect(Collectors.joining(",")) + "]";
    }

    public static String toJson(Object entity) {
        if (entity instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return "[]";
            } else {
                return String.format(
                        "[%s]",
                        collection.stream()
                                .map(it -> {
                                    var meta = metaRepository.<Object>getMeta((Class)it.getClass());

                                    return toJson(it, meta);
                                })
                                .collect(Collectors.joining(","))
                );
            }
        }
        var meta = metaRepository.<Object>getMeta((Class)entity.getClass());

        return toJson(entity, meta);
    }
    private static <T> String toJson(T entity, Meta<T> meta) {
        var jsonObjectBuilder = Json.createObjectBuilder();
        toJson(jsonObjectBuilder, entity, meta);
        return jsonObjectBuilder.build().toString();
    }

    private static <T> void toJson(JsonObjectBuilder jsonObjectBuilder, T entity, Meta<T> meta) {
        Optional.ofNullable(meta).orElseThrow().getFields().forEach(field -> {
            var value = field.get(entity);
            addValue(jsonObjectBuilder, field.name(), value);
        });
    }

    private static void addElement(JsonArrayBuilder jsonArrayBuilder, Object value) {
        if (value instanceof Integer intValue) {
            jsonArrayBuilder.add(intValue);
        } else if (value instanceof Long longValue) {
            jsonArrayBuilder.add(longValue);
        } else if (value instanceof Boolean booleanValue) {
            jsonArrayBuilder.add(booleanValue);
        } else if (value instanceof Double doubleValue) {
            jsonArrayBuilder.add(doubleValue);
        } else if (value instanceof String strValue) {
            jsonArrayBuilder.add(strValue);
        } else if (Enum.class.isAssignableFrom(value.getClass())) {
            jsonArrayBuilder.add(value.toString());
        } else if (value instanceof Collection<?> collection) {
        }
    }

    private static void addValue(JsonObjectBuilder jsonObjectBuilder, String name, Object value) {
        if (value instanceof Integer intValue) {
            jsonObjectBuilder.add(name, intValue);
        } else if (value instanceof Long longValue) {
            jsonObjectBuilder.add(name, longValue);
        } else if (value instanceof Boolean booleanValue) {
            jsonObjectBuilder.add(name, booleanValue);
        } else if (value instanceof Double doubleValue) {
            jsonObjectBuilder.add(name, doubleValue);
        } else if (value instanceof String strValue) {
            jsonObjectBuilder.add(name, strValue);
        } else if (Enum.class.isAssignableFrom(value.getClass())) {
            jsonObjectBuilder.add(name, value.toString());
        } else if (value instanceof Collection<?> collection) {
                var jsonArrayBuilder = Json.createArrayBuilder();
                collection.forEach(element -> {
                    Object x = element;
                    if (element instanceof Enum e) {
                        x = e.name();
                    }

                    if (JvmClassHelper.isPrimitive(x.getClass())) {
                        addElement(jsonArrayBuilder, x);
                    } else {
                        var elementMeta = metaRepository.getMeta(element.getClass());
//                        var elementMeta = metaRepository.getMeta(element.getClass());
                        var jsonArrayObjectBuilder = Json.createObjectBuilder();
                        Optional.ofNullable(elementMeta).orElseThrow().getFields().forEach(field -> {
                            var elementFieldValue = field.get(ClassHelper.uncheckedCast(element));
                            addValue(jsonArrayObjectBuilder, field.name(), elementFieldValue);
                            jsonArrayBuilder.add(jsonArrayObjectBuilder);
                        });
                    }
                });
            jsonObjectBuilder.add(name, jsonArrayBuilder);
        } else {
            var jsonChildObjectBuilder = Json.createObjectBuilder();
            var elementMeta = metaRepository.<Object>getMeta((Class)value.getClass());
            toJson(jsonChildObjectBuilder, value, elementMeta);
        }
    }

    private static Object getValue(JsonValue value) {
        return switch (value.getValueType()) {
            case STRING -> ((JsonString)value).getString();
            case NUMBER -> getNumber((JsonNumber)value);
            case FALSE, TRUE -> value.getValueType() == JsonValue.ValueType.TRUE;
            default -> throw new RuntimeException("Unsupported Json Value Type");
        };
    }

    private static Number getNumber(JsonNumber number) {
        if (number.isIntegral()) {
            return number.intValueExact();
        } else {
            return number.numberValue();
        }
    }

//    @Bean
//    public Gson gson() {
//
//        return new GsonBuilder()
//                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (jsonElement, type, jsonDeserializationContext) -> {
//                    Instant instant = Instant.parse(jsonElement.getAsString());
//
//                    return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
//                })
//                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<Object>) (o, type, jsonSerializationContext) -> {
//                    LocalDateTime localDateTime = (LocalDateTime) o;
//                    Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
//                    return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(instant));
//                })
//                .create();
//    }

}
