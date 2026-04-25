package com.junmoyu.iam.service;

import com.junmoyu.basic.util.RedisUtils;
import com.junmoyu.iam.model.constant.AuthConst;
import com.junmoyu.iam.model.dto.UserPermissionCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务 —— 管理 Redis → 本地缓存 的二级缓存封装。
 *
 * <p>热点路径先查本地缓存，未命中再回源 Redis 并回填。
 * 本地缓存使用 ConcurrentHashMap，TTL 5 分钟，定时清理过期条目。</p>
 *
 * <p>缓存 key：{@code {userId}:{permVer}}，permVer 变化时旧缓存自然失效。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final long LOCAL_TTL_MS = 5 * 60 * 1000;
    private static final long PRUNE_INTERVAL_MS = 60 * 1000;

    private final RedisUtils redisUtils;

    private final ConcurrentHashMap<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-prune");
            t.setDaemon(true);
            return t;
        }).scheduleWithFixedDelay(this::pruneExpired, PRUNE_INTERVAL_MS, PRUNE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取用户权限快照，先查本地缓存，未命中则查 Redis 并回填。
     */
    public UserPermissionCache getPermissions(Long userId, Integer permVer) {
        if (userId == null || permVer == null) {
            return null;
        }
        String cacheKey = userId + ":" + permVer;

        CacheEntry entry = localCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }

        // 清理过期条目
        if (entry != null) {
            localCache.remove(cacheKey);
        }

        String redisKey = AuthConst.buildUserPermissionKey(userId);
        UserPermissionCache fromRedis = redisUtils.get(redisKey, UserPermissionCache.class);
        if (fromRedis != null && fromRedis.permVer().equals(permVer)) {
            localCache.put(cacheKey, new CacheEntry(fromRedis));
            log.debug("权限缓存回填: userId={}, permVer={}", userId, permVer);
        }
        return fromRedis;
    }

    /**
     * 失效所有本地权限缓存（权限变更时调用）。
     */
    public void evictPermissions(Long userId) {
        if (userId == null) {
            return;
        }
        localCache.clear();
        log.info("权限缓存已全量清理: triggered by userId={}", userId);
    }

    private void pruneExpired() {
        int removed = 0;
        Iterator<Map.Entry<String, CacheEntry>> it = localCache.entrySet().iterator();
        while (it.hasNext()) {
            CacheEntry entry = it.next().getValue();
            if (entry.isExpired()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理过期权限缓存: {} 条", removed);
        }
    }

    private static class CacheEntry {
        final UserPermissionCache value;
        final long createdAt;

        CacheEntry(UserPermissionCache value) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > LOCAL_TTL_MS;
        }
    }
}
