package com.junmoyu.basic.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Json 操作工具类
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 获取 ObjectMapper 对象
     *
     * @return 完成配置的 ObjectMapper 对象
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 要转换的对象
     * @return 转换后的 JSON 字符串，如果转换失败，则返回空对象的 JSON 字符串
     */
    public static String toJson(final Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            log.error("converting Java object to Json string exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象
     *
     * @param value 要转换的 JSON 字符串
     * @param clz   目标 Java 对象的 Class 类型
     * @param <T>   目标 Java 对象的泛型类型
     * @return 转换后的 Java 对象，如果转换失败，则返回 null
     */
    public static <T> T toObject(String value, Class<T> clz) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, clz);
        } catch (JsonProcessingException e) {
            log.error("converting Json string to Java object exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象
     *
     * @param value         要转换的 JSON 字符串
     * @param typeReference 目标 Java 对象的 TypeReference，用于泛型类型的转换
     * @param <T>           目标 Java 对象的泛型类型
     * @return 转换后的 Java 对象，如果转换失败或输入为空，则返回 null
     */
    public static <T> T toObject(String value, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(value) || typeReference == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, typeReference);
        } catch (Exception e) {
            log.error("converting Json string to Java object exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象列表
     *
     * @param value 要转换的 JSON 字符串
     * @param clz   目标 Java 对象的 Class 类型，用于指定列表中的元素类型
     * @param <T>   目标 Java 对象的泛型类型
     * @return 转换后的 Java 对象列表，如果转换失败或输入为空，则返回 null
     */
    public static <T> List<T> toArray(String value, Class<T> clz) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(Collection.class, clz);
        try {
            return OBJECT_MAPPER.readValue(value, javaType);
        } catch (JsonProcessingException e) {
            log.error("converting Json string to Java array exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象列表
     *
     * @param values 要转换的 JSON 字符串列表
     * @param clz    目标 Java 对象的 Class 类型，用于指定列表中的元素类型
     * @param <T>    目标 Java 对象的泛型类型
     * @return 转换后的 Java 对象列表，如果转换失败或输入为空，则返回 null
     */
    public static <T> List<T> toArray(List<String> values, Class<T> clz) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        try {
            List<T> result = new ArrayList<>(values.size());
            for (String value : values) {
                T obj = OBJECT_MAPPER.readValue(value, clz);
                result.add(obj);
            }
            return result;
        } catch (Exception e) {
            log.error("converting Json string to Java array exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象列表
     *
     * @param values 要转换的 JSON 字符串列表
     * @param clz    目标 Java 对象的 Class 类型，用于指定列表中的元素类型
     * @param <T>    目标 Java 对象的泛型类型
     * @return 转换后的 Java 对象列表，如果转换失败或输入为空，则返回 null
     */
    public static <T> Set<T> toArray(Set<String> values, Class<T> clz) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptySet();
        }
        try {
            Set<T> result = new HashSet<>(values.size());
            for (String value : values) {
                T obj = OBJECT_MAPPER.readValue(value, clz);
                result.add(obj);
            }
            return result;
        } catch (Exception e) {
            log.error("converting Json string to Java array exception.", e);
            throw new RuntimeException("Json exception");
        }
    }

    /**
     * 创建 ObjectMapper 并完成配置
     *
     * @return 完成配置的 ObjectMapper 对象
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // 反序列化时遇到不匹配的属性，不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时遇到空对象，不抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化的时候如果是无效子类型,不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime 系列序列化和反序列化模块，继承自jsr310，这里修改了日期格式
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        // Date 类型的序列化
        javaTimeModule.addSerializer(Date.class, new JsonSerializer<Date>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN));
            }
        });

        // Date 类型的反序列化
        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return DateUtil.parse(jsonParser.getText()).toJdkDate();
            }
        });

        // Long 类型的数据序列化为 String, 因为 JS 直接接收 Long 类型会有精度问题.可参考《Java开发手册》前后端规约部分说明.
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);

        objectMapper.registerModule(javaTimeModule);
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}