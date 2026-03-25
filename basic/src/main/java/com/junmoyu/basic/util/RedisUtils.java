package com.junmoyu.basic.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 工具类
 */
public class RedisUtils {

    private final StringRedisTemplate redisTemplate;

    /**
     * 构造器
     *
     * @param redisTemplate StringRedisTemplate
     */
    public RedisUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * key 操作 - 获得键名列表
     *
     * @param prefix 键名前缀
     * @return 键名列表
     */
    public Set<String> keys(final String prefix) {
        return redisTemplate.keys(prefix);
    }

    /**
     * key 操作 -检查键是否存在
     *
     * @param key 键名
     * @return 如果键存在则返回 true，否则返回 false
     */
    public boolean hasKey(final String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * String 操作 - 缓存基本数据
     *
     * @param key   键名
     * @param value 缓存值
     * @param <T>   自定义类型
     */
    public <T> void set(final String key, final T value) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value));
    }

    /**
     * String 操作 - 缓存基本数据
     *
     * @param key     键名
     * @param value   缓存值
     * @param timeout 过期时间
     * @param <T>     自定义类型
     */
    public <T> void set(final String key, final T value, final long timeout) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * String 操作 - 缓存基本数据
     *
     * @param key      键名
     * @param value    缓存值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @param <T>      自定义类型
     */
    public <T> void set(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(value), timeout, timeUnit);
    }

    /**
     * String 操作 - 设置有效时间（默认单位为秒）
     *
     * @param key     键名
     * @param timeout 过期时间
     * @return 如果设置成功则返回 true，否则返回 false
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * String 操作 - 设置有效时间（自定义时间单位）
     *
     * @param key     键名
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 如果设置成功则返回 true，否则返回 false
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * String 操作 - 获取剩余有效时间（默认单位为秒）
     *
     * @param key 键名
     * @return 剩余有效时间
     */
    public Long getExpire(final String key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * String 操作 - 获取剩余有效时间（自定义时间单位）
     *
     * @param key  键名
     * @param unit 时间单位
     * @return 剩余有效时间
     */
    public Long getExpire(final String key, final TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * String 操作 - 获得缓存数据
     *
     * @param key 键名
     * @param clz 自定义类型
     * @param <T> 自定义类型
     * @return 键值
     */
    public <T> T get(final String key, Class<T> clz) {
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return JsonUtils.toObject(value, clz);
    }

    /**
     * String 操作 - 获得缓存数据
     *
     * @param key 键名
     * @param clz 自定义类型
     * @param <T> 自定义类型
     * @return 键值
     */
    public <T> List<T> getArray(final String key, Class<T> clz) {
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return JsonUtils.toArray(value, clz);
    }

    /**
     * String 操作 - 删除单个数据
     *
     * @param key 键名
     */
    public void delete(final String key) {
        redisTemplate.delete(key);
    }

    /**
     * String 操作 - 删除多个数据
     *
     * @param keys 键名列表
     * @return 成功删除的数量
     */
    public long delete(final Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count == null ? 0 : count;
    }

    /**
     * String 操作 - 递增指定键的值，并返回递增后的结果。
     *
     * @param key 键名
     * @return 递增后的结果，如果键不存在，则创建并将值设置为1，并返回1
     */
    public Long increment(final String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * String 操作 - 递增指定键的值，并返回递增后的结果。
     *
     * @param key   键名
     * @param delta 递增值
     * @return 递增后的结果，如果键不存在，则创建并将值设置为delta，并返回delta
     */
    public Long increment(final String key, final long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * String 操作 - 递减指定键的值，并返回递减后的结果。
     *
     * @param key 键名
     * @return 递减后的结果，如果键不存在，则创建并将值设置为-1，并返回-1
     */
    public Long decrement(final String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * String 操作 - 递减指定键的值，并返回递减后的结果。
     *
     * @param key   键名
     * @param delta 递减值
     * @return 递减后的结果，如果键不存在，则创建并将值设置为-delta，并返回-delta
     */
    public Long decrement(final String key, final long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * List 操作 - 缓存 List 数据
     *
     * @param key    键名
     * @param values 缓存值列表
     * @param <T>    自定义类型
     * @return 操作成功的数量
     */
    public <T> long setList(final String key, final List<T> values) {
        if (CollectionUtils.isEmpty(values)) {
            return 0L;
        }
        List<String> valueList = values.stream().map(JsonUtils::toJson).collect(Collectors.toList());
        Long count = redisTemplate.opsForList().rightPushAll(key, valueList);
        return count == null ? 0 : count;
    }

    /**
     * List 操作 - 获得缓存的 List 对象
     *
     * @param key 键名
     * @param clz 自定义类型
     * @param <T> 自定义类型
     * @return 缓存值列表
     */
    public <T> List<T> getList(final String key, Class<T> clz) {
        List<String> values = redisTemplate.opsForList().range(key, 0, -1);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return JsonUtils.toArray(values, clz);
    }

    /**
     * Set 操作 - 缓存 Set 数据
     *
     * @param key    键名
     * @param values 缓存值 Set 集合
     * @param <T>    自定义类型
     * @return 缓存值集合
     */
    public <T> Long setSet(final String key, final Set<T> values) {
        if (CollectionUtils.isEmpty(values)) {
            return 0L;
        }
        String[] valueList = new String[values.size()];
        int index = 0;
        for (T value : values) {
            valueList[index++] = JsonUtils.toJson(value);
        }
        return redisTemplate.opsForSet().add(key, valueList);
    }

    /**
     * Set 操作 - 获得缓存的 Set 数据
     *
     * @param key 键名
     * @param clz 自定义类型
     * @param <T> 自定义类型
     * @return 缓存值
     */
    public <T> Set<T> getSet(final String key, Class<T> clz) {
        Set<String> values = redisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptySet();
        }
        return JsonUtils.toArray(values, clz);
    }

    /**
     * Hash Map 操作 - 缓存 Map 数据
     *
     * @param key    键名
     * @param subKey 子键名
     * @param value  缓存值
     * @param <T>    自定义类型
     */
    public <T> void setMap(final String key, final String subKey, T value) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        opsForHash.put(key, subKey, JsonUtils.toJson(value));
    }

    /**
     * Hash Map 操作 - 缓存 Map 数据
     *
     * @param key      键名
     * @param valueMap 缓存值
     * @param <T>      自定义类型
     */
    public <T> void setMap(final String key, final Map<String, T> valueMap) {
        if (MapUtils.isEmpty(valueMap)) {
            return;
        }
        Map<String, String> map = new HashMap<>(valueMap.size());
        for (String k : valueMap.keySet()) {
            map.put(k, JsonUtils.toJson(valueMap.get(k)));
        }
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        opsForHash.putAll(key, map);
    }

    /**
     * Hash Map 操作 - 获得缓存的 Map 数据
     *
     * @param key    键名
     * @param subKey 子键名
     * @param clz    自定义类型
     * @param <T>    自定义类型
     * @return 缓存值
     */
    public <T> T getMap(final String key, final String subKey, Class<T> clz) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String value = opsForHash.get(key, subKey);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return JsonUtils.toObject(value, clz);
    }

    /**
     * Hash Map 操作 - 获得缓存的 Map 数据
     *
     * @param key 键名
     * @param clz 自定义类型
     * @param <T> 自定义类型
     * @return 缓存值集合
     */
    public <T> Map<String, T> getMap(final String key, Class<T> clz) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        Map<String, String> entries = opsForHash.entries(key);
        if (MapUtils.isEmpty(entries)) {
            return null;
        }
        Map<String, T> valueMap = new HashMap<>(entries.size());
        for (String k : entries.keySet()) {
            String value = entries.get(k);
            if (StringUtils.isBlank(value)) {
                valueMap.put(k, null);
            } else {
                valueMap.put(k, JsonUtils.toObject(value, clz));
            }
        }
        return valueMap;
    }
}
