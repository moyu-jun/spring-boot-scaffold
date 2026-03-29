package com.junmoyu.basic.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 工具类
 */
public class RedisUtils {

    private final StringRedisTemplate redisTemplate;

    public RedisUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 通用 Key 操作 - 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 通用 Key 操作 - 删除单个 key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 通用 Key 操作 - 批量删除 key
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 通用 Key 操作 - 设置过期时间（单位默认为秒）
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 通用 Key 操作 - 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 通用 Key 操作 - 设置过期时间（Duration 方式）
     */
    public Boolean expire(String key, Duration duration) {
        if (duration == null) {
            return false;
        }
        return redisTemplate.expire(key, duration);
    }

    /**
     * 通用 Key 操作 - 移除过期时间（持久化 key）
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    /**
     * 通用 Key 操作 - 获取 key 剩余过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * String 类型操作 - 设置 key（无过期时间）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value));
    }

    /**
     * String 类型操作 - 设置 key，并指定过期时间（单位默认为秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * String 类型操作 - 设置 key，并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value), timeout, unit);
    }

    /**
     * String 类型操作 - 设置 key，并指定过期时间（Duration）
     */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value), duration);
    }

    /**
     * String 类型操作 - 当 key 不存在时设置（SETNX）
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, JsonUtils.toJson(value));
    }

    /**
     * String 类型操作 - 当 key 不存在时设置，并带过期时间（单位默认为秒）
     */
    public Boolean setIfAbsent(String key, Object value, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, JsonUtils.toJson(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * String 类型操作 - 当 key 不存在时设置，并带过期时间
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, JsonUtils.toJson(value), timeout, unit);
    }

    /**
     * String 类型操作 - 当 key 不存在时设置，并带过期时间（Duration）
     */
    public Boolean setIfAbsent(String key, Object value, Duration duration) {
        return redisTemplate.opsForValue().setIfAbsent(key, JsonUtils.toJson(value), duration);
    }

    /**
     * String 类型操作 - 获取值并反序列化为指定类型
     */
    public <T> T get(String key, Class<T> clz) {
        String value = redisTemplate.opsForValue().get(key);
        return JsonUtils.toObject(value, clz);
    }

    /**
     * String 类型操作 - 获取值并反序列化（支持泛型）
     * e.g. new TypeReference<Map<String, User>>()
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        String value = redisTemplate.opsForValue().get(key);
        return JsonUtils.toObject(value, typeReference);
    }

    /**
     * String 类型操作 - 获取原始 JSON 字符串
     */
    public String getRaw(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * String 类型操作 - 获取旧值并设置新值
     */
    public <T> T getAndSet(String key, Object value, Class<T> clz) {
        String oldValue = redisTemplate.opsForValue().getAndSet(key, JsonUtils.toJson(value));
        return JsonUtils.toObject(oldValue, clz);
    }

    /**
     * String 类型操作 - 获取旧值（原始字符串）并设置新值
     */
    public String getAndSetRaw(String key, Object value) {
        return redisTemplate.opsForValue().getAndSet(key, JsonUtils.toJson(value));
    }

    /**
     * String 类型操作 - 自增（适用于数值类型字符串）
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * String 类型操作 - 按指定步长自增
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * String 类型操作 - 浮点数自增
     * <p>
     * increment("k", 1.5); - 自增
     * increment("k", -1.5); - 自减
     */
    public Double increment(String key, double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * String 类型操作 - 自减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * String 类型操作 - 按指定步长自减
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * Hash 类型操作 - 设置 hash 字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, JsonUtils.toJson(value));
    }

    /**
     * Hash 类型操作 - 当字段不存在时设置
     */
    public Boolean hSetIfAbsent(String key, String field, Object value) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, JsonUtils.toJson(value));
    }

    /**
     * Hash 类型操作 - 批量设置 hash
     */
    public void hPutAll(String key, Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        Map<String, String> jsonMap = new LinkedHashMap<>();
        map.forEach((k, v) -> jsonMap.put(k, JsonUtils.toJson(v)));
        redisTemplate.opsForHash().putAll(key, jsonMap);
    }

    /**
     * Hash 类型操作 - 获取 hash 字段值
     */
    public <T> T hGet(String key, String field, Class<T> clz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return JsonUtils.toObject(value == null ? null : value.toString(), clz);
    }

    /**
     * Hash 类型操作 - 删除 hash 字段
     */
    public Long hDelete(String key, String... fields) {
        return redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    /**
     * Hash 类型操作 - 判断 hash 字段是否存在
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * Hash 类型操作 - 获取所有字段
     */
    public Set<String> hKeys(String key) {
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        if (keys.isEmpty()) {
            return Collections.emptySet();
        }
        return keys.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    /**
     * Hash 类型操作 - 获取 hash 长度
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * List 类型操作 - 左插入
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, JsonUtils.toJson(value));
    }

    /**
     * List 类型操作 - 右插入
     */
    public Long rPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, JsonUtils.toJson(value));
    }

    /**
     * List 类型操作 - 左弹出
     */
    public <T> T lPop(String key, Class<T> clz) {
        return JsonUtils.toObject(redisTemplate.opsForList().leftPop(key), clz);
    }

    /**
     * List 类型操作 - 右弹出
     */
    public <T> T rPop(String key, Class<T> clz) {
        return JsonUtils.toObject(redisTemplate.opsForList().rightPop(key), clz);
    }

    /**
     * List 类型操作 - 获取范围数据
     */
    public <T> List<T> lRange(String key, long start, long end, Class<T> clz) {
        List<String> list = redisTemplate.opsForList().range(key, start, end);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(v -> JsonUtils.toObject(v, clz)).toList();
    }

    /**
     * List 类型操作 - 获取列表长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * Set 类型操作 - 添加元素
     * <p>
     * e.g. Object[] arr = {"a", 1, true};
     * list.toArray();
     */
    public Long sAdd(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        String[] json = Arrays.stream(values).map(JsonUtils::toJson).toArray(String[]::new);
        return redisTemplate.opsForSet().add(key, json);
    }

    /**
     * Set 类型操作 - 获取所有成员
     */
    public <T> Set<T> sMembers(String key, Class<T> clz) {
        Set<String> set = redisTemplate.opsForSet().members(key);
        if (set == null) {
            return Collections.emptySet();
        }
        return set.stream().map(v -> JsonUtils.toObject(v, clz)).collect(Collectors.toSet());
    }

    /**
     * Set 类型操作 - 判断是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, JsonUtils.toJson(value));
    }

    /**
     * Set 类型操作 - 删除元素
     */
    public Long sRemove(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        String[] json = Arrays.stream(values).map(JsonUtils::toJson).toArray(String[]::new);
        return redisTemplate.opsForSet().remove(key, (Object[]) json);
    }

    /**
     * Set 类型操作 - 获取集合大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * ZSet 类型操作 - 添加元素
     * <p>
     * value 不建议使用对象，推荐使用 String
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZSet 类型操作 - 删除元素
     */
    public Long zRemove(String key, String... values) {
        return redisTemplate.opsForZSet().remove(key, (Object[]) values);
    }

    /**
     * ZSet 类型操作 - 获取分数
     */
    public Double zScore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet 类型操作 - 获取排名（升序）
     */
    public Long zRank(String key, String value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * ZSet 类型操作 - 获取排名（降序）
     */
    public Long zReverseRank(String key, String value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * ZSet 类型操作 - 获取元素数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * ZSet 类型操作 - 分数区间查询
     */
    public List<String> zRangeByScore(String key, double min, double max) {
        Set<String> set = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        if (set == null) {
            return Collections.emptyList();
        }
        return set.stream().toList();
    }
}
